/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.glassfish.servertype.impl;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface IGlassfishServerAttributes extends DefaultServerAttributes {
	
	public static final String GLASSFISH_SERVER_TYPE_PREFIX = "org.jboss.ide.eclipse.as.server.glassfish.";

	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;
	
	/*
	 * Optional
	 */
	public static final String GLASSFISH_HOST = "glassfish.host";
	public static final String GLASSFISH_PORT = "glassfish.port";
	
	
}
