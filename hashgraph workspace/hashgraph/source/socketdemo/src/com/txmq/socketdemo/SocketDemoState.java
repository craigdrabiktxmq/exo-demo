package com.txmq.socketdemo;

/*
 * This file is public domain.
 *
 * SWIRLDS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF 
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SWIRLDS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING OR 
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.swirlds.platform.Address;
import com.swirlds.platform.AddressBook;
import com.swirlds.platform.FCDataInputStream;
import com.swirlds.platform.FCDataOutputStream;
import com.swirlds.platform.FastCopyable;
import com.swirlds.platform.Platform;
import com.swirlds.platform.SwirldState;
import com.txmq.exo.core.ExoState;


/**
 * This holds the current state of the swirld. For this simple "hello swirld" code, each transaction is just
 * a string, and the state is just a list of the strings in all the transactions handled so far, in the
 * order that they were handled.
 */
public class SocketDemoState extends ExoState implements SwirldState {
	
	
	/**
	 * The zoo consists of a number of lions, tigers, and bears pushed into the zoo by users
	 */
	private List<String> lions = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getLions() {
		return lions;
	}

	public synchronized void addLion(String name) {
		this.lions.add(name);
	}
	
	private List<String> tigers = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getTigers() {
		return tigers;
	}

	public synchronized void addTiger(String name) {
		this.tigers.add(name);
	}
	
	private List<String> bears = Collections
			.synchronizedList(new ArrayList<String>());

	/** @return all the strings received so far from the network */
	public synchronized List<String> getBears() {
		return bears;
	}
	
	public synchronized void addBear(String name) {
		this.bears.add(name);
	}

	// ///////////////////////////////////////////////////////////////////

	@Override
	public synchronized AddressBook getAddressBookCopy() {
		return addressBook.copy();
	}

	@Override
	public synchronized FastCopyable copy() {
		SocketDemoState copy = new SocketDemoState();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public void copyTo(FCDataOutputStream outStream) {
		/*
		try {
			Utilities.writeStringArray(outStream,
					strings.toArray(new String[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	@Override
	public void copyFrom(FCDataInputStream inStream) {
		/*
		try {
			strings = new ArrayList<String>(
					Arrays.asList(Utilities.readStringArray(inStream)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	@Override
	public synchronized void copyFrom(SwirldState old) {
		super.copyFrom(old);
		lions = Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).lions));
		tigers = Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).tigers));
		bears= Collections.synchronizedList(new ArrayList<String>(((SocketDemoState) old).bears));
	}

	@Override
	public synchronized void handleTransaction(long id, boolean consensus,
			Instant timeCreated, byte[] transaction, Address address) {
		
		super.handleTransaction(id, consensus, timeCreated, transaction, address);		
	}

	@Override
	public void noMoreTransactions() {
	}

	@Override
	public synchronized void init(Platform platform, AddressBook addressBook) {
		super.init(platform, addressBook);
	}
}	