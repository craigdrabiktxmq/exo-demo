package com.txmq.exo.messaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExoTransactionType implements Serializable {
	public static final String ACKNOWLEDGE = "ACKNOWLEDGE";
	public static final String ANNOUNCE_NODE = "ANNOUNCE_NODE";
	
	private static List<String> transactionTypes;
	
	private static Boolean initialized = false;
	
	public static Boolean getInitialized() {
		return initialized;
	}

	protected static void initialize(String[] transactionTypes) {
		ExoTransactionType.transactionTypes = new ArrayList<String>();
		ExoTransactionType.transactionTypes.add(ACKNOWLEDGE);
		ExoTransactionType.transactionTypes.add(ANNOUNCE_NODE);
		
		for (String transactionType : transactionTypes) {
			ExoTransactionType.transactionTypes.add(transactionType);
		}
	}
		
	public ArrayList<String> getTransactionTypes() {
		return new ArrayList<>(ExoTransactionType.transactionTypes);
	}
	
	private String value;
	public String getValue() {
		return this.value;
	}
	
	public void setValue(String value) throws IllegalArgumentException {
		if (ExoTransactionType.transactionTypes.contains(value)) {
			this.value = value;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public ExoTransactionType() {
		
	}
	
	public ExoTransactionType(String value) {
		this.setValue(value);
	}
	
	public boolean equals(ExoTransactionType type) {
		return this.value.equals(type.value);
	}
}
