package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class TomcatServerDelegate extends GenericServerBehavior {
	public TomcatServerDelegate(IServer server, JSONMemento behaviorMemento) {
		super(server, behaviorMemento);
	}
}
