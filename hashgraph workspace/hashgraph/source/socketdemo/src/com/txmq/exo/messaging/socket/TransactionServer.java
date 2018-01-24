package com.txmq.exo.messaging.socket;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

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
	private SSLServerSocket serverSocket;
	
	public TransactionServer(Platform platform, int port) {
		this.platform = platform;
		try {
			//Set up all the cryptography..  The certificates are known in advance, and used
			// to authenticate client/server and establish TLS encrypted connections.
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextInt();
			
			KeyStore clientKeyStore = KeyStore.getInstance("JKS");
			clientKeyStore.load(new FileInputStream("client.public"), "client".toCharArray());
			
			KeyStore serverKeyStore = KeyStore.getInstance("JKS");
			serverKeyStore.load(new FileInputStream("server.private"), "server".toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(clientKeyStore);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(serverKeyStore, "server".toCharArray());
			
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
			
			//Now that we're set up for SS:, create the listener socket.
			SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
			this.serverSocket = (SSLServerSocket) socketFactory.createServerSocket(port);  //new Socket(HOST, PORT);
			this.serverSocket.setNeedClientAuth(true);
			System.out.println("Listening on port " + String.valueOf(port));
					
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
					new TransactionServerConnection(socket, this.platform).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	}
}
