package com.txmq.exo.core;

import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.persistence.BlockLogger;
import com.txmq.exo.persistence.IBlockLogger;
import com.txmq.exo.transactionrouter.ExoTransactionRouter;

/**
 * A static locator class for Exo platform constructs.  This class allows applications
 * to get access to Swirlds platform, Swirlds state, and the transaction router from
 * anywhere in the application.
 * 
 * I'm not in love with using static methods essentially as global variables.  I'd 
 * love to hear ideas on a better way to approach this.
 */
public class ExoPlatformLocator {
	/**
	 * Reference to the Swirlds platform
	 */
	private static Platform platform;

	/**
	 * Reference to Exo's transaction router.  An application should have only one
	 * router.  Developers should never need to create one.
	 */
	private static ExoTransactionRouter transactionRouter = new ExoTransactionRouter();
	
	/**
	 * Reference to the block logging manager
	 */
	private static BlockLogger blockLogger = new BlockLogger();
	
	/**
	 * Initialization method for the platform.  This should be called by your main's 
	 * init() or run() methods
	 */
	public static void init(Platform platform) {
		ExoPlatformLocator.platform = platform;
		
	}
	
	public static void init(Platform platform, String[] transactionProcessorPackages) {
		init(platform);
		for (String tpp : transactionProcessorPackages) {
			transactionRouter.addPackage(tpp);
		}
	}
	
	public static void init(Platform platform, String[] transactionProcessorPackages, IBlockLogger logger) {
		init(platform, transactionProcessorPackages);
		blockLogger.setLogger(logger,  platform.getAddress().getSelfName());
	}
	
	public static void init(Platform platform, String[] transactionProcessorPackages, IBlockLogger logger, String nodeName) {
		init(platform, transactionProcessorPackages);
		blockLogger.setLogger(logger,  nodeName);
	}
	
	/**
	 * Accessor for a reference to the Swirlds platform.  Developers must call 
	 * ExoPlatformLocator.init() to intialize the locator before calling getPlatform()
	 */
	public static Platform getPlatform() throws IllegalStateException {
		if (platform == null) {
			throw new IllegalStateException(
				"PlatformLocator has not been initialized.  " + 
				"Please initialize PlatformLocator in your SwirldMain implementation."
			);
		}
		
		return platform;
	}
	
	/**
	 * Accessor for Swirlds state.  Developers must call ExoPlatformLocator.init()
	 * to initialize the locator before calling getState()
	 */
	public static SwirldState getState() throws IllegalStateException {
		if (platform == null) {
			throw new IllegalStateException(
				"PlatformLocator has not been initialized.  " + 
				"Please initialize PlatformLocator in your SwirldMain implementation."
			);
		}
		
		return platform.getState();
	}
	
	/**
	 * Accessor for the Exo transaction router.  
	 * @see com.txmq.exo.transactionrouter.ExoTransactionRouter
	 */
	public static ExoTransactionRouter getTransactionRouter() {
		return transactionRouter;
	}
	
	/**
	 * Accessor for the block logging manager
	 */
	public static BlockLogger getBlockLogger() {
		return blockLogger;
	}
}
