package com.txmq.exo.core;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.persistence.BlockLogger;
import com.txmq.socketdemo.SocketDemoState;

public class ExoState {
	/** names and addresses of all members */
	protected AddressBook addressBook;
	
	protected String myName;
	
	private List<String> endpoints = Collections.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getEndpoints() {
		return endpoints;
	}
	
	public synchronized void addEndpoint(String endpoint) {
		this.endpoints.add(endpoint);
	}
	
	public synchronized void copyFrom(SwirldState old) {
		endpoints = Collections.synchronizedList(new ArrayList<String>(((ExoState) old).endpoints));
		addressBook = ((SocketDemoState) old).addressBook.copy();
		myName = ((SocketDemoState) old).myName;
	}
	
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timeCreated, byte[] transaction, Address address) {
		if (consensus) {
			try {
				ExoMessage message = ExoMessage.deserialize(transaction);
				BlockLogger.addTransaction(message, this.myName);
				ExoPlatformLocator.getTransactionRouter().routeTransaction(message, this);				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public synchronized void init(Platform platform, AddressBook addressBook) {
		this.myName = platform.getAddress().getSelfName();
		this.addressBook = addressBook;
	}

}
