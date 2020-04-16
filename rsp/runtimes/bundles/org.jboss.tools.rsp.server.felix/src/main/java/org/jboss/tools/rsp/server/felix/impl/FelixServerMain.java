/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.felix.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;
import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;

public class FelixServerMain extends ServerManagementServerLauncher {

	public static void main(String[] args) throws Exception {
		FelixServerMain instance = new FelixServerMain(args[0]);
		LauncherSingleton.getDefault().setLauncher(instance);
		instance.launch();
		instance.shutdownOnInput();
	}
	
	public FelixServerMain(String string) {
		super(string);
	}
	
	@Override
	public void launch(int port) throws Exception {
		GenericServerExtensionModel felixModel = new GenericServerExtensionModel(
				serverImpl.getModel(), Activator.getDelegateProviderImpl(), Activator.getServerTypeModelStreamImpl());
		felixModel.registerExtensions();
		super.launch(port);
	}

}
