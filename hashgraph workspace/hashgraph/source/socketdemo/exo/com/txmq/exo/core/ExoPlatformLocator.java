package com.txmq.exo.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.Instant;
import java.util.Random;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.config.ExoConfig;
import com.txmq.exo.config.MessagingConfig;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.messaging.rest.CORSFilter;
import com.txmq.exo.messaging.socket.TransactionServer;
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
	 * When run in test mode, Exo will maintain a single instance of the application's 
	 * state so that JUnit tests can run against code that requires data 
	 * in the state, without a dependency on the platform being up and running.
	 * 
	 * Block logging will be disabled when running in test mode.
	 */
	private static ExoState testState = null;
	
	/**
	 * Places Exo in test mode, using the passed-in instance of a state.  This is useful
	 * for automated testing where you may want to configure an application state manually
	 * and run a series of tests against that known state.
	 */
	public static void enableTestMode(ExoState state) {
		ExoPlatformLocator.testState = state;
	}
	
	/**
	 * Places Exo in test mode.  Exo will create an instance 
	 * of the supplied type to service as a mock state. 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void enableTestMode(Class<? extends ExoState> stateClass) throws InstantiationException, IllegalAccessException {
		ExoPlatformLocator.testState = stateClass.newInstance();
	}
	
	/**
	 * Indicates that the node should shut down 
	 */
	private static boolean shouldShutdown = false;
	
	public static boolean shouldShutdown() {
		return shouldShutdown;
	}
	
	public static void shutdown() {
		shouldShutdown = true;
	}
	
	/**
	 * Initializes the platform from an exo-config.json file located 
	 * in the same directory as the application runs in.
	 * @throws ClassNotFoundException 
	 */
	public static synchronized void initFromConfig(Platform platform) {
		ExoPlatformLocator.platform = platform;
		ExoConfig config = ExoConfig.getConfig();
		
		//Initialize transaction types
		Class<? extends ExoTransactionType> transactionTypeClass;
		try {
			Class<?> ttClass = Class.forName(config.hashgraphConfig.transactionTypesClassName);
			transactionTypeClass = (Class<? extends ExoTransactionType>) ttClass;			
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
				"Error instantiating transaction types enumerator: " + config.hashgraphConfig.transactionTypesClassName
			);
		}
		
		init(platform, transactionTypeClass, config.hashgraphConfig.transactionProcessors);
		
		//Set up socket messaging, if it's in the config..
		MessagingConfig messagingConfig = null; 
		if (config.hashgraphConfig.socketMessaging != null) {
			try {
				messagingConfig = parseMessagingConfig(config.hashgraphConfig.socketMessaging);
				if (messagingConfig.secured == true) {
					initSecuredSocketMessaging(	messagingConfig.port, 
												messagingConfig.handlers, 
												messagingConfig.clientKeystore.path, 
												messagingConfig.clientKeystore.password, 
												messagingConfig.serverKeystore.path, 
												messagingConfig.serverKeystore.password);
				} else {
					initSocketMessaging(messagingConfig.port, messagingConfig.handlers);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Error configuring socket messaging:  " + e.getMessage());
			}
		}
		
		//Set up REST, if it's in the config..
		if (config.hashgraphConfig.rest != null) {
			try {
				messagingConfig = parseMessagingConfig(config.hashgraphConfig.rest);
				initREST(messagingConfig);
				//initREST(messagingConfig.port, messagingConfig.handlers);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Error configuring REST:  " + e.getMessage());
			}
		}
		
		//configure block logging, if indicated in the config file
		if (config.hashgraphConfig.blockLogger != null && ExoPlatformLocator.testState == null) {
			try {
				Class<? extends IBlockLogger> loggerClass = 
					(Class<? extends IBlockLogger>) Class.forName(config.hashgraphConfig.blockLogger.loggerClass);
				IBlockLogger logger = loggerClass.newInstance();
				logger.configure(config.hashgraphConfig.blockLogger.parameters);
				blockLogger.setLogger(logger, platform.getAddress().getSelfName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException("Error configuring block logger:  " + e.getMessage());
			}			
		}				
	}
	
	private static MessagingConfig parseMessagingConfig(MessagingConfig config) {
		MessagingConfig result = new MessagingConfig();
		
		//If a port has been defined in the config, use it over the derived port.
		if (config.port > 0) {
			result.port = config.port;				
		} else {
			//Test if there's a derived port value.  If not, we have an invalid messaging config
			if (config.derivedPort != null) {
				//Calculate the port for socket connections based on the hashgraph's port
				//If we're in test mode, mock this up to be a typical value, e.g. 5220X
				if (testState == null) {
					result.port = platform.getAddress().getPortExternalIpv4() + config.derivedPort;
				} else {
					result.port = 50204 + config.derivedPort;
				}
			} else {
				throw new IllegalArgumentException(
					"One of \"port\" or \"derivedPort\" must be defined."
				);
			}
		}
		
		if (config.handlers != null && config.handlers.length > 0) {
			result.handlers = config.handlers;
		} else {
			throw new IllegalArgumentException(
				"No handlers were defined in configuration"
			);
		}
		
		result.secured = config.secured;
		result.clientKeystore = config.clientKeystore;
		result.clientTruststore = config.clientTruststore;
		result.serverKeystore = config.serverKeystore;
		result.serverTruststore = config.serverTruststore;
		return result;
	}
	
	/**
	 * Initializes the platform from an exo-condig.json 
	 * file located using the path parameter.
	 * @param path
	 * @throws ClassNotFoundException 
	 */
	public static synchronized void initFromConfig(Platform platform, String path) {
		ExoConfig.loadConfiguration(path);
		initFromConfig(platform);
	}
	
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
		MessagingConfig internalConfig = new MessagingConfig();
		internalConfig.port = port;
		internalConfig.handlers = packages;
		
		initREST(internalConfig);		
	}
	
	/**
	 * Initializes Grizzly-based REST interfaces as defined in the messaging config.  This method will
	 * allow for REST endpoints using HTTPS by defining keystore information in exo-config.json.
	 * Enabling REST will automatically expose the endpoints service and generate an ANNOUNCE_NODE message.
	 * @param restConfig
	 */
	public static void initREST(MessagingConfig restConfig) {
		URI baseUri = UriBuilder.fromUri("http://0.0.0.0").port(restConfig.port).build();
		ResourceConfig config = new ResourceConfig()
				.packages("com.txmq.exo.messaging.rest")
				.register(new CORSFilter())
				.register(JacksonFeature.class)
				.register(MultiPartFeature.class);
		
		for (String pkg : restConfig.handlers) {
			config.packages(pkg);
		}
		
		System.out.println("Attempting to start Grizzly on " + baseUri);
		if (restConfig.secured == true) {
			SSLContextConfigurator sslContext = new SSLContextConfigurator();
			sslContext.setKeyStoreFile(restConfig.serverKeystore.path);
			sslContext.setKeyStorePass(restConfig.serverKeystore.password);
			sslContext.setTrustStoreFile(restConfig.serverTruststore.path);
			sslContext.setTrustStorePass(restConfig.serverTruststore.password);
			
			GrizzlyHttpServerFactory.createHttpServer(
				baseUri, 
				config, 
				true, 
				new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false)
			);
		} else {
			GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
		}
		
		try {
			createTransaction(
				new ExoMessage(
					new ExoTransactionType(ExoTransactionType.ANNOUNCE_NODE),
					baseUri.toString()
				)
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
	 * 
	 * This method creates unsecured sockets with no authentication or in-transit encryption.  
	 * Beware of dragons.
	 * 
	 * @param port
	 * @param packages
	 */
	public static void initSocketMessaging(int port, String[] packages) {
		new TransactionServer(platform, port, packages).start();
	}
	
	/**
	 * Sets up a TLS-encrypted socket-based API for communicating with this Swirld on 
	 * the supplied port.  X.509 certs are used to authenticate connecting clients, 
	 * and vice-versa.  Scans the supplied list of packages for methods annotated with 
	 * @ExoTransaction and automatically maps incoming messages to matching handlers.  
	 * Any transactions which have not been mapped are passed through the platform to
	 * be processed by the model.
	 * 
	 * @param port
	 * @param packages
	 */
	public static void initSecuredSocketMessaging(	int port, 
													String[] packages, 
													String clientKeystorePath,
													String clientKeystorePassword,
													String serverKeystorePath,
													String serverKeystorePassword) {
		new TransactionServer(	platform, 
								port, 
								packages, 
								clientKeystorePath, 
								clientKeystorePassword, 
								serverKeystorePath, 
								serverKeystorePassword).start();
	}
	
	/**
	 * Submits a transaction to the platform.  Applications should use this method
	 * over ExoPlatformLocator.getPlatform().createTransaction() to enable unit testing 
	 * via test mode.
	 * 
	 * This signature is a convenience method for passing ExoMessages.
	 */
	public static boolean createTransaction(ExoMessage transaction) throws IOException {
		return createTransaction(transaction.serialize(), null);
	}
	
	/**
	 * Submits a transaction to the platform.  Applications should use this method
	 * over ExoPlatformLocator.getPlatform().createTransaction() to enable unit testing 
	 * via test mode.
	 * 
	 * This signature matches the createTransaction signature of the Swirlds Platform.
	 */
	public static boolean createTransaction(byte[] transaction, long[] hintIds) {
		if (testState == null) {
			return platform.createTransaction(transaction);
		} else {
			long transactionID = new Random().nextLong();
			Instant timeCreated = Instant.now();
			
			ExoState preConsensusState = null;
			try {
				preConsensusState = (ExoState) testState.getClass().getConstructors()[0].newInstance();
			} catch (Exception e) {
				// TODO Better error handling..
				e.printStackTrace();
			}

			preConsensusState.copyFrom((SwirldState) testState);
			preConsensusState.handleTransaction(transactionID, false, timeCreated, transaction, null);
			testState.handleTransaction(transactionID, true, timeCreated, transaction, null);
			return true;
		}
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
	 * to initialize the locator before calling getState(), unless running in test mode.
	 * 
	 * Developers should prefer this method to ExoPlatformLocator.getPlatform().getState()
	 * because this method supports returning a state in test mode without initializing
	 * the platform.
	 */
	public static SwirldState getState() throws IllegalStateException {
		if (ExoPlatformLocator.testState == null) {
			if (platform == null) {
				throw new IllegalStateException(
					"PlatformLocator has not been initialized.  " + 
					"Please initialize PlatformLocator in your SwirldMain implementation."
				);
			}
			
			return platform.getState();
		} else {
			return (SwirldState) testState;
		}
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
