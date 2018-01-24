package com.txmq.exo.persistence.nulllogger;

import com.txmq.exo.messaging.ExoMessage;
import com.txmq.exo.persistence.Block;
import com.txmq.exo.persistence.IBlockLogger;

/**
 * A null implementation of a block logger.  Does nothing, but prevents 
 * Exo from falling down if applications don't need logging.
 * @author craigdrabik
 *
 */
public class NullBlockLogger implements IBlockLogger {

	@Override
	public void addTransaction(ExoMessage transaction) {
		return;
	}

	@Override
	public void save(Block block) {
		return;
	}

}
