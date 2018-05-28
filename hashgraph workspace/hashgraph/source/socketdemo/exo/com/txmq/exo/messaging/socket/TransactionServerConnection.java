package com.txmq.exo.messaging.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import com.swirlds.platform.Platform;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoMessage;

/**
 * TransactionServerConnection represents the server-side of an established connection.
 * It runs on its own thread and accepts ExoMessages from the socket.
 */
public class TransactionServerConnection extends Thread {

	private Socket socket;
	private Platform platform;
	private ExoMessageRouter messageRouter;
	
	public TransactionServerConnection(Socket socket, Platform platform, ExoMessageRouter messageRouter) {
		this.socket = socket;
		this.platform = platform;
		this.messageRouter = messageRouter;
	}
	
	/**
	 * Accepts transactions in ExoMessage instances from the socket and process them.
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		try {
			//Set up streams for reading from and writing to the socket.
			ObjectOutputStream writer = new ObjectOutputStream(this.socket.getOutputStream());
			ObjectInputStream reader = new ObjectInputStream(socket.getInputStream());
			ExoMessage<?> message;
			ExoMessage<Serializable> response = new ExoMessage<Serializable>();
			try {
				//Read the message object and try to cast it to ExoMessage
				Object tmp = reader.readObject();
				message = (ExoMessage<?>) tmp; 
				ExoState state = (ExoState) this.platform.getState();
				
				try {
					response = (ExoMessage<Serializable>) this.messageRouter.routeMessage(message, state);
				} catch (IllegalArgumentException e) {
					/*
					 * This exception is thrown by transactionRouter when it can't figure 
					 * out where to route a message.  In the case of socket transactions, 
					 * those transaction types it can't route are messages that we can 
					 * simply pass through to the platform for processing by the Hashgraph,
					 * unless it's an ACKNOWLEDGE transaction.
					 */
					if (message.transactionType.getValue() == ExoTransactionType.ACKNOWLEDGE) {
						//We shouldn't receive this from the client.  If we do, just send it back
						response.transactionType.setValue(ExoTransactionType.ACKNOWLEDGE);
					} else {	
						ExoPlatformLocator.createTransaction(message);
						response.transactionType.setValue(ExoTransactionType.ACKNOWLEDGE);
					}
				} catch (ReflectiveOperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//write the response to the socket
				writer.writeObject(response);					
				writer.flush();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Closing Socket");
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
