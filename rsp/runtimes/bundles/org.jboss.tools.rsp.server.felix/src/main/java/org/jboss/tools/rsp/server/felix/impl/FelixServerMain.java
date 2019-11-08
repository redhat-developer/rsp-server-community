package org.jboss.tools.rsp.server.felix.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;

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
		ExtensionHandler.addExtensions(serverImpl.getModel());
		super.launch(port);
	}

}
