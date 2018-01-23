package com.txmq.exo.core;

import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.transactionrouter.ExoTransactionRouter;

public class ExoPlatformLocator {
	private static Platform platform;
	private static ExoTransactionRouter transactionRouter = new ExoTransactionRouter();
	
	public static void init(Platform platform) {
		ExoPlatformLocator.platform = platform;
	}
	
	public static Platform getPlatform() throws IllegalStateException {
		if (platform == null) {
			throw new IllegalStateException(
				"PlatformLocator has not been initialized.  " + 
				"Please initialize PlatformLocator in your SwirldMain implementation."
			);
		}
		
		return platform;
	}
	
	public static SwirldState getState() throws IllegalStateException {
		if (platform == null) {
			throw new IllegalStateException(
				"PlatformLocator has not been initialized.  " + 
				"Please initialize PlatformLocator in your SwirldMain implementation."
			);
		}
		
		return platform.getState();
	}
	
	public static ExoTransactionRouter getTransactionRouter() {
		return transactionRouter;
	}
}
