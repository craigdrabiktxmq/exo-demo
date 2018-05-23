package com.txmq.exo.messaging.socket;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.swirlds.platform.Platform;

/**
 * TransactionServer is the "controller" for the socket-based Hashgraph integration 
 * functionality in Exo.  It manages the keys used to authenticate client and server, 
 * sets up the server socket and listens for incoming connection requests.
 * 
 * It gets spawned on a separate thread, land when incoming connects are accepted, 
 * spawns a new thread to handle those transactions.
 * 
 * @see com.txmq.exo.messaging.socket.TransactionServerConnection
 */
public class TransactionServer extends Thread {

	/**
	 * A pointer to the Swirlds platform, which it passes 
	 * to the spawned TransactionServerConnection threads.
	 * 
	 * TDOD:  We can replace this with ExoPlatformLocator
	 */
	private Platform platform;
	private ServerSocket serverSocket;
	private ExoMessageRouter messageRouter;
	
	/**
	 * Creates an unsecured socket connection listening on the supplied port.
	 * 
	 * @param platform
	 * @param port
	 * @param packages
	 */
	public TransactionServer(Platform platform, int port, String[] packages) {
		
		this.initialize(platform, packages);
		try {
			this.serverSocket = new ServerSocket(port);
			System.out.println("WARNING:  Unsecured socket has been opened for transactions");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TransactionServer(Platform platform, 
							int port, 
							String[] packages, 
							String clientKeystorePath,
							String clientKeystorePassword,
							String serverKeystorePath,
							String serverKeystorePassword) {
		
		this.initialize(platform, packages);
		try {
			//Set up all the cryptography..  The certificates are known in advance, and used
			// to authenticate client/server and establish TLS encrypted connections.
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextInt();
			
			KeyStore clientKeyStore = KeyStore.getInstance("JKS");
			clientKeyStore.load(new FileInputStream(clientKeystorePath), clientKeystorePassword.toCharArray());
			
			KeyStore serverKeyStore = KeyStore.getInstance("JKS");
			serverKeyStore.load(new FileInputStream(serverKeystorePath), serverKeystorePassword.toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(clientKeyStore);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(serverKeyStore, serverKeystorePassword.toCharArray());
			
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
			
			//Now that we're set up for SS:, create the listener socket.
			SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
			
			SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(port);
			serverSocket.setNeedClientAuth(true);
			this.serverSocket = serverSocket;
			
			System.out.println("Listening on port " + String.valueOf(port));
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the TransactionServer instance with a reference to the 
	 * platform and a list of packages to scan for routing annotations.
	 * 
	 * @param platform
	 * @param packages
	 */
	private void initialize(Platform platform, String[] packages) {
		this.platform = platform;
		
		//Set up a transaction router for socket requests
		this.messageRouter = new ExoMessageRouter();
		for (String pkg : packages) {
			this.messageRouter.addPackage(pkg);
		}
	}

	/**
	 * Starts the server-side socket and spawns TransactionServerConnection threads when clients connect
	 */
	public void run() {
		if (this.serverSocket != null) {
			while (true) {
				try {
					Socket socket = this.serverSocket.accept();
					new TransactionServerConnection(socket, this.platform, this.messageRouter).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	}
}
