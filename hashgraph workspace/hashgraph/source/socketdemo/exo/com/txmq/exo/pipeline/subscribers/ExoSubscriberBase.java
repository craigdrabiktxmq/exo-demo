package com.txmq.exo.pipeline.subscribers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.txmq.exo.messaging.ExoNotification;

public class ExoSubscriberBase<T> {
	
	protected ExoSubscriberManager subscriberManager = new ExoSubscriberManager();
	
	private Class<?> subscriberType = null;
	
	public ExoSubscriberBase() {
		Class<?> clazz = getClass();
		ParameterizedType parameterizedType = null;
		
		while (clazz != null && parameterizedType == null) {
			Type superclassType = clazz.getGenericSuperclass();
			if (superclassType instanceof ParameterizedType) {
				parameterizedType = (ParameterizedType) superclassType;
			} else {
				clazz = clazz.getSuperclass();
			}
		}
		
		if (parameterizedType != null) {
			this.subscriberType = ((Class<?>) parameterizedType.getActualTypeArguments()[0]);
		} else {
			throw new IllegalArgumentException("This should not be possible");
		}
	}
	
	@SuppressWarnings("unchecked")
	protected T getResponder(ExoNotification<?> notification) {
		Object responder = this.subscriberManager.getResponder(notification);
		/*
		 * Test if the responder we received is the type we expect.  If not, return null.
		 * This is used to cover off the situation where there are REST and WebSocket subscribers
		 * for the same event/transaction type.  We might ask for a subscriber and get one of the 
		 * wrong type.
		 * 
		 * TODO:  Might be able to optimize this a bit by "typing" the responder map.
		 */
		if (responder != null && this.subscriberType.isAssignableFrom(responder.getClass())) {
			return (T) responder;
		} else {
			return null;
		}
	}
}
