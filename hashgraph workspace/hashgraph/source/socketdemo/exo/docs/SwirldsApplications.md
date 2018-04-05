How a Swirld Works
==================

A Swirld is basically an application state replicated across each node in a Hashgraph network.  In order to build Swirlds (or applications with Swirlds), You need to understand a few basic mechanics about how the platform functions.  There are two interfaces developers must implement in order to build a Swirld - SwirldMain and SwirldState.  

Your SwirldMain implementation intializes the Swirlds platform.  The init() method of your SwirldMain will be called once, before the node begins receiving messages.  This method is used to initialize any constructs you need to have in place before the node starts accepting incoming transactions.  The run() method is called once the node is ready to receive messages and start responding to transactions.  The run() method should never return, and it basically keeps the node running.

In the demo applications, you also see the application logic itself embedded in various places depending on the demo application.  In a real-world application, it's very unlikely that your users will each run a node that encapsulates the application's user interface.  It's also unlikely that the node will not receive requests for information or incoming transactions from the outside world, and the SDK doesn't provide a way to get those messages in.

SwirldState can be thought of as your application state, held in memory and kept in sync throughout the Hashgraph network.  It will hold your data structures, process incoming transactions, and make updates to the state data based on those transactions.  The SwirldState interface includes several methods used to duplicate the state, as well as a handleTransaction() method (more on this in a minute).

A running node makes copies of the state each round.  Incoming transactions that have not yet reached consensus across the network are processed by the copy of the state.  The handleTransaction() method is invoked for each incoming transaction.  The developer can choose to process or ignore transactions that haven't yet reached consensus.  Once consensus is reached for a round, the original SwirldState receives the transactions for that round, in consensus order.  Again, handleTransaction() is invoked for each incoming transaction and is processed by the state.  The copied states can be discarded as consensus is reached on the transactions they've processed.

In all but the most trivial applications, your handleTransaction method will turn into a giant switch statement (best case scenario).  If you want to save the transactions, you can either hold them in the state - which will eventually bloat the state and run your machine out of memory - or homebrew a way to log them to the filesystem or database.

The Challenges of Building an Application on the Swirlds SDK
------------------------------------------------------------

We can identify several challenges and architectural issues from the above description.

* There isn't a way for transactions to get into the Hashgraph from the outside world.
* There isn't a way to log transactions to someplace outside of the SwirldState
* There isn't an architecture for handling transactions in the SwirldState
* There isn't a good way for an application to get feedback on the success or failure of its transaction

Exo was developed to address these challenges.  Exo (currently) provides:
* Support for applications to invoke API methods on the Hashgraph using REST or Java Sockets (WebSocket support coming soon) 
* Support for logging incoming transactions to a blockchain data structure outside of the SwirldState
* An annotation-based routing architecture to connect your SwirldState to transaction processing business logic
* (Coming Soon) Real-time feedback on transaction success or failure


