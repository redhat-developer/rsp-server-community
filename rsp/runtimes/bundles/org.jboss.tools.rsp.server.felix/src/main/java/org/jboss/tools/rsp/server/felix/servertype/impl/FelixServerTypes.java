package org.jboss.tools.rsp.server.felix.servertype.impl;

import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class FelixServerTypes {
	public static final String FELIX6x_ID = IServerConstants.FELIX_6X_SERVER_TYPE_ID;
	public static final String FELIX6x_NAME = "Apache Felix 6.x";
	public static final String FELIX6x_DESC = "A server adapter capable of discovering and controlling an Apache Felix 6.x OSGi Container.";
	

	public static final IServerType FELIX_60_SERVER_TYPE = 
			new FelixServerType(FELIX6x_ID, FELIX6x_NAME, FELIX6x_DESC);

}
