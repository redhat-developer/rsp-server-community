/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface ITomcatServerAttributes extends DefaultServerAttributes {
	public static final String TOMCAT_SERVER_TYPE_PREFIX = "org.jboss.ide.eclipse.as.server.tomcat.";
	public static final String TOMCAT_90_SERVER_TYPE_ID = "org.jboss.ide.eclipse.as.server.tomcat.90";
	

	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;

	/*
	 * Optional attributes
	 */
	public static final String SERVER_BASE_DIR = "server.base.dir";
	public static final String TOMCAT_SERVER_HOST = "tomcat.server.host";
	public static final String TOMCAT_SERVER_HOST_DEFAULT = "localhost";
	public static final String TOMCAT_SERVER_PORT = "tomcat.server.port";
	public static final int TOMCAT_SERVER_PORT_DEFAULT = 8080;
}
