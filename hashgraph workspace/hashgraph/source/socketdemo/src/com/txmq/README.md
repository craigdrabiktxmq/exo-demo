Exo Framework
=============

Exo is a framework for developing applications on the Swirlds Hashgraph platform.  Primarily, Exo offers features and an application architecture for getting transactions into a Hashgraph, processing those transactions once they've been accepted, and logging transactions out to persistent storage.

Exo was developed to provide the features that Swirlds' SDK lacks that I knew I would need to develop real-world applications.  I'm sure other folks have needs and ideasthat aren't on my radar yet, so I would encourage feature requests and development submissions from other Hashgraph developers.  The framework will only improve for everyone as more people get involved.

Features
--------

Exo implements the following feature set:
- Socket-based messaging for Java applications
- REST-based messaging for anything that can make HTTP requests
- An architecture for structuring transaction processing logic and automated routing of transactions to transaction processors
- A framework for logging processed transactions to blockchain-based (blockchain the data structure, not blockchain the platform)

How To Build Applications on Exo
--------------------------------

### Getting Started
The easiest way to get started is to replicate or repurpose one of the Swirlds SDK demo applications.  Download the SDK from (https://www.swirlds.com/download/) and unzip to the directory of your choice.  You can install Exo in one of two ways:
* Download zipped source and extract the zip file's contents.  Copy the "exo" directory into your source tree - it should be copied to <my source root>/com/txmq so that the directory structure lines up with the package structure.
* Use git subtrees to link the exo source to your project.  Check out https://medium.com/@porteneuve/mastering-git-subtrees-943d29a798ec for a tutorial on how git subtrees work.  Again, you want the exo folder to fall under <source root>/com/txmq when you set up your subtree.

### Initialize the platform
Exo needs a reference to the Swirlds platform to run, and it also needs some configuration to happen before it's ready for use.  Each Swirlds application has a main class that inheirits from SwirldsMain.  In your application's main class, initialize the platform either in the init() method or first thing in the run() method.
```java
ExoPlatformLocator.init(platform);
```
You also will need to initialize the block logging system.  Currently, logging is mandatory and we have implemented only CouchDB support for logging - the ability to turn logging off will come shortly, and implementations for additional logging targets will come as they are contributed or needed.  Create a CouchDBBlockLogger instance and pass it to the logger:
```java
System.out.println("Main creating logger for " + platform.getAddress().getSelfName());
CouchDBBlockLogger blockLogger = new CouchDBBlockLogger(
    "zoo-" + platform.getAddress().getSelfName().toLowerCase(),
    "http",
    "localhost",
    5984);

BlockLogger.setLogger(blockLogger, platform.getAddress().getSelfName());
```

Next, you map your application's transaction types to their handlers:
```java
ExoPlatformLocator.getTransactionRouter().addPackage("com.txmq.socketdemo.transactions");
```

Finally, you set up the mechanisms you'll use to accept incoming messages from client applications.  Exo supports a Java socket-based mechanism and REST.

To use sockets, you create an instance of a TransactionServer.  The port can be any port you like.  My convention is to add 1000 to whatever port the Hashgraph is listening on, which can be obtained from the address book.
```java
int port = platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 1000
TransactionServer server = new TransactionServer(platform, port);
```

To expose a REST API, we use Grizzly, which is a web server offered as part of Java SE.  No Enterprise Edition or web application server is required.  Again, my convention is to add 2000 to whatever port the Hashgraph is listening on, and have Grizzly listen to that port.  The following code creates an http server that routes incoming requests to the endpoints defined in the packages "com.txmq.exo.messaging.rest" and "com.txmq.socketdemo.rest":
```java
int port = platform.getState().getAddressBookCopy().getAddress(selfId).getPortExternalIpv4() + 2000;
		//URI baseUri = UriBuilder.fromUri("http://localhost").port(port).build();
		URI baseUri = UriBuilder.fromUri("http://0.0.0.0").port(port).build();
		ResourceConfig config = new ResourceConfig()
				.packages("com.txmq.exo.messaging.rest")
				.packages("com.txmq.socketdemo.rest")
				.register(new CORSFilter())
				.register(JacksonFeature.class);

		System.out.println("Attempting to start Grizzly on " + baseUri);
		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
```
