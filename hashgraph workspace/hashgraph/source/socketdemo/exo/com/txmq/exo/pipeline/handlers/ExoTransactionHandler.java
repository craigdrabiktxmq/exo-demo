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
public interface ExoTransactionHandler<T extends Serializable, U extends Serializable, V extends ExoState> {

	/**
	 * Invoked when a transaction is ready to be processed pre-consensus, e.g. 
	 * SwirldState.handleTransaction() has been invoked with the consensus 
	 * flag set to false.
	 * 
	 * @param message
	 * @param state
	 */
	public void onExecutePreConsensus(ExoMessage<T, U> message, V state);
	
	/**
	 * Invoked when a transaction is ready to be processed pre-consensus, e.g. 
	 * SwirldState.handleTransaction() has been invoked with the consensus 
	 * flag set to false.
	 * 
	 * @param message
	 * @param state
	 */
	public void onExecuteConsensus(ExoMessage<T, U> message, V state);
}
