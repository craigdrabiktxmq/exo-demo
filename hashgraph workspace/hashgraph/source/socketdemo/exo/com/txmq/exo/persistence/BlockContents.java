package com.txmq.exo.persistence;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.txmq.exo.messaging.ExoMessage;

/**
 * Container for a block's transaction list and the hash of the previous block.
 * This object gets hashed to calculate the hash of the current block.
 */
class BlockContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3111636814943472713L;
	
	/**
	 * Hash of the previous block.  Blockchains ensure the integrity of the chain by 
	 * including the previous block's hash, and including that value in its own hash.
	 */
	public String previousBlockHash;
	public List<ExoMessage<?>> transactions = new ArrayList<ExoMessage<?>>();
	
	/**
	 * Calculates an SHA-256 hash of this object by first 
	 * serializing to JSON and then hashing that string.
	 */
	public String hash() {
		ObjectMapper mapper = new ObjectMapper();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance( "SHA-256" );
			String text = mapper.writeValueAsString(this);
		    md.update( text.getBytes( StandardCharsets.UTF_8 ) );
		    byte[] digest = md.digest();
		    return String.format( "%064x", new BigInteger( 1, digest ) );
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
