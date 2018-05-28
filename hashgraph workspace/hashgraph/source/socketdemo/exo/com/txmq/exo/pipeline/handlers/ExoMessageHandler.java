package com.txmq.exo.pipeline.handlers;

import java.io.Serializable;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;

/**
 * Interface that application message handlers must implement.  Typically, message 
 * handlers are used to read data from the state and return it to client applications.  
 * Message handlers can also be used to perform calculations, acquire external data or 
 * implement authorization logic before Swirlds transactions are created.
 * 
 * @author craigdrabik
 *
 */
public interface ExoMessageHandler<T extends Serializable, U extends ExoState> {

	/**
	 * This method is invoked in response to (messageReceived) events.  Handlers for
	 * operations that only return data to the client and do not modify state should
	 * interrupt the transaction before returning.
	 * 
	 * Developers may return a Serializable object containing results or other 
	 * information about the request that may be relevant to subscribers
	 * 
	 * @param message
	 * @param state
	 */
	public Serializable onMessageReceived(ExoMessage<T> message, U state);
	
}
