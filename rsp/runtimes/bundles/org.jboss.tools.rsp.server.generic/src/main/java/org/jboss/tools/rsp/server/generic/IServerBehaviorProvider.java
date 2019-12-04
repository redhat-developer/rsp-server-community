package org.jboss.tools.rsp.server.generic;

import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

/**
 * Maybe can be deleted?
 *
 */
public interface IServerBehaviorProvider {
	public IServerDelegate createServerDelegate(String typeId, IServer server);
}
