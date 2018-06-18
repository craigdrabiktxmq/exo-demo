package com.txmq.exo.pipeline.subscribers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoNotification;
import com.txmq.exo.pipeline.ReportingEvents;

public class ExoSubscriberManager {

	private static Map<ReportingEvents, Map<UUID, Object>> responders;
	
	public ExoSubscriberManager() {
		if (responders == null) {
			responders = Collections.synchronizedMap(new HashMap<ReportingEvents, Map<UUID, Object>>());
			
			responders.put(	ReportingEvents.submitted, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));
			responders.put(	ReportingEvents.preConsensusResult, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));
			responders.put(	ReportingEvents.consensusResult, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));
			responders.put(	ReportingEvents.transactionComplete, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));			
		}
	}
	
	public synchronized void registerResponder(ExoMessage<?> message, ReportingEvents event, Object responderInstance) {
		
		responders.get(event).put(message.uuid, responderInstance);
	}
	
	public synchronized Object getResponder(ExoNotification<?> notification) {
		Map<UUID, Object> eventMap = responders.get(notification.event);
		if (eventMap.containsKey(notification.triggeringMessage.uuid)) {
			return eventMap.get(notification.triggeringMessage.uuid);
		} else {
			return null;
		}
	}
}
