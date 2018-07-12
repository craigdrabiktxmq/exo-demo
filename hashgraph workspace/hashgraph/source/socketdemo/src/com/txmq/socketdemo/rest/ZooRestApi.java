package com.txmq.socketdemo.rest;

import java.io.IOException;
import java.io.Serializable;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.ReportingEvents;
import com.txmq.exo.pipeline.subscribers.ExoSubscriberManager;
import com.txmq.socketdemo.ZooDemoTransactionTypes;

import io.swagger.model.Animal;

@Path("/HashgraphZoo/1.0.0")
public class ZooRestApi {
	private ExoSubscriberManager subscriberManager = new ExoSubscriberManager();
	
	@GET
	@Path("/zoo")
	@Produces(MediaType.APPLICATION_JSON)
	public void getZoo(@Suspended final AsyncResponse response) {
		ExoMessage<Serializable> message = new ExoMessage<Serializable>(
				new ZooDemoTransactionTypes(ZooDemoTransactionTypes.GET_ZOO),
				null
		);
		
		this.subscriberManager.registerResponder(message, ReportingEvents.transactionComplete, response);
		try {
			message.submit();
		} catch (IOException e) {
			response.resume(Response.serverError().entity(e).build());
		}
	}
	
	@POST
	@Path("/zoo/animals")
	@Produces(MediaType.APPLICATION_JSON)
	public void addAnimal(Animal animal, @Suspended final AsyncResponse response) {
		ExoMessage<Animal> message = new ExoMessage<Animal>(new ZooDemoTransactionTypes(ZooDemoTransactionTypes.ADD_ANIMAL), animal);
		this.subscriberManager.registerResponder(message, ReportingEvents.transactionComplete, response);
		try {
			message.submit();
		} catch (Exception e) {
			response.resume(Response.serverError().entity(e).build());
		}
	}
}
