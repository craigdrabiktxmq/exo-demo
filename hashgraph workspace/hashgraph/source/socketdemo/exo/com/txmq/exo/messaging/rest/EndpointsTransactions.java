package com.txmq.exo.messaging.rest;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.transactionrouter.ExoTransaction;
import com.txmq.exo.core.ExoState;

/**
 * Implements the Endpoints API announcement transaction.
 */
public class EndpointsTransactions {
	@ExoTransaction(ExoTransactionType.ANNOUNCE_NODE)
	public void announceNode(ExoMessage message, ExoState state, boolean consensus) {
		state.addEndpoint((String) message.payload);
	}
}
