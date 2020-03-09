/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.launching.utils.FileUtil;

public interface ITomcatRuntimeResourceConstants {
	
	public static final String CONF = "conf"; //$NON-NLS-1$
	public static final String TEMP = "temp"; //$NON-NLS-1$
	public static final String TM_LOGGING_PROPS = "logging.properties"; //$NON-NLS-1$
	
	public static final String APACHE_JULI_CLASSLOADER_MANAGER = "org.apache.juli.ClassLoaderLogManager"; //$NON-NLS-1$
	public static final String APACHE_CATALINA_WEBRESOURCES = "org.apache.catalina.webresources"; //$NON-NLS-1$
	
	public static final String CATALINA_BASE = "catalina.base"; //$NON-NLS-1$
	public static final String CATALINA_HOME = "catalina.home"; //$NON-NLS-1$
	public static final String JAVA_IO_TMPDIR = "java.io.tmpdir"; //$NON-NLS-1$
	public static final String CATALINA_STARTUP_BOOTSTRAP = "org.apache.catalina.startup.Bootstrap"; //$NON-NLS-1$
	
	public static final String LIB = "lib"; //$NON-NLS-1$
	public static final String CATALINA_JAR_NAME = "catalina.jar"; //$NON-NLS-1$
	public static final String LIB_CATALINA_PATH = FileUtil.asPath(LIB,CATALINA_JAR_NAME);

	
}
