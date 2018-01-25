package com.txmq.exo.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.messaging.rest.CORSFilter;
import com.txmq.exo.messaging.socket.TransactionServer;
import com.txmq.exo.persistence.BlockLogger;
import com.txmq.exo.persistence.IBlockLogger;
import com.txmq.exo.transactionrouter.ExoTransactionRouter;
import com.txmq.socketdemo.SocketDemoTransactionTypes;

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
	public static synchronized void init(Platform platform) {
		ExoPlatformLocator.platform = platform;
		
	}
	
	public static synchronized void init(	Platform platform, 
							Class<? extends ExoTransactionType> transactionTypeClass, 
							String[] transactionProcessorPackages) {
		init(platform);
		
		//Hokey, but we cause the transaction type class to initialize itself by simply instantiating one..
		try {
			transactionTypeClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String tpp : transactionProcessorPackages) {
			transactionRouter.addPackage(tpp);
		}
	}
	
	public static synchronized void init(	Platform platform, 
							Class<? extends ExoTransactionType> transactionTypeClass,
							String[] transactionProcessorPackages, 
							IBlockLogger logger) {
		init(platform, transactionTypeClass, transactionProcessorPackages);
		blockLogger.setLogger(logger,  platform.getAddress().getSelfName());
	}	
	
	/**
	 * Initializes Grizzly-based REST interfaces defined in the included package list, listening on the included port.
	 * Enabling REST will automatically expose the endpoints service and generate an ANNOUNCE_NODE message.
	 * @param port
	 * @param packages
	 */
	public static void initREST(int port, String[] packages) {
		URI baseUri = UriBuilder.fromUri("http://0.0.0.0").port(port).build();
		ResourceConfig config = new ResourceConfig()
				.packages("com.txmq.exo.messaging.rest")
				.register(new CORSFilter())
				.register(JacksonFeature.class);
		
		for (String pkg : packages) {
			config.packages(pkg);
		}
		
		System.out.println("Attempting to start Grizzly on " + baseUri);
		GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
		
		try {
			platform.createTransaction(
				new ExoMessage(
					new ExoTransactionType(ExoTransactionType.ANNOUNCE_NODE),
					baseUri.toString()
				).serialize(),
				null
			);
					
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Sets up a socket-based API for communicating with this Swirld on the supplied 
	 * port.  Scans the supplied list of packages for methods annotated with 
	 * @ExoTransaction and automatically maps incoming messages to matching handlers.  
	 * Any transactions which have not been mapped are passed through the platform to
	 * be processed by the model.  
	 * @param port
	 * @param packages
	 */
	public static void initSocketMessaging(int port, String[] packages) {
		new TransactionServer(platform, port, packages).start();
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
