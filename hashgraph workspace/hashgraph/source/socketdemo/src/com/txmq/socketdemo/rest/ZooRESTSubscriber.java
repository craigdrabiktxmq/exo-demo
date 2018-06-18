package com.txmq.socketdemo.rest;

import javax.ws.rs.container.AsyncResponse;

import com.txmq.exo.messaging.ExoNotification;
import com.txmq.exo.pipeline.ReportingEvents;
import com.txmq.exo.pipeline.metadata.ExoSubscriber;
import com.txmq.exo.pipeline.subscribers.ExoSubscriberBase;
import com.txmq.socketdemo.SocketDemoTransactionTypes;

public class ZooRESTSubscriber extends ExoSubscriberBase<AsyncResponse> {

	@ExoSubscriber(transactionType=SocketDemoTransactionTypes.ADD_ANIMAL, event=ReportingEvents.transactionComplete)
	public void addAnimalTransactionCompleted(ExoNotification<?> notification) {
		this.getResponder(notification).resume(notification.payload);
	}
	
	@ExoSubscriber(transactionType=SocketDemoTransactionTypes.GET_ZOO, event=ReportingEvents.transactionComplete)
	public void getZooTransactionCompleted(ExoNotification<?> notification) {
		this.getResponder(notification).resume(notification.payload);
	}
}
