package com.txmq.exo.pipeline.routers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoNotification;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.pipeline.PipelineStatus;
import com.txmq.exo.pipeline.PlatformEvents;
import com.txmq.exo.pipeline.ReportingEvents;
import com.txmq.exo.pipeline.metadata.ExoHandler;
import com.txmq.exo.pipeline.metadata.ExoSubscriber;
import com.txmq.exo.pipeline.subscribers.ExoSubscriberManager;

public class ExoPipelineRouter {

	private ExoSubscriberManager subscriberManager = new ExoSubscriberManager();
	
	////	Routers for Platform Events 	////
	
	/**
	 *	Routes messages to methods annotated with @ExoHandler(PlatformEvents.messageReceived).
	 *	Used to route incoming messages to handlers that read data from state and return it to the client. 
	 */
	protected ExoParameterizedRouter<PlatformEvents> messageReceivedRouter = 
			new ExoParameterizedRouter<PlatformEvents>(ExoHandler.class, PlatformEvents.messageReceived);
	
	/**
	 *	Routes messages to methods annotated with @ExoHandler(PlatformEvents.executePreConsensus).
	 *	Used to route incoming messages to handlers that perform validation and processing pre-consensus. 
	 */
	protected ExoParameterizedRouter<PlatformEvents> executePreConsensusRouter = 
			new ExoParameterizedRouter<PlatformEvents>(ExoHandler.class, PlatformEvents.executePreConsensus);
	
	/**
	 *	Routes messages to methods annotated with @ExoHandler(PlatformEvents.executeConsensus).
	 *	Used to route incoming messages to handlers that perform validation and processing at consensus. 
	 */
	protected ExoParameterizedRouter<PlatformEvents> executeConsensusRouter = 
			new ExoParameterizedRouter<PlatformEvents>(ExoHandler.class, PlatformEvents.executeConsensus);
	
	////	Routers for Reporting Events	////
	
	/**
	 * Routes notifications to methods annotated with @ExoSubscriber(ReportingEvents.submitted).
	 * Used to notify clients that a transaction has been submitted to the platform.
	 */
	protected ExoParameterizedRouter<ReportingEvents> submittedRouter = 
			new ExoParameterizedRouter<ReportingEvents>(ExoSubscriber.class, ReportingEvents.submitted);
	
	/**
	 * Routes notifications to methods annotated with @ExoSubscriber(ReportingEvents.preConsensusResult).
	 * Used to notify clients that processing has occurred pre-consensus.  
	 */
	protected ExoParameterizedRouter<ReportingEvents> preConsensusResultRouter = 
			new ExoParameterizedRouter<ReportingEvents>(ExoSubscriber.class, ReportingEvents.preConsensusResult);
	
	/**
	 * Routes notifications to methods annotated with @ExoSubscriber(ReportingEvents.submitted).
	 * Used to notify clients that processing has occurred at consensus.
	 */
	protected ExoParameterizedRouter<ReportingEvents> consensusResultRouter = 
			new ExoParameterizedRouter<ReportingEvents>(ExoSubscriber.class, ReportingEvents.consensusResult);
	
	/**
	 * Routes notifications to methods annotated with @ExoSubscriber(ReportingEvents.transactionComplete).
	 * Used to notify clients that transaction processing has completed.
	 */
	protected ExoParameterizedRouter<ReportingEvents> transactionCompletedRouter = 
			new ExoParameterizedRouter<ReportingEvents>(ExoSubscriber.class, ReportingEvents.transactionComplete);
	
	
	
	public void init(String[] packages) {
		for (String pkg : packages ) {
			this.messageReceivedRouter.addPackage(pkg);
			this.executePreConsensusRouter.addPackage(pkg);
			this.executeConsensusRouter.addPackage(pkg);
			this.submittedRouter.addPackage(pkg);
			this.preConsensusResultRouter.addPackage(pkg);
			this.consensusResultRouter.addPackage(pkg);
			this.transactionCompletedRouter.addPackage(pkg);
		}		
	}
	
	public List<ReportingEvents> getRegisteredNotificationsForTransactionType(ExoTransactionType transactionType) {
		List<ReportingEvents> registeredEvents = new ArrayList<ReportingEvents>();
		
		if (this.submittedRouter.hasRouteForTransactionType(transactionType)) {
			registeredEvents.add(ReportingEvents.submitted);			
		}
		
		if (this.preConsensusResultRouter.hasRouteForTransactionType(transactionType)) {
			registeredEvents.add(ReportingEvents.preConsensusResult);			
		}
		
		if (this.consensusResultRouter.hasRouteForTransactionType(transactionType)) {
			registeredEvents.add(ReportingEvents.consensusResult);			
		}
		
		if (this.transactionCompletedRouter.hasRouteForTransactionType(transactionType)) {
			registeredEvents.add(ReportingEvents.transactionComplete);			
		}
		
		return registeredEvents;
	}
	
	public void routeMessageReceived(ExoMessage<?> message, ExoState state) {
		//System.out.println("Routing " + message.uuid + " to messageReceived");
		try {
			Serializable result = this.route(message, state, this.messageReceivedRouter);
			if (message.isInterrupted()) {
				this.sendNotification(ReportingEvents.transactionComplete, result, message, PipelineStatus.INTERRUPTED, state.getMyName());
			} 
		} catch (ExoRoutingException e) {
			/*
			 * Indicates that something happened during processing that should prevent 
			 * the normal notification handler from running.  We don't need to handle 
			 * it directly, it's purpose is simply to short circuit notification.
			 */
		}
	}
	
	public void routeExecutePreConsensus(ExoMessage<?> message, ExoState state) throws ReflectiveOperationException {
		//System.out.println("Routing " + message.uuid + " to executePreConsensus");
		try {
			Serializable result = this.route(message, state, this.executePreConsensusRouter);
			this.sendNotification(	ReportingEvents.preConsensusResult, 
									result, 
									message, 
									(message.isInterrupted()) ? PipelineStatus.INTERRUPTED : PipelineStatus.OK,
									state.getMyName());
			if (message.isInterrupted()) {
				this.sendNotification(ReportingEvents.transactionComplete, result, message, PipelineStatus.INTERRUPTED, state.getMyName());
			} 
		} catch (ExoRoutingException e) {
			/*
			 * Indicates that something happened during processing that should prevent 
			 * the normal notification handler from running.  We don't need to handle 
			 * it directly, it's purpose is simply to short circuit notification.
			 */
		}
	}
	
	public void routeExecuteConsensus(ExoMessage<?> message, ExoState state) throws ReflectiveOperationException {
		System.out.println("Routing " + message.uuid + " to executeConsensus on " + state.getMyName());
		try {
			Serializable result = this.route(message, state, this.executeConsensusRouter);
			this.sendNotification(	ReportingEvents.consensusResult, 
					result, 
					message, 
					(message.isInterrupted()) ? PipelineStatus.INTERRUPTED : PipelineStatus.OK,
					state.getMyName());
			this.sendNotification(ReportingEvents.transactionComplete, result, message, PipelineStatus.COMPLETED, state.getMyName());
		} catch (ExoRoutingException e) {
			/*
			 * Indicates that something happened during processing that should prevent 
			 * the normal notification handler from running.  We don't need to handle 
			 * it directly, it's purpose is simply to short circuit notification.
			 */
		}		
	}
	
	public void notifySubmitted(ExoMessage<?> message) {
		this.sendNotification(ReportingEvents.submitted, null, message, PipelineStatus.OK, ExoPlatformLocator.getState().getMyName());
	}
	
	private Serializable route(ExoMessage<?> message, ExoState state, ExoParameterizedRouter<?> router) throws ExoRoutingException {
		Serializable result = null;
		try {
			result = router.routeTransaction(message, state);
			//If the transaction was interrupted, notify transactionComplete handlers
		} catch (Exception e) {
			/* 
			 * Something has gone wrong that was unhandled.  Interrupt processing and return the
			 * error in a transactionCompelete notification 
			 */
			
			message.interrupt();
			this.sendNotification(	ReportingEvents.transactionComplete, 
									e, 
									message,
									PipelineStatus.ERROR,
									state.getMyName());
			
			throw new ExoRoutingException();
		}	
		
		return result;
	}
	
	private void sendNotification(	ReportingEvents event, 
									Serializable payload, 
									ExoMessage<?> triggeringMessage, 
									PipelineStatus status,
									String nodeName) {
		this.sendNotification(	new ExoNotification<Serializable>(	event, 
																	payload, 
																	status, 
																	triggeringMessage,
																	nodeName)
		);		
	}
	
	private void sendNotification(ExoNotification<?> notification) {
		//System.out.println("Routing " + notification.triggeringMessage.uuid + " to " + notification.event.toString());
		
		ExoParameterizedRouter<ReportingEvents> router = null;
		switch (notification.event) {
			case submitted:
				router = this.submittedRouter;
				break;
			case preConsensusResult:
				router = this.preConsensusResultRouter;
				break;
			case consensusResult:
				router = this.consensusResultRouter;
				break;
			case transactionComplete:
				router = this.transactionCompletedRouter;
				break;				
		}
		
		try {
			router.routeTransaction(notification, null);
		} catch (Exception e) {
			/* 
			 * Something has gone wrong that was unhandled while sending a notification.  
			 * In this case, we don't want to interrupt the further processing of the transaction
			 */
			
			e.printStackTrace();
		} finally {
			//Clean up responders if this is the last step in the pipeline
			if (notification.event.equals(ReportingEvents.transactionComplete)) {
				this.subscriberManager.removeResponder(notification);
			}
		}
	}
}
