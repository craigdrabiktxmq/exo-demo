Exo Java Client
===============

The Exo Java Client library works with the Exo framework for Hashgraph-based application development to facilitate socket-based communication between a Java client application and a running Hashgraph.  Transactions or messages are exchanged between client and server via a TLS-encrypted socket authenticated using X.509 certificates.

## Using the Java Client to Interact with a Hashgraph

Before continuing, review the [Exo Messaging Documentation](https://github.com/craigdrabiktxmq/exo/blob/master/docs/Messaging.md) in the Exo Framework documentation.

To use the Java Client in your application:
1. Determine a list of known Hashgraph node hostnames and ports that are listening for socket connections and create an exo-config.json file that defines the list of known sockets.
2. Copy your Hashgraph application's ExoTransactionType subclass.
3. Ensure that any value objects you're going to be passing back and forth over the socket are in sync between client and server codebases.  If the value objects aren't in sync, you will encounter deserialization issues.
4. Create the keystores that hold your authentication certificates.
5. Use SwirldsAdaptor to make calls to the Hashgraph

### Configuring Known Sockets
You initialize the list of known sockets by defining an exo-config.json file.  This file should wind up in the root directory of where your application runs.  Define the list of known sockets as follows:
```json
{
	"clientConfig": {
		"knownSockets": [
			{
				"hostname": "localhost",
				"port": 51204
			},
			{
				"hostname": "localhost",
				"port": 51205
			},
			{
				"hostname": "localhost",
				"port": 51206
			},
			{
				"hostname": "localhost",
				"port": 51207
			}		
		]
	}
}
```

### Copy or Synchronize Messaging Classes
In order for deserialization to work properly, your value objects and transaction type classes must match between client and server.  Ensure any value objects that will be transferred between client and server are available to both codebases and are the same structurally.  If you are connecting a JAX-RS API to a Hashgraph, you may strip out the serialization metadata from your value objects if desired.  Be sure you copy the ExoTransactionType subclass that defines the transaction types that your Hashgraph application responds to.

### Create Authentication Keystores
The [Using JSSE for Secure Socket Communication Tutorial](https://www.ibm.com/developerworks/java/tutorials/j-jsse/j-jsse.html) is an excellent walkthrough of how secure socket communication works in Java.  In order to configure Exo for secure socket communication, we follow the steps under "Key Management" in the tutorial to generate key pairs and keystores for both sides of the socket.  Once you have created your public and private client and server keystores, place client.public and server.private in your Hashgraph project.  Place client.private and server.public in your client application.  In both cases, thse files should be placed in the directory in which your application executes.

### Use SwirldsAdaptor to Communicate with the Hashgraph
Use an instance of SwirldsAdaptor to send messages tot he Hashgraph and receive responses.  In the following example we send a transaction of the type "GET_ZOO" with a null payload:
```java
SwirldsAdaptor adaptor = new SwirldsAdaptor();
ExoMessage response = adaptor.sendTransaction(
    new SocketDemoTransactionTypes(SocketDemoTransactionTypes.GET_ZOO), 
    null
);
```

