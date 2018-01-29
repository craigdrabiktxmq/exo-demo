package com.txmq.exo.messaging.routing;

import java.net.SocketAddress;
import java.util.List;

public class RoundRobinNodeRouter implements INodeRouter {

	private List<SocketAddress> nodes;
	private int currentNodePointer;
	@Override
	public void setAvailableNodes(List<SocketAddress> nodes) {
		this.nodes = nodes;
	}

	@Override
	public SocketAddress getNode() {
		this.currentNodePointer = (this.currentNodePointer + 1) % this.nodes.size();
		return this.nodes.get(this.currentNodePointer);
	}

}
