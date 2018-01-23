package com.txmq.exo.core;

import java.io.IOException;
import java.time.Instant;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.Platform;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.persistence.BlockLogger;

public class ExoState {
	/** names and addresses of all members */
	protected AddressBook addressBook;
	
	protected String myName;
	
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
