Exo Application Architecture
============================
Exo provides three main features to application developers:
* Request handling
* Transaction processing
* Transaction logging

## Handling Requests
Exo provides two ways for applications to communicate with the Hashgraph:  REST and Java Sockets.  REST is widely supported by most any language that can communicate over the internet.  Java sockets are useful in cases where you might want to make use of or integrate the Hashgraph with existing Java code, such as an existing JAX-RS API or a Java client application.

Exo allows developers to expose REST handlers on their Hashgraph nodes.  It uses standard JAX-RS annotations and the Grizzly web server included in Java Standard Edition.  There are no dependencies on Java Enterprise Edition and you don't have to run in an application server or container.  You annotate your handler methods like you would in any other JAX-RS system, and Exo will scan for those methods and register them with Grizzly.

See [Developing Exo REST Interfaces](REST.md) to learn how to develop REST interfaces on Hashgraph nodes.

Exo also allows developers to easily communicate with other applications via Java Sockets.  Sockets are TLS-encrypted and client applications are authenticated using certificates.  Exo provides a generic message class that is passed back and forth over the socket connection and annotations for routing messages to handlers or the state based on transaction type.

See [Developing Exo Java Socket Interfaces](JavaSockets.md) to learn how to develop socket-based interfaces.

## Processing Transactions
Exo provides an architectural construct for separating business logic from your SwirldState using annotations to automatically route transactions received by your application state to transaction processor methods.  The framework defines a base message class and an extensible enum implementation for defining the list of transaction types your application processes.

See [Exo Messaging](Messaging.md) and [Exo Transaction Routing](TransactionRouting.md) to learn how to set up transaction routing and processing in your application.

## Logging Transactions
Exo provides a logging framework for writing transactions out to a persistent history.  The logger writes transactions to a blockchain data structure.  It uses a plug-in architecture to enable developers to add plug-ins for their choice of data storage.  Exo provides a plug-in for CouchDB (more adaptors coming soon).

See [Transaction Logging](TransactionLogging.md) to learn how to set up logging. 

## JSON Configuration
Exo can be configured using a JSON-formatted configuration file.  See [Configuration using exo-config.json](JSONConfig.md) to learn how to configure Exo using a configuration file.

## Test Mode
Exo supports a test mode for use with JUnit tests that test application logic.  In a typical application, there will be dependencies between components that live in the state.  When in test mode, Exo will run without dependencies on block logging or the Swirlds Platform class.  It maintains a dummy state, and transactions submitted through PlatformLocator.createTransaction() will be processed by the dummy state.
