package com.txmq.exo.pipeline.subscribers;

import com.txmq.exo.messaging.ExoNotification;

public class ExoSubscriberBase<T> {
	protected ExoSubscriberManager subscriberManager = new ExoSubscriberManager();
	
	@SuppressWarnings("unchecked")
	protected T getResponder(ExoNotification<?> notification) {
		return (T) this.subscriberManager.getResponder(notification);
	}
}
