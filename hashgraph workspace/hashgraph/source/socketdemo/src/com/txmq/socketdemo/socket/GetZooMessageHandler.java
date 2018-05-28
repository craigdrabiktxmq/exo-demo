package com.txmq.socketdemo.socket;

import java.io.Serializable;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.handlers.ExoMessageHandler;
import com.txmq.socketdemo.SocketDemoState;

import io.swagger.model.Zoo;

//TODO:  Add metadata to identify this transaction type this handler responds to
public class GetZooMessageHandler implements ExoMessageHandler<Serializable, Zoo, SocketDemoState> {

	@Override
	public void onMessageReceived(ExoMessage<Serializable, Zoo> message, SocketDemoState state) {
		message.result = new Zoo();
		message.result.lions(state.getLions());
		message.result.tigers(state.getTigers());
		message.result.bears(state.getBears());		
	}

}
