package com.txmq.exo.pipeline.subscribers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoNotification;
import com.txmq.exo.pipeline.ReportingEvents;

public class ExoSubscriberManager {

	private static Map<String, Map<ReportingEvents, Map<UUID, Object>>> responders;
	private static Map<Object, List<ResponderLookup>> responderLookups;
	
	//TODO:  Should allow for more than one subscriber per message, per event?
	public ExoSubscriberManager() {
		if (responders == null) {
			responders = Collections.synchronizedMap(new HashMap<String, Map<ReportingEvents, Map<UUID, Object>>>());
			
			/*
			responders.put(	ReportingEvents.submitted, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));
			responders.put(	ReportingEvents.preConsensusResult, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));
			responders.put(	ReportingEvents.consensusResult, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));
			responders.put(	ReportingEvents.transactionComplete, 
							Collections.synchronizedMap(new HashMap<UUID, Object>()));
							*/			
		}
		
		if (responderLookups == null) {
			responderLookups = new MultivaluedHashMap<Object, ResponderLookup>();
		}
	}
	
	private Map<ReportingEvents, Map<UUID, Object>> getRespondersForNode(String nodeName) {
		if (nodeName == null) {
			nodeName = ExoPlatformLocator.getState().getMyName();
		}
		
		if (!responders.containsKey(nodeName)) {
			Map<ReportingEvents, Map<UUID, Object>> nodeMap = Collections.synchronizedMap(
					new HashMap<ReportingEvents, Map<UUID, Object>> ()
			);
			
			nodeMap.put(ReportingEvents.submitted, 
						Collections.synchronizedMap(new HashMap<UUID, Object>()));
			nodeMap.put(ReportingEvents.preConsensusResult, 
						Collections.synchronizedMap(new HashMap<UUID, Object>()));
			nodeMap.put(ReportingEvents.consensusResult, 
						Collections.synchronizedMap(new HashMap<UUID, Object>()));
			nodeMap.put(ReportingEvents.transactionComplete, 
						Collections.synchronizedMap(new HashMap<UUID, Object>()));
			
			responders.put(nodeName, nodeMap);
		}
		
		return responders.get(nodeName);
	}
	
	public synchronized void registerResponder(ExoMessage<?> message, ReportingEvents event, Object responderInstance) {	
		String myName = ExoPlatformLocator.getState().getMyName();
		getRespondersForNode(myName).get(event).put(message.uuid, responderInstance);
		
		if (!responderLookups.containsKey(responderInstance)) {
			responderLookups.put(responderInstance, new ArrayList<ResponderLookup>());
		}
		
		responderLookups.get(responderInstance).add(new ResponderLookup(myName, event, message.uuid));
	}
	
	public synchronized void registerAllAvailableResponders(ExoMessage<?> message, Object responderInstance) {
		List<ReportingEvents> events = ExoPlatformLocator
				.getPipelineRouter()
				.getRegisteredNotificationsForTransactionType(message.transactionType);
		
		for (ReportingEvents event : events) {
			this.registerResponder(message, event, responderInstance);
		}
		
	}
	
	public synchronized Object getResponder(ExoNotification<?> notification) {
		Map<UUID, Object> eventMap = getRespondersForNode(notification.nodeName).get(notification.event);
		if (eventMap.containsKey(notification.triggeringMessage.uuid)) {
			return eventMap.get(notification.triggeringMessage.uuid);
		} else {
			return null;
		}
	}
	
	public synchronized void removeResponder(ExoNotification<?> notification) {
		Map<UUID, Object> eventMap = getRespondersForNode(notification.nodeName).get(notification.event);
		eventMap.remove(notification.triggeringMessage.uuid);
	}	
	
	public synchronized void removeResponder(Object responder) {
		if (responderLookups.containsKey(responder)) {
			for (ResponderLookup lookup : responderLookups.get(responder)) {
				if (getRespondersForNode(lookup.node).get(lookup.event).containsKey(lookup.notificationUUID)) {
					getRespondersForNode(lookup.node).get(lookup.event).remove(lookup.notificationUUID);
				}
			}
			
			responderLookups.remove(responder);
		}
	}
	
	private class ResponderLookup {
		public String node;
		public ReportingEvents event;
		public UUID notificationUUID;
		
		public ResponderLookup(String node, ReportingEvents event, UUID notificationUUID) {
			this.event = event;
			this.notificationUUID = notificationUUID;
		}
	}
}
