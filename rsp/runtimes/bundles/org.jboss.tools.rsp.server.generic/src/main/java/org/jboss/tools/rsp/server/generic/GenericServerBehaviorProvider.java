package org.jboss.tools.rsp.server.generic;

import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericServerBehaviorProvider implements IServerBehaviorProvider {
	private JSONMemento behaviorMemento;

	public GenericServerBehaviorProvider(JSONMemento behaviorMemento) {
		this.behaviorMemento = behaviorMemento;
	}

	@Override
	public IServerDelegate createServerDelegate(String typeId, IServer server) {
		return new GenericServerBehavior(server, behaviorMemento);
	}

}
