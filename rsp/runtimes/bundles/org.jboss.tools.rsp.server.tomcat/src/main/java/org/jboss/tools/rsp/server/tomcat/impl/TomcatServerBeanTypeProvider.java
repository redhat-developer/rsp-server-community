package org.jboss.tools.rsp.server.tomcat.impl;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.tomcat.beans.impl.TomcatBean9;

public class TomcatServerBeanTypeProvider implements IServerBeanTypeProvider{
	
	public static final ServerBeanType TOMCAT90 = new TomcatBean9();
	public static final ServerBeanType[] KNOWN_TYPES = {
			TOMCAT90
	};
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return KNOWN_TYPES;
	}	

}
