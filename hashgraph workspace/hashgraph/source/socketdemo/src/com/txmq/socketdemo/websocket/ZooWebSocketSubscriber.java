package com.txmq.socketdemo.websocket;

import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.messaging.ExoNotification;
import com.txmq.exo.pipeline.ReportingEvents;
import com.txmq.exo.pipeline.metadata.ExoSubscriber;
import com.txmq.exo.pipeline.subscribers.ExoWebSocketSubscriber;
import com.txmq.socketdemo.ZooDemoTransactionTypes;

/**
 * This class listens for events on zoo demo transactions and relays them back to clients through the web socket.
 * 
 * TODO:  Either through annotations or a simple JSON mapping file, there should be a way to configure this without having to write code.  
 * Code generation is a possibility, like I want to do with REST, but a config file or a section of exo-config.json might be a better approach.  
 * @author craigdrabik
 *
 */
public class ZooWebSocketSubscriber extends ExoWebSocketSubscriber {

	@ExoSubscriber(	transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
					events= {	ReportingEvents.submitted, 
								ReportingEvents.preConsensusResult, 
								ReportingEvents.consensusResult, 
								ReportingEvents.transactionComplete	})
	public void addAnimalTransactionProgress(ExoNotification<?> notification) {
		String myName = ExoPlatformLocator.getState().getMyName();
		System.out.println("Sending notification from " + myName);
		this.sendNotification(notification);
	}
	
	@ExoSubscriber(	transactionType=ZooDemoTransactionTypes.GET_ZOO, events={ReportingEvents.transactionComplete})
	public void getZooTransactionProgress(ExoNotification<?> notification) {
		String myName = ExoPlatformLocator.getState().getMyName();
		System.out.println("Sending notification from " + myName);
		this.sendNotification(notification);
	}


}
