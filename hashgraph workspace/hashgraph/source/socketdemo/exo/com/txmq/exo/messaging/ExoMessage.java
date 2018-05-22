package com.txmq.exo.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

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
 */
public class ExoMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -543276168606092941L;

	/**
	 * The type of transaction this represents
	 */
	public ExoTransactionType transactionType;

	/**
	 * The business data associated with this transaction.  
	 * It can be anything, as long as it's serializable.
	 */
	public Serializable payload;

	/**
	 * Hash of a unique identifier.
	 * 
	 * TODO:  Is this still needed?  This may be leftover from debugging the CouchDB block logger
	 */
	public int uuidHash;

	
	public ExoMessage() {
		super();
		this.transactionType = new ExoTransactionType();
		this.uuidHash = UUID.randomUUID().hashCode();
	}
	
	/**
	 * Initialize this message with the supplied transaction type.
	 * @param transactionType
	 */
	public ExoMessage(ExoTransactionType transactionType) {
		super();
		this.transactionType = transactionType;	
		this.uuidHash = UUID.randomUUID().hashCode();
	}
	
	/**
	 * Initialize this message with the supplied transaction type and payload.
	 * @param transactionType
	 */
	public ExoMessage(ExoTransactionType transactionType, Serializable payload) {
		super();
		this.transactionType = transactionType;				
		this.payload = payload;
		this.uuidHash = UUID.randomUUID().hashCode();
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
	public static ExoMessage deserialize(byte[] b) throws IOException, ClassNotFoundException {
		ObjectInputStream o = new ObjectInputStream(new ByteArrayInputStream(b));
		ExoMessage result = (ExoMessage) o.readObject();
		o.close();
		
		return result;
	}
}
