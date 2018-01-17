package com.txmq.socketdemo;

import com.txmq.exo.messaging.ExoTransactionType;

public class SocketDemoTransactionTypes extends ExoTransactionType {
	public static final String GET_ZOO = "GET_ZOO";
	public static final String ADD_ANIMAL = "ADD_ANIMAL";
	
	private static final String[] values = {
			GET_ZOO,
			ADD_ANIMAL
	};
	
	public SocketDemoTransactionTypes() {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
	}
	
	public SocketDemoTransactionTypes(String transactionType) {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
		
		this.setValue(transactionType);
	}
}
