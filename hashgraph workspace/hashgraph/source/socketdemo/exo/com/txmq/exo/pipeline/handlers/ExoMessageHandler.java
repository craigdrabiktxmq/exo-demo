package com.txmq.exo.pipeline.handlers;

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
public interface ExoMessageHandler<T extends ExoMessage, U extends ExoState> {

	/**
	 * This method is invoked in response to (messageReceived) events.  Handlers for
	 * operations that only return data to the client and do not modify state should
	 * interrupt the transaction before returning.
	 * 
	 * @param message
	 * @param state
	 */
	public void onMessageReceived(T message, U state);
	
}
