/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.tomcat.servertype.impl;

public interface ITomcatRuntimeFlags {
	// Launch configuration constants / Command Line Args
	public static final String SPACE = " ";//$NON-NLS-1$
	public static final String DASH = "-"; //$NON-NLS-1$
	public static final String SYSPROP = "-D";//$NON-NLS-1$
	public static final String EQ = "="; //$NON-NLS-1$
	public static final String QUOTE = "\""; //$NON-NLS-1$
	public static final String FILE_COLON = "file:"; //$NON-NLS-1$
	
	public static final String JAVA_LOGGING_CONFIG_FILE = "java.util.logging.config.file"; //$NON-NLS-1$
	public static final String JAVA_LOGGING_MANAGER = "java.util.logging.manager"; //$NON-NLS-1$	
	public static final String JDK_TLS_KEYSIZE = "jdk.tls.ephemeralDHKeySize"; //$NON-NLS-1$
	public static final String JAVA_PROTOCOL_HANDLER_PKGS = "java.protocol.handler.pkgs"; //$NON-NLS-1$
	public static final String APACHE_CATALINA_SECURITY_LISTENER_UMASK = "org.apache.catalina.security.SecurityListener.UMASK"; //$NON-NLS-1$
	public static final String IGNORE_ENDORSED_DIRS = "ignore.endorsed.dirs"; //$NON-NLS-1$
	
	

}
