package com.txmq.exo.pipeline.routers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.messaging.websocket.grizzly.ExoMessageJsonParser;
import com.txmq.exo.pipeline.metadata.ExoNullPayloadType;

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
	protected Map<Integer, List<Method>> transactionMap;

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
		this.transactionMap = new HashMap<Integer, List<Method>>();
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
		Reflections reflections = new Reflections(transactionPackage, new MethodAnnotationsScanner());			
		Set<Method> methods = reflections.getMethodsAnnotatedWith(this.annotationType);
		for (Method method : methods) {
			try {
				Annotation[] methodAnnotations = method.getAnnotationsByType(this.annotationType);
				
				for (Annotation methodAnnotation : methodAnnotations) {
					Method transactionTypeMethod;
					Method eventTypesMethod;
					Method payloadTypeMethod;
					
					try {
						transactionTypeMethod = methodAnnotation.getClass().getMethod("transactionType");
						eventTypesMethod = methodAnnotation.getClass().getMethod("events");
						payloadTypeMethod = null;
						
						try {
							payloadTypeMethod = methodAnnotation.getClass().getMethod("payloadClass");
						} catch (NoSuchMethodException e) {
							//No problem, we check for nulls later on
						}
						
						E[] eventTypes = (E[]) eventTypesMethod.invoke(methodAnnotation);
						for (E eventType : eventTypes) {
							//Add a mapping from this transaction type to its processor 
							//if the event matches the event this instance tracks.  
							if (eventType.equals(this.event)) {
								Integer transactionType = (Integer) transactionTypeMethod.invoke(methodAnnotation);
								if (!this.transactionMap.containsKey(transactionType)) {
									this.transactionMap.put(transactionType, new ArrayList<Method>());
								}
								this.transactionMap.get(transactionType).add(method);

								//Add a mapping from transaction type to its payload if the payload isn't empty.
								//We use ExoNullPayloadType as a placeholder for an empty payload in annotations
								if (payloadTypeMethod != null) {
									Class<?> payloadType = (Class<?>) payloadTypeMethod.invoke(methodAnnotation);
									if (!payloadType.equals(ExoNullPayloadType.class)) {
										ExoMessageJsonParser.registerPayloadType(transactionType, payloadType);
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw new IllegalArgumentException(
								"The annotation " + this.annotationType.getName() + 
								" returned an unexpected event type or value"
						);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public Serializable routeTransaction(ExoMessage<?> message, ExoState state) throws ReflectiveOperationException {
		return this.invokeHandler(message.transactionType.getValue(), message, state);
	}
	
	public boolean hasRouteForTransactionType(ExoTransactionType transactionType) {
		return this.transactionMap.containsKey(transactionType.getValue());
	}
	
	protected Serializable invokeHandler(int key, Object... args) throws ReflectiveOperationException {
		Serializable result = null;;
		if (this.transactionMap.containsKey(key)) {
			List<Method> methods = this.transactionMap.get(key); 
			for (Method method : methods) { 
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
					/*
					 * Also kind of a "safe hack"..  We should only have one handler (processor) for platform events, 
					 * while notifications may have multiple handlers, but we don't care about the results of those 
					 * handlers.  Thus, it's safe to just return the last value we get from a processor.  It'll either 
					 * be the only one, or irrelevant.
					 */
					result = (Serializable) method.invoke(transactionProcessor, Arrays.copyOfRange(args, 0, method.getParameterCount()));
				} else {
					result = (Serializable) method.invoke(transactionProcessor, args);
				}
			}
		} 
		return result;
	}
}
