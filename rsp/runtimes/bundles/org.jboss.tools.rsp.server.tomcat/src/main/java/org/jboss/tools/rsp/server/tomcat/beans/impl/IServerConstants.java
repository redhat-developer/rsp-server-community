package org.jboss.tools.rsp.server.tomcat.beans.impl;

import org.jboss.tools.rsp.launching.utils.FileUtil;

public interface IServerConstants {

	public static final String RUNTIME_TOMCAT_90 = "org.jboss.ide.eclipse.as.runtime.tomcat.90";
	
	public static final String ID_TOMCAT = "Tomcat";
	public static final String NAME_TOMCAT = "Tomcat Application Server";
	
	public static final String IMPLEMENTATION_TITLE = "Implementation-Title"; //$NON-NLS-1$
	
	public static final String LIB = "lib"; //$NON-NLS-1$
	public static final String CATALINA_JAR_NAME = "catalina.jar"; //$NON-NLS-1$
	public static final String LIB_TWIDDLE_PATH = FileUtil.asPath(LIB,CATALINA_JAR_NAME);
}
