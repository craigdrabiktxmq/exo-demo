package com.txmq.exo.messaging.rest;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.transactionrouter.ExoTransaction;
import com.txmq.socketdemo.SocketDemoState;

/**
 * Implements the Endpoints API announcement transaction.
 */
public class EndpointsTransactions {
	@ExoTransaction(ExoTransactionType.ANNOUNCE_NODE)
	public void announceNode(ExoMessage message, SocketDemoState state) { //TODO:  Refactor state so that Exo stuff is inheirited
		state.addEndpoint((String) message.payload);
	}
}
