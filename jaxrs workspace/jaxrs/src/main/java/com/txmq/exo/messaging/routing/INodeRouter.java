package com.txmq.exo.messaging.routing;

import java.net.SocketAddress;
import java.util.List;

public interface INodeRouter {
	public void setAvailableNodes(List<SocketAddress> nodes);
	public SocketAddress getNode();
}
