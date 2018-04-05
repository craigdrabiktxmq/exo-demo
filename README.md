Hashgraph-Web Application Communication
================================

This project contains a demonstration of a web application communicating with a Swirld.  The application tracks lions, tigers, and bears in a zoo.  Users can add animals to the zoo and see the updated state.  The zoo state is tracked in a Swirld, and adding an animal executes a transaction on the Swirld.  The demo supports two methods of communication:
- Through a JAX-RS based REST API.  The JAX-RS application communicates with the Swirld via a TLS-secured, authenticated socket.  
- Directly to the Swirld via a REST API embedded in the Swirld itself.

NOTE:  THIS DOCUMENT IS OUT OF DATE...  AN UPDATE IS COMING SOON.

Running the Application
-----------------------
1. Install docker
2. Download the Hashgraph SDK.  The license doesn't explicitly grant permission to redistribute so the files aren't committed directly in the project.
3. Clone the repository.
4. Copy swirlds.jar and the data directory from the SDK to /hashgraph workspace/hashgraph
5. Run `docker-compose up` from the command line
6. Wait for docker to build all of the containers.  Eventually you'll see a message similar to `jaxrs_1      | [INFO] Started Jetty Server`.  The application is now up and running.
6. Open a browser and navigate to http://localhost

What's going on here?
---------------------
When the application loads, it will start asking for the state of the zoo every two seconds.  At startup, the application is configured to query the Hashgraph directly.  If you open the browser debugging tools and watch the requests to the zoo endpoint, you'll see that each call is sent to a different port, which routes it to a specific Hashgraph node.  The application asks the Hashgraph for a list of active nodes, then cycles through the nodes on each request.

If you change to "Use JAX-RS REST API" using the radio buttons at the top of the app, you'll see that all requests are then made to port 8080, which is routed to a JAX-RS web API running independent from the Hashgraph.  The JAX-RS tier receives requests from the browser and routes them to the Hashgraph, then returns the result to the browser.

How the JAX-RS/Hashgraph Integration Works
------------------------------------------

Walking through the Swirld itself is outside the scope of what I'm trying to demonstrate, so if you're not familiar with how a Swirld works, please head over to swirlds.com and download the SDK and read the docs.

In either Java project, the communications code is centralized in the com.txmq.swirldsframework.messaging package.   This code should be reusable between applications.  Right now, the hostname of the Swirld is hard-coded to the name of the Docker container it runs in.  A better approach would be to pull this from a configuration file, parameter, JNDI, etc. but be aware if you reuse this code or try to run outside of Docker, you'll need to update the hostname in SwirldsAdaptor in your client (web API) project.

We'll walk through the code in the order in which it's invoked.  First, the Swirld is started.  During initialization, it creates an instance of TransactionServer.  TransactionServer is responsible for configuring the security setup we're going to use, and opening a server socket to listen for client connections.  In the constructor, we instantiate the key management objects we need to secure the connection.  For the purposes of the demo, we are using a single public/private key pair for a single client.  All of the servers use the same server key pair.  When the client (web API tier) attempts to connect, it will authenticate using it's private key, which is validated on the Swirld using the client's public key, which is known to the Swirld (loaded on line 36).  Only clients that the Swirld already knows can be authenticated.

Next, a request will come through the REST API.  When you load the web app in the browser, it will issue a call to the REST API to retrieve the current state of the zoo.  In the API code, the request is ultimately handled by the getZoo method of ZooApiServiceImpl, in the io.swagger.api.impl package.  The method creates an instance of SwirldsAdaptor, which is the client-side communication management class for connecting to the Swirld.

SwirldsAdaptor works a lot like TransactionServer, but for the client side of the socket connection.  The server's hostname is hard-coded (see above) in this class, so if you're going to reuse the code or run outside of Docker, you'll need to change the host name on line 29 of SwirldsAdaptor.java.  Like TransactionServer, its constructor sets up key management and TLS on the socket, and public/private key pairs are used to authenticate client to server and server to client.  Once the service has created an adaptor, it invokes the sendTransaction method on the adaptor to send a transaction to the Swirld over the socket.  

Each transaction is encapsulated in a SwirldsMessage instance.  A SwirldsMessage consists of a transactiont type and a payload.  Transaction types are defined as an enum called SwirldsTransactionType.  A copy of this file exists on both the client and server, and it defines the types of transactions supported by the Swirld.  In our case, we have two "requests" - GET_ZOO (for retrieving the state of the zoo) and ADD_ANIMAL (for requesting that new animals are added to the zoo).  We also have a single response type, ACKNOWLEDGE.  SwirldsMessage payloads can be any Java type that implements Serializable.  Using the payload and transaction type, you can support passing Java objects of just about any type between the Swirld and your REST implementation.  The model objects for our application are in the io.swagger.model package, Animal.java and Zoo.java.  The model objects must exist on both the client and server.  If you aren't going to expose APIs directly on Hashgraph nodes, you can strip the annotations used by JAX-RS (or Spring or whatever) so you don't have to include unnecessary packages in your Hashgraph code.  When the add animal method is invoked, an instance of Animal is passed to the Swirld in the payload field.  When the get zoo method is invoked, the Hashgraph returns an instance of Zoo in the payload of the acknowledge message it returns to the API.  SwirldsMessage includes the code necessary to serialize and deserialize SwirldsMessages to and from byte arrays, which are transmitted over the socket.

TL;DR:  Define your transaction types in SwirldsTransactionType, create your model objects in both the client and server projects, and pass an instance of SwirldsMessage to SwirldsAdaptor.sendTransaction().

How the Hashgraph REST API Works 
--------------------------------

The Hashgraph embedded REST API is based on Grizzly, which is a web server technology that is part of Java SE.  It lets us develop applications that can handle HTTP requests without deploying our Java application in a JEE server like Tomcat.  See http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/griz_jersey_intro/Grizzly-Jersey-Intro.html#section4 for a walkthrough of setting up a basic REST service using Grizzly.

In the Hashgraph application code, the Grizzly REST service works as follows:
- In SocketDemoMain, set up the Grizzly server itself and instantiate it.  This is done starting on line 84.  On line 97, we set up a ResourceConfig that tells Grizzly that it should look in the com.txmq.socketdemo.http package for REST call handlers.  We also set up a CORS filter to enable the web application to be served from a different port.

The actual handlers for HTTP requests are defined in com.txmq.socketdemo.ZooRestApi.  These are set up using JAX-RS annotations as with any other JAX-RS API.  The methods themselves work a lot like the socket-based versions, reading from state or submitting SwirldsMessages to the Hashgraph to add animals.

On the client, we're using the beginnings of a framework called "Exo", which is used to set up and handle communication between the Angular client and the APIs exposed on the Hashgraph nodes.  More info on Exo is forthcoming, but for now you should look at the in-code documentation in client/client/exo/*.

Reuse
-----

You are welcome to reuse/modify this code pursuant to the terms of the license file.  You'll want to copy the com.txmq.swirldsframework.messaging package into your application.  Next, define your transaction types and model objects.  On the server side, create an instance of TransactionServer to listen for client connections.  Finally, build out your REST API in the technology of your choosing and use SwirldsAdaptor to handle messaging between the Swirld and your REST API code.

No warranty or support is expressed or implied, use at your own risk.

Here's the catch:  I don't know what's coming in the near term from Swirlds.  The public SDK only runs through the Swirlds browser, which I imagine will be changed at some point.  I don't know whether or not Swirlds has a connectivity paradigm in the works, or when it would arrive.  This demonstration could certainly be developed into a framework for enabling communication between Swirlds and other Java or Javascript applications.  The point is, I don't know how much development this is going to see, because it could all be obsolete tomorrow.  Having said that, I'm willing to look at suggestions and pull requests if someone wants to contribute to this code.

Licensing
---------
The code in this GitHub repository is made available through the MIT license (see LICENSE.txt).  The Swirlds SDK is licensed by Swirlds.  You must agree to abide by Swirlds licensing requirements to run this application.  Use of the Swirlds SDK in production requires an additional license.  Contact Swirlds for more information or to obtain a license for production use.

Shameless Plug
--------------
TxMQ's Disruptive Technology Group consults and develops distributed ledger-based solutions on a number of platforms, including Swirlds.  If you have projects you're looking to build or need consulting services on Swirlds, Hyperledger, Ethereum, or blockchain/distributed ledger in general please reach out.

craig.drabik@txmq.com
