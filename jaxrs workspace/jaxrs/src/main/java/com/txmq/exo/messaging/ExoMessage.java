package com.txmq.exo.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

public class ExoMessage implements Serializable {
	public ExoTransactionType transactionType;
	public Serializable payload;
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
