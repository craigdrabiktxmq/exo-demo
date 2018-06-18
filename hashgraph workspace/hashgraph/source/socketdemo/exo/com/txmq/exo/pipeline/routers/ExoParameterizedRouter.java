package com.txmq.exo.pipeline.routers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.PlatformEvents;
import com.txmq.exo.pipeline.metadata.ExoHandler;
import com.txmq.exo.pipeline.metadata.ExoSubscriber;
import com.txmq.exo.transactionrouter.ExoRouter;

/**
 * Generic router that enables us to "parameterize" the lookup of methods decorated with handler metadata.
 * Instead of just decorating a method with @ExoTransaction like we did in Exo 1, we can decorate a method
 * with metadata that describes which event or events it should react to, e.g. 
 * 
 *  @ExoHandler(PlatformEvents.executePreConsensus)
 *  @ExoHandler(PlatformEvents.executeConsensus)
 *  public void handleTransaction(ExoMessage message, ExoState state)
 *  
 *  is intended to respond to both executePreConsensus and executeConsensus events.
 *  
 *  In the platform, we can now define routers for each event:
 *  
 *  ExoParameterizedRouter<ExoHandler, PlatformEvents> preConsensusRouter = 
 *  	new ExoParameterizedRouter<ExoHandler, PlatformEvents>(PlatformEvents.executePreConsensus);
 *  
 *  ExoParameterizedRouter<ExoHandler, PlatformEvents> consensusRouter = 
 *  	new ExoParameterizedRouter<ExoHandler, PlatformEvents>(PlatformEvents.executeConsensus);
 *   
 *  will instantiate a routers that can pick up each decorator on our handleTransaction() method defined above.
 * 
 * @author craigdrabik
 *
 * @param <T>
 * @param <E>
 */
//public class ExoParameterizedRouter<T extends Annotation, E extends Enum<E>> extends ExoRouter<T> {
public class ExoParameterizedRouter<E extends Enum<E>> {
	
	protected E event;
	protected Class<? extends Annotation> annotationType;
	
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
	
	/**
	 * No-op constructor.  ExoTransactionRouter will be instantiated by 
	 * ExoPlatformLocator and TransactionServer, and managed by the platform.
	 * 
	 * Applications should not create instances of ExoTransactionRouter.
	 * 
	 * @see com.txmq.exo.core.ExoPlatformLocator
	 */
	public ExoParameterizedRouter(Class<? extends Annotation> annotationType, E event) {
		this.transactionMap = new HashMap<Integer, Method>();
		this.transactionProcessors = new HashMap<Class<?>, Object>(); 		
		this.annotationType = annotationType;
		this.event = event;
	}
	
	/**
	 * Scans a package, e.g. "com.txmq.exo.messaging.rest" for 
	 * @ExoHandler annotations using reflection and sets up the
	 * internal mapping of transaction type to processing method.
	 * 
	 * This method differs from ExoRouter's implementation by 
	 * checking the event value against the value supplied in 
	 * the constructor before adding the method to the router.
	 */
	@SuppressWarnings("unchecked")
	public ExoParameterizedRouter<E> addPackage(String transactionPackage) {
		System.out.println("Adding routes for " + event.name() + " in package " + transactionPackage);
		int methodsAdded = 0;
		Reflections reflections = new Reflections(transactionPackage, new MethodAnnotationsScanner());			
		
		Set<Method> methods = reflections.getMethodsAnnotatedWith(this.annotationType);
		for (Method method : methods) {
			try {
				Annotation[] methodAnnotations = method.getAnnotationsByType(this.annotationType);
				
				for (Annotation methodAnnotation : methodAnnotations) {
					Method transactionTypeMethod;
					Method eventTypeMethod;
					try {
						transactionTypeMethod = methodAnnotation.getClass().getMethod("transactionType");
						eventTypeMethod = methodAnnotation.getClass().getMethod("event");
						
						if (eventTypeMethod.getReturnType().equals(this.event.getClass())) {
							if (((E) eventTypeMethod.invoke(methodAnnotation)).equals(this.event)) {
								this.transactionMap.put((Integer) transactionTypeMethod.invoke(methodAnnotation), method);
								methodsAdded++;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new IllegalArgumentException(
								"The annotation " + this.annotationType.getName() + 
								" must implement a value() method that returns a type of int"
						);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Added " + methodsAdded + " routes (" + this.transactionMap.size() + " routes currently registerd)");
		return this;
	}
	
	public Serializable routeTransaction(ExoMessage<?> message, ExoState state) throws ReflectiveOperationException {
		return (Serializable) this.invokeHandler(message.transactionType.getValue(), message, state);
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
			System.out.println("Invoking " + event.name() + " handler for " + key);
			
			//Kind of a "safe hack" - If the length of the args lists differs between 
			//what we've been passed and what the function expects, just truncate.
			if (args.length > method.getParameterCount()) {
				return method.invoke(transactionProcessor, Arrays.copyOfRange(args, 0, method.getParameterCount()));
			} else {
				return method.invoke(transactionProcessor, args);
			}
		} 
		return null;
	}
}
