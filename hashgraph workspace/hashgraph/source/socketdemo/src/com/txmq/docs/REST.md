Developing REST Interfaces with Exo
===================================

Exo makes it easy for developers who are familiar with JAX-RS to expose REST operations on Hashgraph nodes.  Simply create classes and methods for handling incoming requests, and Exo provides the mechanisms to expose those methods and provide access to the Hashgraph state.

## Identifying REST methods
An example of an endpoint method can be found in the service Exo provides for querying the URLs of nodes providing REST services:

```java
@Path("/HashgraphZoo/1.0.0")
public class EndpointsApi {
	@GET
	@Path("/endpoints")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEndpoints() {
		SocketDemoState state = (SocketDemoState) ExoPlatformLocator.getPlatform().getState();
		return Response.ok().entity(state.getEndpoints()).build();
	}
}
```

Note that all of the annotations used are just typical JAX-RS annotations, there is nothing custom to add.  The ExoPlatformLocator provides a way to access the application state for querying data.  For operations that modify state, handlers can create instances of ExoMessage and pass them to the platform:

```Java
@POST
@Path("/zoo/animals")
@Produces(MediaType.APPLICATION_JSON)
public Response addAnimal(Animal animal) {
    ExoMessage message = new ExoMessage(new SocketDemoTransactionTypes(SocketDemoTransactionTypes.ADD_ANIMAL), animal);
    
    try {
        ExoPlatformLocator.getPlatform().createTransaction(message.serialize(), null);
    } catch (IllegalStateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    return Response.status(201).entity(animal).build();
}
```

##Initializing the REST API

REST is enabled in Exo by calling the initREST() method on ExoPlatformLocator.  You should initialize your REST endpoints in the init() method of your SwirldMain.  Exo will spin up a Grizzly server and register JAX-RS annotated methods.

Invoke ExoPlatformLocator.initREST and pass in: 
* The port you want to listen for REST requests on
* A list of packages that contain your annotated classes

```java
ExoPlatformLocator.initREST(
    platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 2000, 
    new String[] {"com.txmq.socketdemo.rest"}
);
```