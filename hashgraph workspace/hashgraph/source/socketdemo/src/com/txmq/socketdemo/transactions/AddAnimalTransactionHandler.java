package com.txmq.socketdemo.transactions;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.handlers.ExoTransactionHandler;
import com.txmq.socketdemo.SocketDemoState;

import io.swagger.model.Animal;

public class AddAnimalTransactionHandler implements ExoTransactionHandler<Animal, SocketDemoState> {

	@Override
	public Animal onExecutePreConsensus(ExoMessage<Animal> message, SocketDemoState state) {
		return this.addToZoo(message.payload, state);		
	}

	@Override
	public Animal onExecuteConsensus(ExoMessage<Animal> message, SocketDemoState state) {
		return this.addToZoo(message.payload, state);		
	}
	
	private Animal addToZoo(Animal animal, SocketDemoState state) {
		//TODO:  Test if the animal is in state and throw an exception if so to demo handling errors.
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
		
		return animal;
	}

}
