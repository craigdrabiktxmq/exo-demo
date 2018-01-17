package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.ZooApiService;
import io.swagger.api.factories.ZooApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import io.swagger.model.Animal;

import java.util.Map;
import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/zoo")


@io.swagger.annotations.Api(description = "the zoo API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2018-01-08T17:35:13.100Z")
public class ZooApi  {
   private final ZooApiService delegate;

   public ZooApi(@Context ServletConfig servletContext) {
      ZooApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("ZooApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (ZooApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = ZooApiServiceFactory.getZooApi();
      }

      this.delegate = delegate;
   }

    @POST
    @Path("/animals")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "adds an animal to the zoo", notes = "Adds an animal to the zoo", response = Void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "item created", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "invalid input, object invalid", response = Void.class),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "an existing item already exists", response = Void.class) })
    public Response addAnimal(@ApiParam(value = "Animal to add" ) Animal animal
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.addAnimal(animal,securityContext);
    }
    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns all the animals in the zoo", notes = "Returns all of the animals in the zoo ", response = Object.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "search results matching criteria", response = Object.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "bad input parameter", response = Void.class) })
    public Response getZoo(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getZoo(securityContext);
    }
}
