/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.payara.servertype.impl;

import org.jboss.tools.rsp.api.DefaultServerAttributes;

public interface IPayaraServerAttributes extends DefaultServerAttributes {
	public static final String PAYARA_5X_SERVER_TYPE_ID = "org.jboss.ide.eclipse.as.server.payara.5x";
	

	/*
	 * Required attributes
	 */
	public static final String SERVER_HOME = DefaultServerAttributes.SERVER_HOME_DIR;
	
	/*
	 * Optional
	 */
	public static final String PAYARA_HOST = "payara.host";
	public static final String PAYARA_PORT = "payara.port";
	
	
}
