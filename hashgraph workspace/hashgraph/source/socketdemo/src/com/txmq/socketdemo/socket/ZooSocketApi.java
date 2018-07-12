package com.txmq.socketdemo.socket;

import java.io.Serializable;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.socket.ExoMessageHandler;
import com.txmq.socketdemo.SocketDemoState;
import com.txmq.socketdemo.ZooDemoTransactionTypes;

import io.swagger.model.Zoo;

public class ZooSocketApi {

	@ExoMessageHandler(ZooDemoTransactionTypes.GET_ZOO)
	public Serializable getZoo(ExoMessage<?> message, SocketDemoState state) {
		Zoo result = new Zoo();
		result.lions(state.getLions());
		result.tigers(state.getTigers());
		result.bears(state.getBears());
		
		return result;
		
	}
}
