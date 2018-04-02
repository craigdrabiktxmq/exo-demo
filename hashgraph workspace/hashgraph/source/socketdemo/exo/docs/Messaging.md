Exo Messaging
=============

Exo Messaging is a core component of the framework.  It is a consistent mechanism that the framework can rely on to define messages and transaction types.  

## Transaction Types
When you implement an application using Exo, you define the list of transaction types that the application supports by extending the ExoTransactionType class.  ExoTransactionType is an "extensible enum" - it defines a few internal/default transactions the framework implements.  Each transaction type is identified by a unique string.  Your application must extend ExoTransactionType and initialize it with the list of transaction identifiers it will support.  The SocketDemoTransactionTypes class in the demo application provides a good example:

```java
public class SocketDemoTransactionTypes extends ExoTransactionType {
	public static final String GET_ZOO = "GET_ZOO";
	public static final String ADD_ANIMAL = "ADD_ANIMAL";
	
	private static final String[] values = {
			GET_ZOO,
			ADD_ANIMAL
	};
	
	public SocketDemoTransactionTypes() {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
	}
	
	public SocketDemoTransactionTypes(String transactionType) {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
		
		this.setValue(transactionType);
	}
}
```

Note that we define each transaction identifier as a static constant on the class - this is not strictly required but is a best practice.  It makes routing transactions less error-prone - you can use those constants later on when you [define your routing metadata](TransactionRouting.md).

## ExoMessage
ExoMessage is the base messaging class used by the framework.  It defines a message as a transaction type and a payload.  The transaction type will be an instance of your ExoTransactionType subclass.  The paylod can be anything that implements Serializable.  Payloads can and should be transaction-specific.  Any code that handles a message non-generically can test the transaction type and/or the payload and cast it to the correct type.

ExoMessage is used by [Exo Transaction Processors](TransactionRouting.md) and [Java Sockets](JavaSockets.md).

