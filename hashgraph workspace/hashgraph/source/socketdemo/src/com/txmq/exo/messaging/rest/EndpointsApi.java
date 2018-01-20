package com.txmq.exo.messaging.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.txmq.exo.core.PlatformLocator;
import com.txmq.socketdemo.SocketDemoState;

@Path("/HashgraphZoo/1.0.0")
public class EndpointsApi {
	@GET
	@Path("/endpoints")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEndpoints() {
		SocketDemoState state = (SocketDemoState) PlatformLocator.getPlatform().getState();
		return Response.ok().entity(state.getEndpoints()).build();
	}
	
}
