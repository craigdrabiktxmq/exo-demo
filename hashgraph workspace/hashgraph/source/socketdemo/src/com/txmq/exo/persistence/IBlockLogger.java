package com.txmq.exo.persistence;

import com.txmq.exo.messaging.ExoMessage;

public interface IBlockLogger {

	public void addTransaction(ExoMessage transaction);
    public void save(Block block);
    
}