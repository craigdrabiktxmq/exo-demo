package com.txmq.exo.persistence;

import java.io.IOException;

import com.txmq.exo.messaging.ExoMessage;

public class Block {
	private BlockContents contents = new BlockContents();
	private String hash;
	private boolean committed = false;
	private double index;
	
	public int getBlockSize() {
		return this.contents.transactions.size();
	}
	
	public void setPreviousBlockHash(String previousBlockHash) {
		this.contents.previousBlockHash = previousBlockHash;
	}
	
	public String getHash() {
		return this.hash;
	}
	
	public double getIndex() {
		return this.index;
	}
	
	public void setIndex(double index) {
		this.index = index;
	}
	
	public void addTransaction(ExoMessage transaction) {
		if (this.committed == true) {
			throw new IllegalStateException("This block has already been committed.  No more transactions can be added to a committed block");
		}
		this.contents.transactions.add(transaction);
	}
	
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
