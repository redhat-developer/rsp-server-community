/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;

/**
 * This class is for testing purposes until a definitive structure
 * can be decided upon. This just allows me to run the server with 
 * the wildfly enhancements added. 
 */
public class WonkaServerMain extends ServerManagementServerLauncher {
	public static void main(String[] args) throws Exception {
		WonkaServerMain instance = new WonkaServerMain();
		LauncherSingleton.getDefault().setLauncher(instance);
		instance.launch(args[0]);
		instance.shutdownOnInput();
	}
	
	@Override
	public void launch(int port) throws Exception {
		ExtensionHandler.addExtensions(serverImpl.getModel());
		super.launch(port);
	}
}
