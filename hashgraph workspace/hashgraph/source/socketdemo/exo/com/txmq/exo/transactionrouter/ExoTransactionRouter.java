package com.txmq.exo.transactionrouter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

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
public class ExoTransactionRouter {
	
	/**
	 * Map of transaction type values to the methods that handle them.
	 */
	protected Map<String, Method> transactionMap;

	/**
	 * Methods have to be invoked on an instance of an object (unless
	 * we use static transaction handlers and that makes me feel dirty).
	 * 
	 * This map holds instances of each transaction processor class.
	 * An instance is automatically created if it doesn't exist.
	 * Transaction processor classes should be written as if they will
	 * only be instantiated once, and should be careful about any
	 * state they maintain.  Realize that Exo will probably only ever
	 * create one instance.
	 */
	protected Map<Class<?>, Object> transactionProcessors;
	
	/**
	 * No-op constructor.  ExoTransactionRouter will be instantiated by 
	 * ExoPlatformLocator and TransactionServer, and managed by the platform.
	 * 
	 * Applications should not create instances of ExoTransactionRouter.
	 * 
	 * @see com.txmq.exo.core.ExoPlatformLocator
	 */
	public ExoTransactionRouter() {
		this.transactionMap = new HashMap<String, Method>();
		this.transactionProcessors = new HashMap<Class<?>, Object>(); 
	}
	
	/**
	 * Scans a package, e.g. "com.txmq.exo.messaging.rest" for 
	 * @ExoTransaction annotations using reflection and sets up the
	 * internal mapping of transaction type to processing method.
	 */
	public ExoTransactionRouter addPackage(String transactionPackage) {
		/*
		Reflections reflections = new Reflections(new ConfigurationBuilder()
		     .setUrls(ClasspathHelper.forPackage(transactionPackage))
		     .setScanners(new MethodAnnotationsScanner())
		);*/
		Reflections reflections = new Reflections(transactionPackage, new MethodAnnotationsScanner());			
		
		Set<Method> methods = reflections.getMethodsAnnotatedWith(ExoTransaction.class);
		for (Method method : methods) {
			ExoTransaction annotation = method.getAnnotation(ExoTransaction.class);
			this.transactionMap.put(annotation.value(), method);
		}
		
		return this;
	}
	
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
	public Object routeTransaction(ExoMessage message, ExoState state, boolean consensus) throws ReflectiveOperationException { 
		if (this.transactionMap.containsKey(message.transactionType.getValue())) {
			Method method = this.transactionMap.get(message.transactionType.getValue());
			Class<?> processorClass = method.getDeclaringClass();			
			if (!this.transactionProcessors.containsKey(processorClass)) {
				Constructor<?> processorConstructor = processorClass.getConstructor();
				this.transactionProcessors.put(processorClass, processorConstructor.newInstance());				
			}
			
			Object transactionProcessor = this.transactionProcessors.get(processorClass);
			return method.invoke(transactionProcessor, message, state, consensus);
		} else {
			throw new IllegalArgumentException(
					"A handler for transaction type " + message.transactionType.getValue() + 
					" was not registered with the transaction router"
			);
		}
	}
}
