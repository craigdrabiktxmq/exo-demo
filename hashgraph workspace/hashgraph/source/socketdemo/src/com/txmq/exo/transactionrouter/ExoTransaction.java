package com.txmq.exo.transactionrouter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.txmq.exo.messaging.ExoTransactionType;;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExoTransaction {
	//Intended to be an enum value of an extension of ExoTransactionType
	String value();
}
