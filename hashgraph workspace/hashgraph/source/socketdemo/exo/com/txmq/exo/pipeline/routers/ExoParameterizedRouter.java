package com.txmq.exo.pipeline.routers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.PlatformEvents;
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
public class ExoParameterizedRouter<T extends Annotation, E extends Enum<E>> extends ExoRouter<T> {
	
	protected E event;
	
	public ExoParameterizedRouter(E event) {
		super();
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
	@Override
	public ExoRouter<T> addPackage(String transactionPackage) {
		Reflections reflections = new Reflections(transactionPackage, new MethodAnnotationsScanner());			
		
		Set<Method> methods = reflections.getMethodsAnnotatedWith(this.annotationType);
		for (Method method : methods) {
			@SuppressWarnings("unchecked")
			T[] methodAnnotations = (T[]) method.getAnnotationsByType(this.annotationType);
			
			for (T methodAnnotation : methodAnnotations) {
				Method transactionTypeMethod;
				Method eventTypeMethod;
				try {
					transactionTypeMethod = this.annotationType.getMethod("transactionType", (Class<?>[]) null);
					eventTypeMethod = this.annotationType.getMethod("event", (Class<?>[]) null);
					if (((PlatformEvents) eventTypeMethod.invoke(methodAnnotation)).equals(this.event)) {
						this.transactionMap.put((Integer) transactionTypeMethod.invoke(methodAnnotation), method);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new IllegalArgumentException(
							"The annotation " + this.annotationType.getName() + 
							" must implement a value() method that returns a type of int"
					);
				}
			}
		}
		
		return this;
	}
	
	public Serializable routeTransaction(ExoMessage<?> message, ExoState state) throws ReflectiveOperationException {
		return (Serializable) this.invokeHandler(message.transactionType.getValue(), message, state);
	}
}
