package com.txmq.exo.pipeline.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.txmq.exo.pipeline.PlatformEvents;

/**
 * This annotation is used to identify methods which receive and respond to Exo platform
 * events.  These methods typically implement your application's business logic at various
 * stages of the pipeline.
 * 
 * Methods decorated with this annotation must implement the following signature:
 * 
 * public Serializable onMessageReceived(ExoMessage<T> message, U state) throws ExoPipelineException;
 * 
 * Methods may optionally return Serializable results.  These results will be included in
 * the ExoMessage instance broadcast by the next reporting event in the pipeline.  For 
 * example, if your (executeConsensus) handler returns a value, then that value will be 
 * used as the payload for the message received by your (consensusResult) handler.
 * 
 * @author craigdrabik
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExoHandler {
	int transactionType();
	PlatformEvents[] events() default {};
	Class<?> payloadClass() default ExoNullPayloadType.class;
}
