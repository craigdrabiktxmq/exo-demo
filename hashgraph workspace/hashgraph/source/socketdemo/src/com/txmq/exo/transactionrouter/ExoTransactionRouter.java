package com.txmq.exo.transactionrouter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.socketdemo.SocketDemoState;

public class ExoTransactionRouter {
	
	private Map<String, Method> transactionMap;
	private Map<Class<?>, Object> transactionProcessors;
	
	public ExoTransactionRouter() {
		this.transactionMap = new HashMap<String, Method>();
		this.transactionProcessors = new HashMap<Class<?>, Object>(); 
	}
	
	public ExoTransactionRouter addPackage(String transactionPackage) {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
		     .setUrls(ClasspathHelper.forPackage(transactionPackage))
		     .setScanners(new MethodAnnotationsScanner())
		);
		
		Set<Method> methods = reflections.getMethodsAnnotatedWith(ExoTransaction.class);
		for (Method method : methods) {
			ExoTransaction annotation = method.getAnnotation(ExoTransaction.class);
			this.transactionMap.put(annotation.value(), method);
		}
		
		return this;
	}
	
	//TODO:  Refactor state so exo stuff is already in there in a superclass
	public void routeTransaction(ExoMessage message, SocketDemoState state) throws ReflectiveOperationException { 
		if (this.transactionMap.containsKey(message.transactionType.getValue())) {
			Method method = this.transactionMap.get(message.transactionType.getValue());
			Class<?> processorClass = method.getDeclaringClass();			
			if (!this.transactionProcessors.containsKey(processorClass)) {
				Constructor<?> processorConstructor = processorClass.getConstructor();
				this.transactionProcessors.put(processorClass, processorConstructor.newInstance());				
			}
			
			Object transactionProcessor = this.transactionProcessors.get(processorClass);
			method.invoke(transactionProcessor, message, state );
		}
	}
}
