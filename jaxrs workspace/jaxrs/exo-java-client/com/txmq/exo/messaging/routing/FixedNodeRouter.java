package com.txmq.exo.messaging.routing;

import java.net.SocketAddress;
import java.util.List;
import java.util.Random;

/**
 * This is a very basic node selection mechanism that selects 
 * a node at random from the node list and returns that same 
 * node every time a node is requested.
 * @author craigdrabik
 *
 */
public class FixedNodeRouter implements INodeRouter {
	private List<SocketAddress> nodes;
	private SocketAddress stickyNode;
	
	@Override
	public void setAvailableNodes(List<SocketAddress> nodes) {
		this.nodes = nodes;
		this.stickyNode = nodes.get(new Random().nextInt(nodes.size()));
	}

	@Override
	public SocketAddress getNode() {
		return this.stickyNode;
	}

}
