Exo Java Socket API
===================

Exo provides a Java socket-based communication mechanism.  Java sockets may be a better fit for existing Java applications that want to interact with a Hashgraph.  In the near future, Exo will implement a mechanism for returning transaction results to callers.  Java sockets will provide the ability to receive those updates in real time from the Hashgraph.

# Routing Socket Messages to Message Handlers
Exo will map incoming socket messages to handlers that have been annotated with @ExoTransaction.  The underlying mechanism is the same as used to [route transactions from SwirldState.handleTransaction() to processor methods](TransactionRouting.md).  Incoming messages are expected to be instances of ExoMessage.  The transaction type of the message is inspected, and the message is forwarded to the proper message handler if a mapping is found.

One important difference between transaction routing and socket message routing is that all transactions that can be received by a SwirldState will have a handler defined.  In the case of socket message routing, you will only define handlers for messages that:
* Only return data **without modifying the state**
* Require some kind of additional processing before they are forwarded on to the Hashgraph for processing.

If your operation modifies data in the SwirldState, it must be forwarded on to the platform to be processed by the Hashgraph.  Do not modify state data from socket message handlers.

If your operation should be forwarded to the Hashgraph and it does not require additional processing, you do not need to define a handler for it.  Exo will forward transactions it can't map to handlers on to the Hashgraph for you automatically.

To set up a mapping for a socket message handler, annotate the handler method with @ExoTransaction:
```java
@ExoTransaction(SocketDemoTransactionTypes.GET_ZOO)
public ExoMessage getZoo(ExoMessage message, ExoState state) {
    SocketDemoState _state = (SocketDemoState) ExoPlatformLocator.getState();
    Zoo result = new Zoo();
    result.lions(_state.getLions());
    result.tigers(_state.getTigers());
    result.bears(_state.getBears());
    
    return new ExoMessage(
        new ExoTransactionType(ExoTransactionType.ACKNOWLEDGE),
        result
    );	
}
```

Handler methods must accept an ExoMessage and ExoState parameter.  The router will pass in the message it received from the socket and a reference to the state.  Message handlers should return an instance of ExoMessage with their response.

# Initializing the Socket API

Initialize the socket API in the init() method of your SwirldMain by invoking the initSocketMessaging() method on ExoPlatformLocator.  Pass in:
* The port that Exo should listen for socket connections on
* A list of packages that contain your socket message handlers

```java
ExoPlatformLocator.initSocketMessaging(
    platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 1000,
    new String[] {"com.txmq.socketdemo.socket"}
);
```

Note that the method above creates an unsecured socket for messaging.  This configuration should not be used in a production setting.  Exo can configure a TLS-encrypted socket authenticated using X.509 certificates:

```java
ExoPlatformLocator.initSecuredSocketMessaging(
    platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 1000,
    new String[] {"com.txmq.socketdemo.socket"},
    "client.public",
    "clientKeystorePassword"
    "server.private",
    "serverKeystorePassword"
);
```

Please review the [Exo Java Client](https://github.com/craigdrabiktxmq/exo-java-client/blob/master/README.md) documentation for more information on how to configure keystores for secured socket configuration.

## Exo Java Client
The functionality in Exo covers the Hashgraph side of of the socket connection.  The [Exo Java Client](https://github.com/craigdrabiktxmq/exo-java-client) impolements the client application side of the socket connection.

## Message Payloads
ExoMessage payloads can be anything that implements Serializable.  In nearly all cases, your application will define a data model that will be transferred over the socket connection.  The classes that make up your data or messaging model need to be included in the Java applications on both sides of the socket connection, or you will encounter NoClassDefFound exceptions when messages are deserialized.  You will also need to include your ExoTransactionType subclass in both applications for the same reason.
