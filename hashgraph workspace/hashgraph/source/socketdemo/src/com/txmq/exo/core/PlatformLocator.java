package com.txmq.exo.core;

import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;

public class PlatformLocator {
	private static Platform platform;
	
	public static void init(Platform platform) {
		PlatformLocator.platform = platform;
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
}
