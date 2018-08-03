Exo Application Architecture and the Pipeline
=============================================

Version 1 of Exo intorduced an annotation-based scheme for routing Swirlds transactions to processing logic.  At a high level, the architecture invoked routing code each time handleTransaction() was invoked in your SwirldState, which would then invoke the correct method on the correct class to apply application-specific logic.  Exo 2 expands on this idea by introducing the pipeline.  The pipeline is a consistent flow for transaction processing.  All transactions follow the same path through the pipeline.  At each stage, developers have the opportunity to hook business logic into the pipeline to perform application-specific tasks and return information to client applications.

##Architecture

##Exo Pipeline
Transactions in Exo move through a predefined pipeline.  The framework automatically moves transactions from one step to the next.  Events are emitted at each step.  Developers register hooks for pertinent events and implement their application's business logic in the hooks.  Each transaction is represented by a message, which contains the transaction's inputs and the transaction state.  The transaction state is developer-defined, and is used to capture the progress of the transaction through the pipeline and the results obtained when processing each step.  At any point in the pipeline, transactions can be *interrupted*, which ends further progress along the pipeline.

(pipeline_overview.png)

The diagram above describes the stages in the pipeline and the order they are executed in (left to right).  When a transaction is moved from one stage to the next, an event is emitted.  Developers write handlers for one or more events to implement their application logic.

### Event Types
There are two distinct kinds of events in Exo2.  **Platform events** are emitted when transactions reach points in the pipeline where developer code can run.  Developers write handlers for these events to retrieve data, apply business logic, and modify the state.  **Reporting events** are emitted when the transaction reaches points in the pipeline where end users or client programs might be interested in the results of the transaction at that point in the pipeline. 

Generally speaking, developers return results to clients by coding **reporting event handlers** and perform transaction processing by coding **platform event handlers**.

### Events
* (messageReceived) is emitted when a transaction message enters the pipeline.  The easiest way to get transaction messages into the pipeline is to use the code generators to generate a REST API or WebSocket-based message schema.  For most read-only transactions, (messageReceived) will be the only event your handler will need to listen for.  (messageReceived) is a platform event.
* (submitted) is emitted when a transaction is submitted to the Swirlds platform.  (submitted) is a reporting event.
* (executePreConsensus) is emitted when a transaction is ready to be processed pre-consensus - it occurs when handleTransaction() is invoked on the SwirldState for a transaction with the consensus parameter equal to false.  Applications that require processing to occur pre-consensus can register handlers against this event.  (executePreConsensus) is a platform event.
* (preConsensusResult) is emitted after all pre-consensus processing has completed.  (preConsensusResult) is a reporting event.
* (executeConsensus) is emitted when a transaction is ready to be processed at consensus - it occurs when handleTransaction() is invoked on the SwirldState for a transaction with the consensus parameter equal to true.  Generally speaking, developers will code handlers for this event when they need to modify state.  (executeConsensus) is a platform event.
* (preConsensusResult) is emitted after all at-consensus processing has completed.  (consensusResult) is a reporting event.
* (transactionComplete) is emitted when the transaction has fully completed its lifecycle.  Note that a transaction may not complete the entire pipeline.  It can be interrupted by any handler at any time.  A (transactionComplete) event means no further events will be issued for this transaction, and the state of the message is considered its final result.  (transactionComplete) is a reporting event.

## Receiving transactions
Transactions can be received through WebSockets, REST, or Java sockets.  When implementing REST, developers write JAX-RS-annotated code to implement their REST API.  Each API method essentially packages up the method's inputs into an ExoMessage and submits it.  The demo application's "add animal" endpoint code looks like this:

```
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
```
Something to take notice of in the above code is that we are using asyncronous responders to return a result to the client.  If we want to let the client know when the transaction has reached consensus, been applied, and what the result is, then we have to submit it to the Hashgraph network and wait some indeterminate period of time for consensus.  That operation is asynchronous.  On line 6, we register a **responder** with Exo.  Responders allow the framework to route results back to the proper client when those asyncronous events occur.

Socket-based APIs can skip this step - the framework exposes generic socket or web socket server features that receive ExoMessages from client applications directly, handles the responder functionality automatically, and submits them to the pipeline.

## Handling Platform Events

Platform events are used by developers to take some kind of action on a particular type of transaction at a particular stage.  Applications do not need to implement handlers every platform event, and applications may implement different combinations of handlers for each transaction type.

Exo exposes three platform events - (messageReceived), (executePreConsensus), and (executeConsensus).  To implement a handler for a platform event, annotate your method with the `@ExoHandler` annotation.  Handler methods accept an `ExoMessage<?> message` parameter and a `SwirldState state` parameter as input, and can return any serializable type as a result.  Application methods should specify the message's payload type if known (e.g. `ExoMessage<Animal> message`), and should use their SwirldState implementation's type as the type for the state parameter (e.g. `ZooState state`).

###Handling (messageReceived)
The (mesageReceived) event is emitted **before** a transaction is submitted to the Hashgraph network.  It can be used to acquire data from outside sources and perform validation against the current state before submitting the transaction to the network.  (messageReceived) will most often be used to handle read-only requests for data from your application's state.

This example shows how the "get zoo" transaction's (messageReceived) event is handled:

```
@ExoHandler(transactionType=ZooDemoTransactionTypes.GET_ZOO, 
				events={PlatformEvents.messageReceived})
public Zoo getZoo(ExoMessage<?> message, SocketDemoState state) {
    Zoo zoo = new Zoo();
    zoo.setLions(state.getLions());
    zoo.setTigers(state.getTigers());
    zoo.setBears(state.getBears());
    
    message.interrupt();
    return zoo;
}
```

The method's `@ExoHandler` annotation tells Exo that this method should be invoked any time the pipeline emits a (messageReceived) event for an `ExoMessage` with a transactiont type of `GET_ZOO`.  Exo will automatically pass the message that triggered the event and the current consensus state into the handler.  This handler creates a `Zoo` object and copies the animals from the state into the new `Zoo`.  Finally, it returns the `Zoo` as a result.

Note that on line 9 we call `message.interrupt()`.  Because `GET_ZOO` does not modify the application state, there is no need to submit it to the network for consensus.  Calling `interrupt()` means that the message will not continue to advance through the rest of the pipeline.  Exo will route it directly to (transactionComplete).

###Handling (executePreConsensus) and (executeConsensus)
The (executePreConsensus) and (executeConsensus) events are emitted when `SwirldState.handleTransaction()` is invoked for a particular ExoMessage.  As their names imply, (executePreConsesnus) is emitted when `handleTransaction()`'s consensus parameter is false, while (executeConsensus) is invoked when true.  Developers will implement handlers for these events for transactions which change the application state.  Note that you do not have to implement handlers for both methods.  Note also that you can implement one handler that responds to both events, if your processing logic is the same for both pre- and post-consensus processing.

This example shows how we handle "add animal" transactions in the demo application:
```
@ExoHandler(transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
            events={PlatformEvents.executePreConsensus, PlatformEvents.executeConsensus},
            payloadClass=Animal.class)
public void addAnimal(ExoMessage<Animal> message, SocketDemoState state) {
    //todo:  improve this so that we're testing if an animal of the same name exists, and failing if so 
    Animal animal = message.payload;
    switch (animal.getSpecies()) {
        case "lion":
            state.addLion(animal.getName());
            break;
        case "tiger":
            state.addTiger(animal.getName());
            break;
        case "bear":
            state.addBear(animal.getName());
            break;
    }						
}
```

Similar to how we handled (messageReceived) we have an `@ExoHandler` annotation that indicates which transaction types and events this method handles.  In this case, we're listening for (executePreConsensus) and (executeConsensus) events on `ADD_ANIMAL` transactions.  There is an additional property on this annotation - `payloadClass`.  This property is required only when using web sockets or Java sockets and is used by the framework to figure out how to deserialize incoming messages.  At runtime, the transaction type of an incoming message is used to look up the `payloadClass` value for handlers of that transaction type.  That class is used as the type of payload when deserializing to `ExoMessage`.  In this case, Exo knows that messages with a transaction type of `ADD_ANIMAL` are of the type `ExoMessage<Animal>`.

The rest of the method is straightforward - it inspects the message payload and adds the new animal's name to the list for the correct species.  This example does not return a result, but it could for example echo back the animal it added or the updated state of the zoo.

### Subscribing to Reporting Events
Subscribers react to a transaction's progress through the pipeline.  Typically, subscribers will be used to listen for the completion of a transaction and relay the results to client applications.  In a REST application, subscribers are typically invoked only once while a WebSocket could relay multiple messages about a single transaction back to a client application.  Subscribers run in your application's SwirldsMain and can therefore also be used to take action in response to events occurring on its node.

Each platform event has a corresponding reporting event.  (submitted) follows (messageReceived) and indicates that the transaction was submitted to the network.  (preConsensusResult) and (consensusResult) are emitted in response to (executePreConsensus) and (executeConsensus) respectively.  (transactionComplete) is a special case.  It is invoked at the end of the pipeline, and is guaranteed to be emitted even if a message is interrupted.  (transactionComplete) can always be used to listen for teh "final result" of an operation, even if it was interrupted at the (messageReceived) stage and was never actually submitted to the network.  That makes it easy to implement operations for clients that only care about the end result.

The platform will pass an `ExoNotification` to a subscriber that indicates the status of the operation at that point in time.  If a result was returned from a platform event handler, that value will be passed to the subscriber in the `ExoNotification`.

Earlier, in "Handling (messageReceived)", we saw the demo application's (messageReceived) handler for `GET_ZOO` requests:
```
@ExoHandler(transactionType=ZooDemoTransactionTypes.GET_ZOO, 
				events={PlatformEvents.messageReceived})
public Zoo getZoo(ExoMessage<?> message, SocketDemoState state) {
    Zoo zoo = new Zoo();
    zoo.setLions(state.getLions());
    zoo.setTigers(state.getTigers());
    zoo.setBears(state.getBears());
    
    message.interrupt();
    return zoo;
}
```

In this case, a handler for (submitted) or (transactionComplete) would receive an `ExoNotification` with the returned zoo as its payload (`ExoNotification<Zoo>`).  We have such a handler, for (transactionComplete):

```
@ExoSubscriber(transactionType=ZooDemoTransactionTypes.GET_ZOO, events={ReportingEvents.transactionComplete})
public void getZooTransactionCompleted(ExoNotification<?> notification) {
    AsyncResponse responder = this.getResponder(notification);
    if (responder != null) {
        responder.resume(notification);
    }
}
```

The `@ExoSubscriber` annotation tells Exo that this method should be invoked any time a (transactionComplete) event is emitted for a `GET_ZOO` transaction.  Way back at the beginning of this document, we saw an example of a REST API endpoint registering a responder for a message:

```
this.subscriberManager.registerResponder(message, ReportingEvents.transactionComplete, response);
```

Our subscriber method now needs that responder in order to relay the notification back to the client.  It simply asks for the responder that goes with the notification it's just received - the notification contains the original message, and Exo uses that information to find the `AsyncResponse` instance that it belongs to.  The subscriber then uses the responder to return the notification back to the client.

For web or Java sockets, we follow a similar pattern.  Remember that for socket-based transactions, Exo automatically managers responders and will have maintained an association between the original message and the socket that received it.  Exo provides base classes for socket subscribers that automate the task or returning a notification to a client through the correct socket, and our subscriber code simply passes in the notification:

```
@ExoSubscriber(	transactionType=ZooDemoTransactionTypes.GET_ZOO, events={ReportingEvents.transactionComplete})
public void getZooTransactionProgress(ExoNotification<?> notification) {
    this.sendNotification(notification);
}
```

For `GET_ZOO`, which is read only, we only care about the end result - either we get an error (which will be attached to the notification) or we get the result.  When we add animals, the transaction moves through the entire pipeline - and we can return information about what's happening to the transaction at each stage:

```
@ExoSubscriber(	transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
                events= {	ReportingEvents.submitted, 
                            ReportingEvents.preConsensusResult, 
                            ReportingEvents.consensusResult, 
                            ReportingEvents.transactionComplete	})
public void addAnimalTransactionProgress(ExoNotification<?> notification) {
    this.sendNotification(notification);
}
```

Note that the events parameter to our `@ExoSubscriber` annotation registers this subscriber to all of the reporting events emitted by the pipeline.  When you run the demo application and connect over web socket, you'll be able to see all of the events as they occur when you add animals to the zoo.

##Working with REST and Sockets
You've probably figured out by now that the code that really does the work - looking up data in (messageReceived) handlers or validating and applying transactions in (executeConsensus) - is independent of the mechanism used to submit the transaction.  You can easily structure your application to work with REST, web sockets, or Java sockets and reuse the same code.  Your subscribers will be different, but that code is basically boilerplate (hint - could be code generated).  Similarly, you have a little more work to do to write the JAX-RS methods that implement a REST API, but those again are boilerplate (hint - code generated).

##TL/DR; for REST APIs

###How do I return data from the state?

Write a JAX-RS-annotated method that:
* Packages up any parameters into an `ExoMessage`
* Registers a responder of type AsyncResponse
* Submits the message

and a platform event handler that:
* Is annotated with `@ExoHandler` for the transaction type and (messageReceived) event
* Looks up the requested data and prepares a result
* Interrupts the transaction
* Returns the prepared result

and a subscriber method that:
* Is annotated with `@ExoSubscriber` for the transaction type and (transactionComplete) event
* Retrieves the `AsyncResponse` registered in the JAX-RS method
* Uses the `AsyncResponse` to return a notification

###How do I process a transaction that modifies the state?

Write a JAX-RS-annotated method that:
* Packages up any parameters into an `ExoMessage`
* Registers a responder of type AsyncResponse
* Submits the message

and a platform event handler that:
* Is annotated with `@ExoHandler` for the transaction type and (executeConsensus) event
* Validates the transaction and applies state changes
* Optionally returns a result

and a subscriber method that:
* Is annotated with `@ExoSubscriber` for the transaction type and (transactionComplete) event
* Retrieves the `AsyncResponse` registered in the JAX-RS method
* Uses the `AsyncResponse` to return a notification

##TL/DR; for socket APIs
###How do I return data from the state?

Write a platform event handler that:
* Is annotated with `@ExoHandler` for the transaction type and (messageReceived) event
* Looks up the requested data and prepares a result
* Interrupts the transaction
* Returns the prepared result

and a subscriber method that:
* Is annotated with `@ExoSubscriber` for the transaction type and any events the client is interested in.  Minimally, you should listen for the (transactionComplete) event.
* Retrieves the `WebSocket` or `Socket` registered in the JAX-RS method
* Uses the `WebSocket` or `Socket` to return a notification

###How do I process a transaction that modifies the state?

Write a JAX-RS-annotated method that:
* Packages up any parameters into an `ExoMessage`
* Registers a responder of type AsyncResponse
* Submits the message

and a platform event handler that:
* Is annotated with `@ExoHandler` for the transaction type and (executeConsensus) event
* Validates the transaction and applies state changes
* Optionally returns a result

and a subscriber method that:
* Is annotated with `@ExoSubscriber` for the transaction type and any events the client is interested in.  Minimally, you should listen for the (transactionComplete) event.
* Retrieves the `WebSocket` or `Socket` registered in the JAX-RS method
* Uses the `WebSocket` or `Socket` to return a notification
