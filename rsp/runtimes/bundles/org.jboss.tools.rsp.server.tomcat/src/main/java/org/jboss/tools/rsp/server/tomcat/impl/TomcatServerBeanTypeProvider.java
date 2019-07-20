package org.jboss.tools.rsp.server.tomcat.impl;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class TomcatServerBeanTypeProvider implements IServerBeanTypeProvider{
	
	public static final ServerBeanType TOMCAT ;
	public static final ServerBeanType[] KNOWN_TYPES = {TOMCAT};
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return KNOWN_TYPES;
	}	

}
