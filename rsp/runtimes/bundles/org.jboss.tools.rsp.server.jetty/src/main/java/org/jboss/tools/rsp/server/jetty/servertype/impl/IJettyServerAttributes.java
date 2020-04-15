/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.jetty.servertype.impl;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface IJettyServerAttributes extends DefaultServerAttributes {
	public static final String JETTY_9X_SERVER_TYPE_ID = "org.jboss.ide.eclipse.as.server.jetty.9x";
	

	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;
	
	/*
	 * Optional
	 */
	public static final String JETTY_BASEDIR = "jetty.base.dir";
	public static final String JETTY_HOST = "jetty.host";
	public static final String JETTY_PORT = "jetty.port";
	
	
}
