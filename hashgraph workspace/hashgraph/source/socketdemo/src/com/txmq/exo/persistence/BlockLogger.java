package com.txmq.exo.persistence;

import java.util.HashMap;
import java.util.Map;

import com.txmq.exo.core.PlatformLocator;
import com.txmq.exo.messaging.ExoMessage;

public class BlockLogger {
	//private static IBlockLogger logger;
	private static Map<String, IBlockLogger> loggers = new HashMap<String, IBlockLogger>();
	
	public static void setLogger(IBlockLogger logger, String nodeName) {
		//BlockLogger.logger = logger;
		loggers.put(nodeName, logger);
	}
	
	public static IBlockLogger getLogger(String nodeName) {
		return loggers.get(nodeName);
	}
	
	public static void addTransaction(ExoMessage transaction, String nodeName) {
		System.out.println("logging transaction for " + nodeName);
		//TODO:  Figure out a way to cut in notification that a transaction has reached consensus and has been processed by the hashgraph.
		getLogger(nodeName).addTransaction(transaction);
	}
}
