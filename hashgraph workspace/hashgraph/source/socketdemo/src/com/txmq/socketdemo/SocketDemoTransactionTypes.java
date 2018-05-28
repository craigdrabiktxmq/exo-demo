package com.txmq.socketdemo;

import com.txmq.exo.messaging.ExoTransactionType;

public class SocketDemoTransactionTypes extends ExoTransactionType {
	public static final int GET_ZOO = 1;
	public static final int ADD_ANIMAL = 2;
	
	private static final int[] values = {
			GET_ZOO,
			ADD_ANIMAL
	};
	
	public SocketDemoTransactionTypes() {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
	}
	
	public SocketDemoTransactionTypes(int transactionType) {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
		
		this.setValue(transactionType);
	}
}
