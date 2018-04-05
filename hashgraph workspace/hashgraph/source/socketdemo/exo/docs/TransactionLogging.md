Exo Transaction Logging
=======================

Exo provides an easy-to-use transaction logger that can automatically log transactions to persistent storage in a blockchain data structure.  The logger groups transactions into blocks and writes those blocks to storage.  Each block is signed with an SHA-256 hash, and the block incorporates the hash of the previous block to maintain data integrity in the chain.

The logger uses a plug-in mechanism to allow the logger to work with any type of data storage.  A CouchDB plugin is provided with more implementations on the way.

## Initializing the logger
To initialize the logger, you create an instance of your storage-specific logger plugin and pass it to the logger.  The logger should be initialized in the init() method of your SwirldMain and can be done as part of the platform initialization:

```Java
String[] transactionProcessorPackages = {"com.txmq.exo.messaging.rest", "com.txmq.socketdemo.transactions"};
CouchDBBlockLogger blockLogger = new CouchDBBlockLogger(
        "zoo-" + platform.getAddress().getSelfName().toLowerCase(),
        "http",
        "couchdb",
        //"localhost",
        5984);
ExoPlatformLocator.init(    platform, 
                            SocketDemoTransactionTypes.class, 
                            transactionProcessorPackages, 
                            blockLogger);
```

It can also be initialized separately, but should still be done in the init() method of your SwirldMain:
```java
ExoPlatformLocator.getBlockLogger.setLogger(
    blockLogger, 
    "zoo-" + platform.getAddress().getSelfName().toLowerCase());
```

## Building Transaction Logger Plug-ins
If you need to support a logging target other than CouchDB, you can implement your own logger plugin.  Logger plugins implement the IBlockLogger interface.

```java
/**
 * Interface that defines what a storage-specific block logger 
 * has to implement.  Exo is written to deal with IBlockLoggers, 
 * not concrete instances of storage-specific loggers.
 */
public interface IBlockLogger {

    /**
     * Adds a transaction to the next block
     */
    public void addTransaction(ExoMessage transaction);
    
    /**
     * Asks the logger to persist a block to storage
     */
    public void save(Block block);
    
}
```

Your plug-in is responsible for managing its internal transaction list and the mechanics for saving the block to whatever storage your plug-in uses.  Note that even though the save method is public, it is not automatically invoked by the logger.  Your plug-in should decide when to invoke the save method.  The CouchDB plugin writes blocks when a block contains a certain number of transactions, but you could implement a timeout-based mechanism or calculate the size of the data in the block to trigger writes, for example.