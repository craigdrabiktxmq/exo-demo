package com.txmq.exo.messaging.socket;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.transactionrouter.ExoRouter;

public class ExoMessageRouter extends ExoRouter<ExoMessageHandler> {
	public Object routeMessage(ExoMessage<?> message, ExoState state) throws ReflectiveOperationException {
		return this.invokeHandler(message.transactionType.getValue(), message, state);
	}
}
