package com.txmq.exo.pipeline.handlers;

import java.io.Serializable;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;

/**
 * Interface that subscribers must implement.  Subscribers are typically used to 
 * report the results of transactions back to client applications.
 * 
 * @author craigdrabik
 *
 * @param <T> the type of the input parameter carrier class.  Must implement Serializable.
 * @param <U> the type of the output carrier class.  Must implement Serializable.
 * @param <V> the type of the application's state (SwirldState subclass).
 */
public interface ExoSubscriber<T extends Serializable, U extends ExoState> {
	
	/**
	 * Invoked in response to a transaction having been submitted to the 
	 * platform, e.g. Platform.createTransaction() has been invoked.
	 * 
	 * @param message
	 * @param state
	 */
	public void onSubmitted(ExoMessage<T> message, U state);
	
	/**
	 * Invoked after pre-consensus transaction processing has occurred, 
	 * e.g. SwirldState.handleTransaction() has been invoked with the
	 * consensus flag set to false.
	 * 
	 * @param message
	 * @param state
	 */
	public void onPreConsensusResult(ExoMessage<T> message, U state);
	
	/**
	 * Invoked after consensus transaction processing has occurred, 
	 * e.g. SwirldState.handleTransaction() has been invoked with the
	 * consensus flag set to true.
	 * 
	 * @param message
	 * @param state
	 */
	public void onConsensusResult(ExoMessage<T> message, U state);
	
	/**
	 * Invoked when a transaction has completed its journey through the pipeline.
	 * Listeners can use this as a catch-all when they only care about the final
	 * result of a transaction, instead of implementing the same logic in each 
	 * stage.  If a transaction is interrupted at any point, onTransactionComplete()
	 * will be invoked with the result of the handler that interrupted the transaction.
	 * @param message
	 * @param state
	 */
	public void onTransactionComplete(ExoMessage<T> message, U state);
	
}
