package com.txmq.socketdemo.transactions;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.handlers.ExoTransactionHandler;
import com.txmq.socketdemo.SocketDemoState;

import io.swagger.model.Animal;

public class AddAnimalTransactionHandler implements ExoTransactionHandler<Animal, Animal, SocketDemoState> {

	@Override
	public void onExecutePreConsensus(ExoMessage<Animal, Animal> message, SocketDemoState state) {
		message.result = this.addToZoo(message.payload, state);		
	}

	@Override
	public void onExecuteConsensus(ExoMessage<Animal, Animal> message, SocketDemoState state) {
		message.result = this.addToZoo(message.payload, state);		
	}
	
	private Animal addToZoo(Animal animal, SocketDemoState state) {
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
