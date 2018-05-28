package com.txmq.exo.persistence;

import com.txmq.exo.messaging.ExoMessage;

/**
 * A Block defines the "wrapper" for a set of transactions written out 
 * to storage by an IBlockLogger.  Transactions are added to the block 
 * by Exo's block logging scheme until the block reaches capacity.  
 * "Capacity" is defined by the individual block logger, and could be 
 * based on transaction length, data size, or time-based.  One the block 
 * is "full", it gets marked as "committed".  A committed block cannot 
 * accept additional transactions, and will be written to storage and 
 * then discarded from memory.
 * 
 * @see IBlockLogger
 * @see BlockLogger
 * @see BlockContents
 */
public class Block {

	/**
	 * Contents of the block - a list of transactions plus the previous 
	 * block's signature.  The contents are segregated from the block 
	 * itself so that it can be hashed independently.
	 * 
	 * @see BlockContents
	 */
	private BlockContents contents = new BlockContents();

	/**
	 * Calculated hash for the block contents
	 */
	private String hash;

	/**
	 * Indicates that the block has been committed - 
	 * either it has been or is about to be written 
	 * to storage and cannot accept new transactions.
	 */
	private boolean committed = false;

	/**
	 * Sequential block index, helps for easy ordering 
	 * of blocks e.g. in a document database.
	 */
	private double index;
	
	/**
	 * Accessor for block size.  In the default implementation, 
	 * this is the number of transactions in the block.
	 */
	public int getBlockSize() {
		return this.contents.transactions.size();
	}
	
	/**
	 * Setter for storing the hash of the previous block.  The 
	 * previous block's hash is incorporated into this block's 
	 * hash, which is how the integrity of the chain is ensured.
	 */
	public void setPreviousBlockHash(String previousBlockHash) {
		this.contents.previousBlockHash = previousBlockHash;
	}
	
	/**
	 * Public accessor for the block's hash
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * Public accessor for the block's index
	 */
	public double getIndex() {
		return this.index;
	}
	
	/**
	 * Public setter for the block's index.  This should be set 
	 * by the logger and not tampered with by application code.
	 * 
	 * //TODO: Maybe should be protected, not public?
	 */
	public void setIndex(double index) {
		this.index = index;
	}
	
	/**
	 * Adds a transaction to the block.
	 * 
	 * TODO:  Maybe should be protected, not public?
	 */
	public void addTransaction(ExoMessage<?> transaction) {
		if (this.committed == true) {
			throw new IllegalStateException("This block has already been committed.  No more transactions can be added to a committed block");
		}
		this.contents.transactions.add(transaction);
	}
	
	/**
	 * Sets the block state to committed.  Once the block is marked 
	 * as committed, it will refuse to accept additional transactions.
	 */
	public void commit() {
		if (this.contents.previousBlockHash == null) {
			throw new IllegalStateException("A block must have a value set for the previous block hash before it can be committed.");
		}
		
		if (this.committed == true) {
			throw new IllegalStateException("This block has already been committed.  A block may only be committed once");
		}

		this.committed = true;
		this.hash = this.contents.hash();
	}
}
