package com.txmq.exo.messaging.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.intiva.intivahealth.IntivaHealthState;
import com.txmq.exo.core.ExoPlatformLocator;

/**
 * This class implements a REST endpoint for retrieving a list of endpoints that the Swirld
 * exposes.  Endpoints self-report by issuing an ExoMessage, which is logged in state.
 * 
 */
@Path("/HashgraphZoo/1.0.0") //TODO:  Remove HashgraphZoo prefix, give the internal APIs their own
public class EndpointsApi {
	@GET
	@Path("/endpoints")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEndpoints() {
		IntivaHealthState state = (IntivaHealthState) ExoPlatformLocator.getState();
		return Response.ok().entity(state.getEndpoints()).build();
	}
	
}
