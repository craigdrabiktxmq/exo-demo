package com.txmq.exo.messaging;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.txmq.socketdemo.SocketDemoTransactionTypes;

public class SwirldsAdaptor {
	//public static final String HOST = "0.0.0.0";
	public static final String HOST = "hashgraph";
	public static final int PORT = 51204;
	
	private Socket socket;
	public SwirldsAdaptor() {
		try {
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.nextInt();
			
			KeyStore clientKeyStore = KeyStore.getInstance("JKS");
			clientKeyStore.load(new FileInputStream("client.private"), "client".toCharArray());
			
			KeyStore serverKeyStore = KeyStore.getInstance("JKS");
			serverKeyStore.load(new FileInputStream("server.public"), "server".toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(serverKeyStore);
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(clientKeyStore, "client".toCharArray());
			
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
			
			SSLSocketFactory socketFactory = sslContext.getSocketFactory();
			this.socket = socketFactory.createSocket(HOST, PORT);  //new Socket(HOST, PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public SwirldsMessage sendTransaction(SocketDemoTransactionTypes transactionType, Serializable payload) {
		if (this.socket != null && this.socket.isConnected()) {
			try {
				//PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
				ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream writer = new ObjectOutputStream(this.socket.getOutputStream());
				SwirldsMessage message = new SwirldsMessage();
				
				message.transactionType = transactionType;
				message.payload = payload;
				writer.writeObject(message);
				writer.flush();
				
				SwirldsMessage response = (SwirldsMessage) reader.readObject();
				return response;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();				
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
}