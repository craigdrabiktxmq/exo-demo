package com.txmq.exo.messaging.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

/**
 * Basic CORS filter, allows access from any origin, and shuts 
 * Grizzly up when certain common headers are received in requests.
 */
public class CORSFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		response.getHeaders().add("Access-Control-Allow-Origin", "*");
		response.getHeaders().add("Access-Control-Allow-Headers",  "Origin, Content-type, Accept");
		response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
	}

}
