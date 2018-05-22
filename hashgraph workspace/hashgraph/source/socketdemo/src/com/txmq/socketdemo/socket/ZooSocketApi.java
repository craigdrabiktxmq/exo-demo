package com.txmq.socketdemo.socket;

import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.transactionrouter.ExoTransaction;
import com.txmq.socketdemo.SocketDemoState;
import com.txmq.socketdemo.SocketDemoTransactionTypes;

import io.swagger.model.Zoo;

public class ZooSocketApi {

	@ExoTransaction(SocketDemoTransactionTypes.GET_ZOO)
	public ExoMessage getZoo(ExoMessage message, ExoState state, boolean consensus) {
		SocketDemoState _state = (SocketDemoState) ExoPlatformLocator.getState();
		Zoo result = new Zoo();
		result.lions(_state.getLions());
		result.tigers(_state.getTigers());
		result.bears(_state.getBears());
		
		return new ExoMessage(
			new ExoTransactionType(ExoTransactionType.ACKNOWLEDGE),
			result
		);
		
	}
}
