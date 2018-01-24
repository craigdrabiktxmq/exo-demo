package com.txmq.exo.persistence.couchdb;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.lightcouch.CouchDbClient;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.persistence.Block;
import com.txmq.exo.persistence.IBlockLogger;

/**
 * Implementation of IBlockLogger for CouchDB.  The logger works by writing 
 * blocks as JSON to a CouchDB instance.  Each block appears in CouchDB as 
 * a separate document.  The chain can be navigated by index or by following
 * the hash values - the genesis block's hash is included in the next block's
 * previousBlockHash value, and so on.
 * 
 * TODO:  Make this more configurable - set up block size, and add a properties
 * file based scheme for setting CouchDB logger parameters.
 */
public class CouchDBBlockLogger implements IBlockLogger {
	//TODO:  Make this configurable
	private int BLOCK_SIZE = 4;
	private Block block;
	private CouchDbClient client;
	private Map<Integer, Integer> processedTransactions = new HashedMap<Integer, Integer>();
	
	/**
	 * No-op constructor, will construct a logger pointed at the 
	 * database identified by the LightCouch properties file.
	 * 
	 * @see org.lightcouch.CouchDbClient
	 */
	public CouchDBBlockLogger() {
		this.client = new CouchDbClient();
		this.initialize();
	}
	
	/**
	 * Constructor which allows the instantiating code to pass 
	 * in a subset of common CouchDBClient configuration variables 
	 * used to instantiate the logger's CouchDBClient..
	 * 
	 * @see org.lightcouch.CouchDbClient
	 */
	public CouchDBBlockLogger(String dbName, String protocol, String host, int port) {
		this.client = new CouchDbClient(dbName, true, protocol, host, port, null, null);
		this.initialize();
	}
	
	/**
	 * Constructor which allows the instantiating code to pass in CouchDBClient
	 * configuration variables used to instantiate the logger's CouchDBClient..
	 * 
	 * @see org.lightcouch.CouchDbClient
	 */
	public CouchDBBlockLogger(
			String dbName, 
			boolean createDbIfNotExist, 
			String protocol, 
			String host, 
			int port, 
			String username, 
			String password) 
	{
		this.client = new CouchDbClient(dbName, createDbIfNotExist, protocol, host, port, username, password);
		this.initialize();
	}

	/**
	 * Initializes the logger, preparing it to write its first block.
	 * 
	 * TODO:  The logger isn't capable of recovery..  It starts a new 
	 * chain.  This might actually be the desirable approach - the fact 
	 * that a new genesis block was written indicates a node was restarted.  
	 * There probably isn't enough information in the block to identify 
	 * which chain is which.  Maybe add a timestamp, or some kind of 
	 * identifier for the chain instance.  This could be added as the first 
	 * transaction in the genesis block without disturbing other code or 
	 * changing the structure of the block data.
	 */
	private synchronized void initialize() {
		this.block = new Block();
		//TODO:  Should be the string rep of the Swirld ID from the platform, 
		//not the string "GENESIS_BLOCK"?  Maybe we add the Swirld ID to the
		//initial chain transaction described above?
		this.block.setPreviousBlockHash("GENESIS_BLOCK");
	}
		
	/**
	 * Adds a trasnaction to the block.  The logger tracks transactions that
	 * have been added to blocks to ensure that transactions are written 
	 * only once.
	 * 
	 * TODO:  We probably don't need to track the transactions any more.
	 * I was doing this while debugging the block writer.  The issue it
	 * was intended to mitigate was probably fixed by tracking separate
	 * loggers for each node, and only logging transactions that have 
	 * reached consensus
	 */
	@Override
	public synchronized void addTransaction(ExoMessage transaction) {
		if (!this.processedTransactions.containsKey(transaction.uuidHash)) { 
			this.processedTransactions.put(transaction.uuidHash, 1);
			this.block.addTransaction(transaction);
			if (this.block.getBlockSize() == this.BLOCK_SIZE) {
				this.save(block);
			}
		} else {
			Integer count = this.processedTransactions.get(transaction.uuidHash);
			this.processedTransactions.put(transaction.uuidHash, count + 1);
		}
	}

	/**
	 * Saves the block to CouchDB.  Creates a new current block, 
	 * commits the previous block and writes that block's hash 
	 * into the new current block's previousBlockHash property.
	 */
	@Override
	public synchronized void save(Block block) {
		Block blockToSave = this.block;
		this.block = new Block();
		this.block.setIndex(blockToSave.getIndex() + 1);
		blockToSave.commit();
		this.block.setPreviousBlockHash(blockToSave.getHash());
		this.client.save(blockToSave);
	}

}
