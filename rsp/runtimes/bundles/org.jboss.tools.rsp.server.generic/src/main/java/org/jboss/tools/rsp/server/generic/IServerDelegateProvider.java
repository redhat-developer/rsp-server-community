package org.jboss.tools.rsp.server.generic;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public interface IServerDelegateProvider {
	public IServerDelegate createServerDelegate(String typeId, IServer server);
}
