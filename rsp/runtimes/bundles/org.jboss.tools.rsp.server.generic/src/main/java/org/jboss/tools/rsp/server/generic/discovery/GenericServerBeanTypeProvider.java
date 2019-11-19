package org.jboss.tools.rsp.server.generic.discovery;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class GenericServerBeanTypeProvider implements IServerBeanTypeProvider{
	private ServerBeanType[] serverBeanTypes;
	public GenericServerBeanTypeProvider(ServerBeanType[] allTypes) {
		this.serverBeanTypes = allTypes;
	}
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return serverBeanTypes;
	}	

}
