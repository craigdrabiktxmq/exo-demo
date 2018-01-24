package com.txmq.exo.persistence;

import java.util.HashMap;
import java.util.Map;

import com.txmq.exo.messaging.ExoMessage;

/**
 * BlockLogger is the manager class for Exo's "low rent" blockchain transaction
 * logging.  It serves as a locator for a singleton logger implementation, and 
 * includes an addTransaction() utility method so code that wants to log data 
 * can do so right from the locator.
 */
public class BlockLogger {
	/**
	 * A collection of logger instances.  I kind of lied when I described the 
	 * logger as a singleton.  Static properties and methods behave strangely 
	 * in Hashgraph applications running the way the demos run.  I suspect 
	 * it's a combination of the nodes all running as children of the same 
	 * root process combined with non-synchronized accessor methods.  The symptom
	 * is that each node winds up creating a logger for itself, but they all wind 
	 * up using a single logger.  This property indexes a node's logger to the node 
	 * name to work around the problem.  It should cause minimal overhead when used 
	 * in a production setting.
	 */
	private static Map<String, IBlockLogger> loggers = new HashMap<String, IBlockLogger>();
	
	/**
	 * Setter for assingning a logger to a node and 
	 * making it available through a static accessor.
	 */
	public static void setLogger(IBlockLogger logger, String nodeName) {
		//BlockLogger.logger = logger;
		loggers.put(nodeName, logger);
	}
	
	/**
	 * Retrieves the logger associated with the supplied node name
	 */
	public static IBlockLogger getLogger(String nodeName) {
		return loggers.get(nodeName);
	}
	
	/**
	 * Utility method that passes a transaction to a node's logger, 
	 * saving a call to getLogger() for the calling code.
	 */
	public static void addTransaction(ExoMessage transaction, String nodeName) {
		System.out.println("logging transaction for " + nodeName);
		//TODO:  Figure out a way to cut in notification that a transaction has reached consensus and has been processed by the hashgraph.
		getLogger(nodeName).addTransaction(transaction);
	}
}
