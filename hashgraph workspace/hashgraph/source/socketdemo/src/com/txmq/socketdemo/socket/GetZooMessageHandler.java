package com.txmq.socketdemo.socket;

import java.io.Serializable;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.handlers.ExoMessageHandler;
import com.txmq.socketdemo.SocketDemoState;

import io.swagger.model.Zoo;

//TODO:  Add metadata to identify this transaction type this handler responds to
public class GetZooMessageHandler implements ExoMessageHandler<Serializable, SocketDemoState> {

	@Override
	public Serializable onMessageReceived(ExoMessage<Serializable> message, SocketDemoState state) {
		Zoo result = new Zoo();
		result.lions(state.getLions());
		result.tigers(state.getTigers());
		result.bears(state.getBears());
		
		return result;
	}
}
