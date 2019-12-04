package org.jboss.tools.rsp.server.generic;

import org.jboss.tools.rsp.launching.memento.JSONMemento;

public interface IServerBehaviorFromJSONProvider {
	public IServerBehaviorProvider loadBehaviorFromJSON(String serverTypeId, JSONMemento behaviorMemento);
}
