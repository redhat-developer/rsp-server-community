/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.servertype.impl;

public class WonkaServerTypes {

	public static final String WONKA_SERVER_1_0_ID = "wonka.1.0";
	public static final String WONKA_SERVER_1_0_NAME = "Wonka Server 1.0";
	public static final String WONKA_SERVER_1_0_DESC = "A server adapter for Wonka Server 1.0 or greater";
	
	public static final WonkaServerType WONKA_1_0_TYPE = 
			new WonkaServerType(WONKA_SERVER_1_0_ID, WONKA_SERVER_1_0_NAME, WONKA_SERVER_1_0_DESC);
}
