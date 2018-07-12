package com.txmq.socketdemo;

import com.txmq.exo.messaging.ExoTransactionType;

public class ZooDemoTransactionTypes extends ExoTransactionType {
	public static final int GET_ZOO = 1;
	public static final int ADD_ANIMAL = 2;
	
	private static final int[] values = {
			GET_ZOO,
			ADD_ANIMAL
	};
	
	public ZooDemoTransactionTypes() {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
	}
	
	public ZooDemoTransactionTypes(int transactionType) {
		super();
		if (getInitialized() == false) {
			initialize(values);
		}
		
		this.setValue(transactionType);
	}
}
