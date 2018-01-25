package io.swagger.api.impl;

import io.swagger.api.*;
import io.swagger.model.*;

import io.swagger.model.Animal;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.SwirldsAdaptor;
import com.txmq.socketdemo.SocketDemoTransactionTypes;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-08T17:35:13.100Z")
public class ZooApiServiceImpl extends ZooApiService {
	@Override
	public Response addAnimal(Animal animal, SecurityContext securityContext) throws NotFoundException {
		if (animal.getSpecies().equals("lion") || animal.getSpecies().equals("tiger")
				|| animal.getSpecies().equals("bear")) {

			SwirldsAdaptor adaptor = new SwirldsAdaptor();
			adaptor.sendTransaction(new SocketDemoTransactionTypes(SocketDemoTransactionTypes.ADD_ANIMAL), animal);
			return Response.ok().entity(animal).build();
		} else {
			return Response.serverError().build();
		}
	}

	@Override
	public Response getZoo(SecurityContext securityContext) throws NotFoundException {
		SwirldsAdaptor adaptor = new SwirldsAdaptor();
		ExoMessage response = adaptor.sendTransaction(new SocketDemoTransactionTypes(SocketDemoTransactionTypes.GET_ZOO), null);
		return Response.ok().entity(response.payload).build();
	}
}
