Exo Transaction Routing
=======================

Exo provides a transaction routing framework based on [Exo Messages](Messaging.md) and the @ExoTransaction annotation.  The routing framework is used to route messages received by the application's SwirldState.handleTransaction() method to your application's business logic.  The routing mechanism helps you separate your business logic from application state without being dogmatic about how you organize your application.

Routing is handled on a per-transaction type basis.  If you haven't read the [Exo Messages](Messaging.md) documentation yet, please review it now.  Exo transaction processors accept ExoMessage and ExoState parameters.  You annotate the processor with the @ExoTransaction annotation to define the transaction type it processes.

```java
@ExoTransaction(ExoTransactionType.ANNOUNCE_NODE)
public void announceNode(ExoMessage message, ExoState state) {
    state.addEndpoint((String) message.payload);
}
```

At runtime, "announce node" transactions will be automatically routed to the announceNode() method for processing.  The framework passes in the received message and a reference to your application's state.  In the future, the framework will provide a mechanism for the application that submitted the transaction to receive a result message.  Once implemented, any return value from an Exo processor method will be passed back to the calling application.