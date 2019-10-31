package org.jboss.tools.rsp.server.felix.impl.discovery;

import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class FelixServerBeanTypeProvider implements IServerBeanTypeProvider{
	
	public static final ServerBeanType FELIX_60 = new FelixBean6x();
	public static final ServerBeanType[] KNOWN_TYPES = {
			FELIX_60
	};
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return KNOWN_TYPES;
	}	

}
