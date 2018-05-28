Event-Driven Exo
================

Exo2 switches to an event-based architecture

## BREAKING CHANGES
* ExoTransactionType has moved from String keys to Integer keys.  This will save some size on the serialized transaction payloads.

## Pipeline and Events
Transactions move through a predefined pipeline.  The framework automatically moves transactions from one step to the next.  Events are emitted at each step.  Developers register hooks for pertinent events and implement their application's business logic in the hooks.  Each transaction is represented by a message, which contains the transaction's inputs and the transaction state.  The transaction state is developer-defined, and is used to capture the progress of the transaction through the pipeline and the results obtained when processing each step.  At any point in the pipeline, transactions can be *interrupted*, which ends further progress along the pipeline.

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
Transactions can be received through WebSockets or via REST.  Code generators will be developed to translate an API design document into the Java code that defines a developer's REST API or socket messaging schema.

## Handlers
Exo2 defines three kinds of handlers.

### Message Handlers
Message handlers receive incoming messages from client applications and apply processing before a transaction is submitted to the platform - before Platform.createTransaction() is invoked.  Message handlers are used to:
* Process read-only requests that do not modify data in the state.  For read-only requests, a message handler that listens for (messageReceived) events is all that developers need to code.
* Pre-validate transactions against the consensus state before submitting transactions to the platform.
* Calculate or acquire data that will be used later on when processing the transaction.

Message Handlers listen for platform events.

### Transaction Handlers
Transaction handlers implement application logic that processes transactions that modify state.  They listen for (executePreConsensus) and/or (executeConsensus) events.  In response, they validate and apply transactions to the state.

Transaction handlers listen for platform events.

### Subscribers
Subscribers react to a transaction's progress through the pipeline.  Typically, subscribers will be used to listen for the completion of a transaction and relay the results to client applications.  In a REST application, subscribers are typically invoked only once while a WebSocket could relay multiple messages about a single transaction back to a client application.  Subscribers run in your application's SwirldsMain and can therefore also be used to take action in response to events occurring on its node.

Subscribers listen for reporting events.


Notes
=====

## Notifications
* related message ID
* status
* pipeline stage
# payload