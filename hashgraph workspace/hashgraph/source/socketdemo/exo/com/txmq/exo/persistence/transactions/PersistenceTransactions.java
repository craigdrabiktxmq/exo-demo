package com.txmq.exo.persistence.transactions;

import com.txmq.exo.core.ExoPlatformLocator;
import com.txmq.exo.core.ExoState;
import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.messaging.ExoTransactionType;
import com.txmq.exo.pipeline.PlatformEvents;
import com.txmq.exo.pipeline.metadata.ExoHandler;

public class PersistenceTransactions {

	@ExoHandler(transactionType=ExoTransactionType.SHUTDOWN, events= {PlatformEvents.executeConsensus})
	public ExoMessage<?> shutdown(ExoMessage<?> message, ExoState state, boolean consensus) {
		//If we have a block logger, then ask it to flush to the chain.
		if (ExoPlatformLocator.getBlockLogger() != null) {
			ExoPlatformLocator.shutdown();	
			System.out.println("It is now safe to shut down.");
		}
		
		return message;
	}
	
	@ExoHandler(transactionType=ExoTransactionType.RECOVER_STATE, events= {PlatformEvents.executeConsensus})
	public ExoMessage<?> recoverState(ExoMessage<?> message, ExoState state, boolean consensus) {
		return message;
	}
}
