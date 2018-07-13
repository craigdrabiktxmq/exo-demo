package com.txmq.exo.pipeline.subscribers;

import org.glassfish.grizzly.websockets.WebSocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.txmq.exo.messaging.ExoNotification;
import com.txmq.exo.pipeline.PipelineStatus;

public class ExoWebSocketSubscriber extends ExoSubscriberBase<WebSocket> {

	protected void sendNotification(ExoNotification<?> notification) {
		WebSocket ws = this.getResponder(notification);
		if (ws == null) {
			return;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String message = null;
		try {
			message = mapper.writeValueAsString(notification);
		} catch (JsonProcessingException e) {
			ExoNotification<String> err = new ExoNotification<String>();
			err.event = notification.event;
			err.status = PipelineStatus.ERROR;
			err.transactionType = notification.transactionType;
			err.triggeringMessage = notification.triggeringMessage;
			err.payload = "An error occurred trying to serialize a notification to JSON";
			
			try { 
				message = mapper.writeValueAsString(err);
			} catch (JsonProcessingException e2) {
				//We should really never get here, and if we do a developer 
				//has probably noticed they aren't getting responses
				e.printStackTrace();
			}
		} finally {
			if (message != null) {
				ws.send(message);
			}
		}
	}
}
