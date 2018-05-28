package com.txmq.exo.pipeline.handlers;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;

/**
 * Interface that application transaction handlers must implement.  Transaction
 * handlers are used to process application logic and update the SwirldState.
 * 
 * @author craigdrabik
 *
 */
public interface ExoTransactionHandler<T extends ExoMessage, U extends ExoState> {

	/**
	 * Invoked when a transaction is ready to be processed pre-consensus, e.g. 
	 * SwirldState.handleTransaction() has been invoked with the consensus 
	 * flag set to false.
	 * 
	 * @param message
	 * @param state
	 */
	public void onExecutePreConsensus(T message, U state);
	
	/**
	 * Invoked when a transaction is ready to be processed pre-consensus, e.g. 
	 * SwirldState.handleTransaction() has been invoked with the consensus 
	 * flag set to false.
	 * 
	 * @param message
	 * @param state
	 */
	public void onExecuteConsensus(T message, U state);
}
