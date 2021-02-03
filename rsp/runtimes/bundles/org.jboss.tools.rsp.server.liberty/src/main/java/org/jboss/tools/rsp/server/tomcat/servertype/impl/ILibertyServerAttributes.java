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

public interface ILibertyServerAttributes extends DefaultServerAttributes {
	public static final String LIBERTY_SERVER_TYPE_PREFIX = "org.jboss.ide.eclipse.as.server.liberty.";
	public static final String LIBERTY_210_SERVER_TYPE_ID = "org.jboss.ide.eclipse.as.server.liberty.210";
	

	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;
	
	/*
	 * Optional attributes
	 */
	public static final String LIBERTY_SERVER_HOST = "liberty.server.host";
	public static final String LIBERTY_SERVER_HOST_DEFAULT = "localhost";
	public static final String LIBERTY_SERVER_PORT = "server.http.port";
	public static final int LIBERTY_SERVER_PORT_DEFAULT = 9080;
}
