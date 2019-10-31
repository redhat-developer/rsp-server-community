package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class TomcatServerTypes {
	public static final String TC9_ID = IServerConstants.TOMCAT_90_SERVER_TYPE_ID;
	public static final String TC9_NAME = "Tomcat 9.x";
	public static final String TC9_DESC = "A server adapter capable of discovering and controlling a Tomcat 9.x runtime instance.";
	

	public static final IServerType TC9_SERVER_TYPE = 
			new TomcatServerType(TC9_ID, TC9_NAME, TC9_DESC);

}
