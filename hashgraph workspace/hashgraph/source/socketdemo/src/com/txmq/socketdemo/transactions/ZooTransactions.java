package com.txmq.socketdemo.transactions;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.transactionrouter.ExoTransaction;
import com.txmq.socketdemo.SocketDemoState;
import com.txmq.socketdemo.SocketDemoTransactionTypes;

import io.swagger.model.Animal;

public class ZooTransactions {

	@ExoTransaction(SocketDemoTransactionTypes.ADD_ANIMAL)
	public void addAnimal(ExoMessage message, SocketDemoState state) {
		Animal animal = (Animal) message.payload;
		switch (animal.getSpecies()) {
			case "lion":
				state.addLion(animal.getName());
				break;
			case "tiger":
				state.addTiger(animal.getName());
				break;
			case "bear":
				state.addBear(animal.getName());
				break;
		}						
	}
}
