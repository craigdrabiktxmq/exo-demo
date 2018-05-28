package com.txmq.exo.persistence;

import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import com.txmq.exo.messaging.ExoMessage;

/**
 * Interface that defines what a storage-specific block logger 
 * has to implement.  Exo is written to deal with IBlockLoggers, 
 * not concrete instances of storage-specific loggers.
 */
public interface IBlockLogger {

    /**
     * Adds a transaction to the next block
     */
    public void addTransaction(ExoMessage<?> transaction);
    
    /**
     * Asks the logger to persist a block to storage
     */
    public void save(Block block);
    
    /**
     * Used to configure a logger from an exo-config file.  The contents 
     * of the parameter list will be specific to each logger
     * @param parameters
     */
    public void configure(DefaultKeyValue<String, String>[] parameters);
    
    /**
     * Used to flush to database/disk before shutting down the hashgraph
     */
    public void flush();
}