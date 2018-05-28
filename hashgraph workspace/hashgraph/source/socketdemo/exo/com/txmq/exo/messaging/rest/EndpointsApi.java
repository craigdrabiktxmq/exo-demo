package com.txmq.exo.messaging.rest;

import java.io.Serializable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;

/**
 * This class implements a REST endpoint for retrieving a list of endpoints that the Swirld
 * exposes.  Endpoints self-report by issuing an ExoMessage, which is logged in state.
 * 
 */
@Path("/exo/0.2.0") //TODO:  Remove HashgraphZoo prefix, give the internal APIs their own
public class EndpointsApi {
	@GET
	@Path("/endpoints")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEndpoints() {
		ExoState state = (ExoState) ExoPlatformLocator.getPlatform().getState();
		return Response.ok().entity(state.getEndpoints()).build();
	}
	
	@GET
	@Path("/shutdown")
	public Response shutdown() {
		
		ExoMessage<Serializable> transaction = 
				new ExoMessage<Serializable>(new ExoTransactionType(ExoTransactionType.SHUTDOWN));
		try {
			ExoPlatformLocator.createTransaction(transaction);
		} catch (Exception e) {
			return Response.serverError().entity(e).build();
		}
		
		return Response.ok().build();
	}
}
