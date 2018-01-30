package com.txmq.exo.messaging;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.txmq.exo.config.ClientConfig;
import com.txmq.exo.config.ExoConfig;
import com.txmq.exo.messaging.routing.*;
import com.txmq.socketdemo.SocketDemoTransactionTypes;

public class SwirldsAdaptor {
	
	private static ExoConfig config;
	private static INodeRouter nodeRouter;
	
	private Socket socket;
	
	public SwirldsAdaptor() {
		//Set up the list of available nodes
		
		if (SwirldsAdaptor.nodeRouter == null) {
			SwirldsAdaptor.nodeRouter = new FixedNodeRouter();
			SwirldsAdaptor.nodeRouter.setAvailableNodes(ExoConfig.getConfig().clientConfig.getKnownSockets());
		}
		
		if (ExoConfig.getConfig().clientConfig.encrypted == true) {
			this.setupSecuredSocket();
		} else {
			this.setupUnsecuredSocket();
		}
			
	}
	
	private void setupUnsecuredSocket() {
		InetSocketAddress destination = (InetSocketAddress) SwirldsAdaptor.nodeRouter.getNode();
		try {
			this.socket = new Socket(destination.getHostName(), destination.getPort());
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("An error occurred while configuring an unsecured socker: " + e.getMessage());
		}
	}
	
	private void setupSecuredSocket() {		
		ClientConfig config = ExoConfig.getConfig().clientConfig;
		
		try {
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextInt();
			
			KeyStore clientKeyStore = KeyStore.getInstance("JKS");
			clientKeyStore.load(new FileInputStream(config.clientKeystore.path), config.clientKeystore.password.toCharArray());
			
			KeyStore serverKeyStore = KeyStore.getInstance("JKS");
			serverKeyStore.load(new FileInputStream(config.serverKeystore.path), config.serverKeystore.password.toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(serverKeyStore);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(clientKeyStore, "client".toCharArray());
			
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
			
			SSLSocketFactory socketFactory = sslContext.getSocketFactory();
			InetSocketAddress destination = (InetSocketAddress) SwirldsAdaptor.nodeRouter.getNode();

			System.out.println("Attempting to create a connection to " + destination.getHostName() + ":" + destination.getPort());
			this.socket = socketFactory.createSocket(destination.getHostName(), destination.getPort());  //new Socket(HOST, PORT);
		} catch (	IOException | 
					KeyStoreException | 
					NoSuchAlgorithmException | 
					CertificateException | 
					UnrecoverableKeyException | 
					KeyManagementException e) {
			e.printStackTrace();
			throw new IllegalStateException("An error has occurred while configuring a secured socket:  " + e.getMessage());
		}
	}
	
	public ExoMessage sendTransaction(SocketDemoTransactionTypes transactionType, Serializable payload) {
		if (this.socket == null) {
			throw new IllegalStateException("A socket has not been created");
		}
		
		//Reconnect if we're not currently connected
		if (!this.socket.isConnected()) {
			try {
				this.socket.connect(SwirldsAdaptor.nodeRouter.getNode());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();		
			}
		}
		
		try {
			//PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
			ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream writer = new ObjectOutputStream(this.socket.getOutputStream());
			ExoMessage message = new ExoMessage();
			
			message.transactionType = transactionType;
			message.payload = payload;
			writer.writeObject(message);
			writer.flush();
			
			ExoMessage response = (ExoMessage) reader.readObject();
			return response;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();				
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}