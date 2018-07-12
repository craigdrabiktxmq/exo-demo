package com.txmq.socketdemo.transactions;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.pipeline.PlatformEvents;
import com.txmq.exo.pipeline.metadata.ExoHandler;
import com.txmq.socketdemo.SocketDemoState;
import com.txmq.socketdemo.ZooDemoTransactionTypes;

import io.swagger.model.Animal;
import io.swagger.model.Zoo;

public class ZooTransactions {

	@ExoHandler(transactionType=ZooDemoTransactionTypes.ADD_ANIMAL, 
				events={PlatformEvents.executePreConsensus, PlatformEvents.executeConsensus},
				payloadClass=Animal.class)
	public void addAnimal(ExoMessage<Animal> message, SocketDemoState state) {
		//todo:  improve this so that we're testing if an animal of the same name exists, and failing if so 
		Animal animal = message.payload;
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
	
	@ExoHandler(transactionType=ZooDemoTransactionTypes.GET_ZOO, 
				events={PlatformEvents.messageReceived})
	public Zoo getZoo(ExoMessage<?> message, SocketDemoState state) {
		Zoo zoo = new Zoo();
		zoo.setLions(state.getLions());
		zoo.setTigers(state.getTigers());
		zoo.setBears(state.getBears());
		
		message.interrupt();
		return zoo;
	}
}
