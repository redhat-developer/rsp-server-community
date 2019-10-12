package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import java.util.HashMap;
import java.util.Map;

import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class TomcatServerTypes implements ServerTypeStringConstants{
	
	public static final IServerType TC9_SERVER_TYPE = 
			new TomcatServerType(TC9_ID, TC9_NAME, TC9_DESC);

	public static final Map<String, String> RUNTIME_TO_SERVER = new HashMap<>();
	static {
		RUNTIME_TO_SERVER.put(RUNTIME_TC_9_ID, TC9_ID);		
	};
}
