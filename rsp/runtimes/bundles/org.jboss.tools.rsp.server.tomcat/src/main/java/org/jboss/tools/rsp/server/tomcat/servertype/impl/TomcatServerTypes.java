package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class TomcatServerTypes implements ServerTypeStringConstants{
	
	public static final IServerType TC9_SERVER_TYPE = 
			new TomcatServerType(TC9_ID, TC9_NAME, TC9_DESC);

}
