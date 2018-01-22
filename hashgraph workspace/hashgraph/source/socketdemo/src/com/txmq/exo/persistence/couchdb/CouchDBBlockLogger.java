package com.txmq.exo.persistence.couchdb;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.lightcouch.CouchDbClient;

import com.txmq.exo.core.PlatformLocator;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.persistence.Block;
import com.txmq.exo.persistence.IBlockLogger;

public class CouchDBBlockLogger implements IBlockLogger {
	//TODO:  Make this configurable
	private int BLOCK_SIZE = 4;
	private Block block;
	private CouchDbClient client;
	private Map<Integer, Integer> processedTransactions = new HashedMap<Integer, Integer>();
	
	public CouchDBBlockLogger() {
		this.client = new CouchDbClient();
		this.initialize();
	}
	
	public CouchDBBlockLogger(String dbName, String protocol, String host, int port) {
		this.client = new CouchDbClient(dbName, true, protocol, host, port, null, null);
		this.initialize();
	}
	
	
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

	private synchronized void initialize() {
		this.block = new Block();
		//TODO:  Should be the string rep of the Swirld ID from the platform, not the string "GENESIS_BLOCK"
		this.block.setPreviousBlockHash("GENESIS_BLOCK");
	}
		
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
