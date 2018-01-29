package com.txmq.exo.messaging.routing;

import java.net.SocketAddress;
import java.util.List;
import java.util.Random;

public class RandomNodeRouter implements INodeRouter {

	private List<SocketAddress> nodes;
	private Random random = new Random();
	@Override
	public void setAvailableNodes(List<SocketAddress> nodes) {
		this.nodes = nodes;
	}


	@Override
	public SocketAddress getNode() {
		return this.nodes.get(this.random.nextInt(this.nodes.size()));
	}

}
