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


class BlockContents implements Serializable {
	public String previousBlockHash;
	public List<ExoMessage> transactions = new ArrayList<ExoMessage>();
	
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
