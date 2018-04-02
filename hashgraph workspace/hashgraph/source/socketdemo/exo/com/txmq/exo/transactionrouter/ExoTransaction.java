package com.txmq.exo.transactionrouter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates a method processes a certain transactiont ype.
 * The ExoTransactionRouter uses this annotation to automatically route
 * transactions by transaction type in the state's handleTransaction()
 * method.  ExoTransactionRouter scans packages for this annotation when
 * it is intialized.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExoTransaction {
	//Intended to be an enum value of an extension of ExoTransactionType
	String value();
}
