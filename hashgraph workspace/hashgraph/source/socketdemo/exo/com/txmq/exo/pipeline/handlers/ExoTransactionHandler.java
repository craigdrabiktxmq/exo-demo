package com.txmq.exo.pipeline.handlers;

import java.io.Serializable;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;

/**
 * Interface that application transaction handlers must implement.  Transaction
 * handlers are used to process application logic and update the SwirldState.
 * 
 * @param <T> the type of the input parameter carrier class.  Must implement Serializable.
 * @param <U> the type of the output carrier class.  Must implement Serializable.
 * @param <V> the type of the application's state (SwirldState subclass).
 * 
 * @author craigdrabik
 *
 */
public interface ExoTransactionHandler<T extends Serializable, U extends ExoState> {

	/**
	 * Invoked when a transaction is ready to be processed pre-consensus, e.g. 
	 * SwirldState.handleTransaction() has been invoked with the consensus 
	 * flag set to false.
	 * 
	 * Developers may return a Serializable object containing results or other 
	 * information about the request that may be relevant to subscribers
	 * 
	 * @param message
	 * @param state
	 */
	public Serializable onExecutePreConsensus(ExoMessage<T> message, U state);
	
	/**
	 * Invoked when a transaction is ready to be processed pre-consensus, e.g. 
	 * SwirldState.handleTransaction() has been invoked with the consensus 
	 * flag set to false.
	 *
 	 * Developers may return a Serializable object containing results or other 
	 * information about the request that may be relevant to subscribers
	 * 
	 * @param message
	 * @param state
	 */
	public Serializable onExecuteConsensus(ExoMessage<T> message, U state);
}
