package com.txmq.exo.messaging;

import java.io.Serializable;

import com.txmq.exo.pipeline.PipelineStatus;
import com.txmq.exo.pipeline.ReportingEvents;

public class ExoNotification<T extends Serializable> extends ExoMessage<T> {
	public ExoMessage<?> triggeringMessage;
	public ReportingEvents event;
	public PipelineStatus status;
	public String nodeName;
	
	public ExoNotification() {
		
	}
	
	public ExoNotification(ReportingEvents event, T payload, PipelineStatus status, ExoMessage<?> triggeringMessage, String nodeName) {
		this.event = event;
		this.payload = payload;
		this.status = status;
		this.transactionType = triggeringMessage.transactionType;
		this.triggeringMessage = triggeringMessage;
		this.nodeName = nodeName;
	}
}
