package com.txmq.exo.transactionrouter;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.core.ExoState;

/**
 * ExoTransactionRouter implements an annotation-based transaction routing 
 * scheme.  To implement, you annotate a transaction processing method with 
 * the ExoTransactionType value that the method handles.
 * 
 * ExoTransactionRouter is a singleton, and is managed by ExoPlatformLocator.
 * Application code doesn't need to instantiate ExoTransactionRouter.  
 * Application code can access the router through ExoPlatformLocator.
 * 
 * During initialization, Exo applications should call addPackage() for each
 * package that contains annotated processing methods.  ExoTransactionRouter
 * will scan the package for @ExoTransaction annotations and catalog those
 * methods by the transactiont type they process.
 * 
 * States that inherit from ExoState will automatically route transactions
 * that come into the handleTransaction() method with no additional code 
 * (assuming you remembered to call super.handleTransaction()).
 * 
 * Methods that implement transactions must use the following signature:
 * 
 * @ExoTransaction("sometransaction")
 * public void myTransactionHandler(ExoMessage message, ExoState state, boolean consensus)
 * 
 * TODO:  Add a means for transaction processors to return data which will
 * later be made available through an API to client applications.
 */
public class ExoTransactionRouter extends ExoRouter<ExoTransaction> {
	
	/**
	 * Routes an incoming transaction to its processor.  Application code 
	 * should not need to call this method directly.  It is invoked by 
	 * ExoState.handleTransaction() when transactions are received by the 
	 * Hashgraph state.
	 * 
	 * Internally, it looks for a method that handles the type of transaction
	 * in the message and an instance of the class that encloses that method.
	 * It will create the instance if it needs to.  Assuming it finds/creates
	 * what it needs, it invokes the method passing in the message and state.
	 */
	public Object routeTransaction(ExoMessage<?> message, ExoState state, boolean consensus) throws ReflectiveOperationException {
		return this.invokeHandler(message.transactionType.getValue(), message, state, consensus);
	}
}
