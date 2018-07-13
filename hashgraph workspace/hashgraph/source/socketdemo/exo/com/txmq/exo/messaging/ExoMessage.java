package com.txmq.exo.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.txmq.exo.core.ExoPlatformLocator;

/**
 * ExoMessage is the base wrapper for transactions that come in through Exo. 
 * The framework is designed to work with instances of ExoMessage.  It is
 * unknown if it will work with subclasses of ExoMessage (likely not),
 * but it should be flexible enough for most cases.
 * 
 * It takes an ExoTrasnactionType ((which can and should be subclassed)) and
 * a payload, which can be anything that implements Serializable.  Your payload
 * can be designed in any application-specific way, Exo doesn't care.
 * 
 * @see com.txmq.exo.messaging.socket.ExoTransactionType
 * 
 * @param <T> the type of the input parameter carrier class.  Must implement Serializable.
 * @param <U> the type of the output carrier class.  Must implement Serializable.
 * 
 */
public class ExoMessage<T extends Serializable> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -543276168606092942L;

	/**
	 * The type of transaction this represents
	 */
	public ExoTransactionType transactionType;

	/**
	 * The business data associated with this transaction.  
	 * It can be anything, as long as it's serializable.
	 */
	public T payload;
	
	/**
	 * Unique identifier.
	 */
	public UUID uuid;

	/**
	 * Indicates that this transaction has been interrupted.  This transaction will 
	 * cease moving through the pipeline and go straight to the completed state.
	 */
	private boolean interrupted = false;
	
	/**
	 * Tests if this transaction has been interrupted.
	 * @return
	 */
	public boolean isInterrupted() {
		return this.interrupted;
	}
	
	/**
	 * Interrupts this transaction.
	 */
	public void interrupt() {
		this.interrupted = true;
	}
	
	/**
	 * Inserts the transaction into the pipeline, beginning processing
	 */
	public void submit() throws IOException {
		ExoPlatformLocator.createTransaction(this);
	}
	
	public ExoMessage() {
		super();
		this.transactionType = new ExoTransactionType();
		this.uuid = UUID.randomUUID();
	}
	
	/**
	 * Initialize this message with the supplied transaction type.
	 * @param transactionType
	 */
	public ExoMessage(ExoTransactionType transactionType) {
		super();
		this.transactionType = transactionType;	
		this.uuid = UUID.randomUUID();
	}
	
	/**
	 * Initialize this message with the supplied transaction type and payload.
	 * @param transactionType
	 */
	public ExoMessage(ExoTransactionType transactionType, T payload) {
		super();
		this.transactionType = transactionType;				
		this.payload = payload;
		this.uuid = UUID.randomUUID();
	}
	
	/**
	 * Serialize this transaction to a sequence of bytes
	 * 
	 * @return the sequence as a byte array
	 * @throws IOException
	 *             if anything goes wrong
	 */
	public byte[] serialize() throws IOException {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(this);
		o.close();
		return b.toByteArray();
	}

	/**
	 * Deserialize this file transaction from a sequence of bytes
	 *
	 * @param b
	 *            the sequence of bytes
	 * @return the file transaction
	 * @throws IOException
	 *             if anything goes wrong
	 * @throws ClassNotFoundException 
	 */
	public static ExoMessage<?> deserialize(byte[] b) throws IOException, ClassNotFoundException {
		ObjectInputStream o = new ObjectInputStream(new ByteArrayInputStream(b));
		ExoMessage<?> result = (ExoMessage<?>) o.readObject();
		o.close();
		
		return result;
	}	
}
