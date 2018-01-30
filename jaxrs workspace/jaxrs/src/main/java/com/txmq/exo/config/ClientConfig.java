package com.txmq.exo.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ClientConfig {
	public boolean encrypted;
	public KeystoreConfig clientKeystore;
	public KeystoreConfig serverKeystore;
	public List<NodeAddress> knownSockets;
	
	public List<SocketAddress> getKnownSockets() {
		ArrayList<SocketAddress> result = new ArrayList<SocketAddress>();
		for (NodeAddress address : this.knownSockets) {
			result.add(new InetSocketAddress(address.hostname, address.port));
		}
		
		return result;
	}
}
