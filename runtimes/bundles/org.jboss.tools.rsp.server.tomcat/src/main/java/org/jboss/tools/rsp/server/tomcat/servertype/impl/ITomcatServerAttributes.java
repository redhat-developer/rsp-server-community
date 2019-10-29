package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface ITomcatServerAttributes extends DefaultServerAttributes {
	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;
	
	/*
	 * Optional attributes
	 */
	public static final String TOMCAT_SERVER_HOST = "tomcat.server.host";
	public static final String TOMCAT_SERVER_HOST_DEFAULT = "localhost";
	public static final String TOMCAT_SERVER_PORT = "tomcat.server.port";
	public static final int TOMCAT_SERVER_PORT_DEFAULT = 8080;
}
