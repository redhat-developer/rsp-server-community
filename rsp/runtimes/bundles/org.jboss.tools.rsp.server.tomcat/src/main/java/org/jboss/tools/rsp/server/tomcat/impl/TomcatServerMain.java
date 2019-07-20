package org.jboss.tools.rsp.server.tomcat.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;

public class TomcatServerMain extends ServerManagementServerLauncher {

	public static void main(String[] args) throws Exception {
		TomcatServerMain instance = new TomcatServerMain();
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
