package com.txmq.exo.transactionrouter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

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
public abstract class ExoRouter<T extends Annotation> {
	
	/**
	 * Map of transaction type values to the methods that handle them.
	 */
	protected Map<Integer, Method> transactionMap;

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
	
	protected Class<? extends Annotation> annotationType;
	/**
	 * No-op constructor.  ExoTransactionRouter will be instantiated by 
	 * ExoPlatformLocator and TransactionServer, and managed by the platform.
	 * 
	 * Applications should not create instances of ExoTransactionRouter.
	 * 
	 * @see com.txmq.exo.core.ExoPlatformLocator
	 */
	@SuppressWarnings("unchecked")
	public ExoRouter() {
		this.transactionMap = new HashMap<Integer, Method>();
		this.transactionProcessors = new HashMap<Class<?>, Object>(); 		
		Type tmp = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		Class clazz = (Class) tmp;
		this.annotationType = ((Class<? extends Annotation>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}
	
	/**
	 * Scans a package, e.g. "com.txmq.exo.messaging.rest" for 
	 * @ExoTransaction annotations using reflection and sets up the
	 * internal mapping of transaction type to processing method.
	 */
	public ExoRouter<T> addPackage(String transactionPackage) {
		Reflections reflections = new Reflections(transactionPackage, new MethodAnnotationsScanner());			
		
		Set<Method> methods = reflections.getMethodsAnnotatedWith(this.annotationType);
		for (Method method : methods) {
			@SuppressWarnings("unchecked")
			T methodAnnotation = (T) method.getAnnotation(this.annotationType);
			Method valueMethod;
			try {
				valueMethod = this.annotationType.getMethod("value", (Class<?>[]) null);
				this.transactionMap.put((Integer) valueMethod.invoke(methodAnnotation), method);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalArgumentException(
						"The annotation " + this.annotationType.getName() + 
						" must implement a value() method that returns a type of String"
				);
			}
		}
		
		return this;
	}
	
	protected Object invokeHandler(int key, Object... args) throws ReflectiveOperationException {
		if (this.transactionMap.containsKey(key)) {
			Method method = this.transactionMap.get(key);
			Class<?> processorClass = method.getDeclaringClass();			
			if (!this.transactionProcessors.containsKey(processorClass)) {
				Constructor<?> processorConstructor = processorClass.getConstructor();
				this.transactionProcessors.put(processorClass, processorConstructor.newInstance());				
			}
			
			Object transactionProcessor = this.transactionProcessors.get(processorClass);
			return method.invoke(transactionProcessor, args);
		} else {
			throw new IllegalArgumentException(
					"A handler for route type " + key + 
					" was not registered with the router"
			);
		}
	}
}
