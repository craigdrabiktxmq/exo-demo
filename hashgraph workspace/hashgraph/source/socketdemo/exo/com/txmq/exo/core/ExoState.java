package com.txmq.exo.core;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.messaging.ExoMessage;

/**
 * ExoState is a base class for developers to extend when implementing Swirlds states.
 * ExoState encapsulates persisting the address book, enables the collection of data
 * about available endpoints (exposed by the endpoints API), and contains the hooks
 * for routing transactions using annotations.
 * 
 * Developers should be sure to call super-methods when extending/implementing init, 
 * copyFrom, and handleTransaction when subclassing ExoState.
 * 
 * @see com.txmq.exo.messaging.rest.EndpointsApi
 */
public class ExoState {
	
	/** names and addresses of all members */
	protected AddressBook addressBook;
	
	/**
	 * Node name that this state belongs to.  Tracked for disambiguation 
	 * purposes in the block logger.  Suspicion is that this won't be needed
	 * for nodes which are launched on separate machines.
	 */
	protected String myName;
	
	public String getMyName() {
		return this.myName;
	}
	
	/**
	 * List of endpoints reported through the Endpoints API
	 */
	private List<String> endpoints = Collections.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getEndpoints() {
		return endpoints;
	}
	
	/**
	 * Public accessor method used by the endpoints API to add available endpoints to the state.
	 */
	public synchronized void addEndpoint(String endpoint) {
		this.endpoints.add(endpoint);
	}
	
	/**
	 * Base implementation of copyFrom.  Copies endpoints, addressBook, 
	 * and node naming information stored in the state.
	 */
	public synchronized void copyFrom(SwirldState old) {
		endpoints = Collections.synchronizedList(new ArrayList<String>(((ExoState) old).endpoints));
		if (addressBook != null) {
			addressBook = ((ExoState) old).addressBook.copy();
		}
		myName = ((ExoState) old).myName;
	}
	
	/**
	 * Base implementation of transaction handler for Swirlds states.  The 
	 * platform will invoke this method once per transaction as transactions 
	 * are received, and again once consensus has been reached.
	 * 
	 * The base implementation routes events using Exo's annotation model.
	 * When transactions are received, they are routed to methods that 
	 * have been annotated with @ExoTransaction(<transaction type>).  
	 * 
	 * Transactions that have reached consensus are logged to a BlockLogger automatically.
	 * 
	 * TODO:  Make blockchain logging configurable
	 */
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timeCreated, byte[] transaction, Address address) {
		
		try {
			ExoMessage<?> message = ExoMessage.deserialize(transaction);
			if (consensus == false) {
				//Route the transaction through the pre-consensus part of the pipeline
				ExoPlatformLocator.getPipelineRouter(this.myName).routeExecutePreConsensus(message, this);				
			} else {
				ExoPlatformLocator.getPipelineRouter(this.myName).routeExecuteConsensus(message, this);
				if (message.isInterrupted() == false) {
					ExoPlatformLocator.getBlockLogger().addTransaction(message, this.myName);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Initializer method.  This gets called by the platform when it creates a
	 * copy of the state.  When extending ExoState, be sure to call super.init()
	 * when overriding/implementing the init method.
	 */
	public synchronized void init(Platform platform, AddressBook addressBook) {
		this.myName = platform.getAddress().getSelfName();
		this.addressBook = addressBook;
	}

}
