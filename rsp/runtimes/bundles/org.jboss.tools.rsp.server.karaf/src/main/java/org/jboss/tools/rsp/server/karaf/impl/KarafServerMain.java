package org.jboss.tools.rsp.server.karaf.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;
import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;

public class KarafServerMain extends ServerManagementServerLauncher {

	public static void main(String[] args) throws Exception {
		KarafServerMain instance = new KarafServerMain(args[0]);
		LauncherSingleton.getDefault().setLauncher(instance);
		instance.launch();
		instance.shutdownOnInput();
	}
	
	public KarafServerMain(String string) {
		super(string);
	}
	
	@Override
	public void launch(int port) throws Exception {
		GenericServerExtensionModel model = new GenericServerExtensionModel(
				serverImpl.getModel(), Activator.getDelegateProviderImpl(), Activator.getServerTypeModelStreamImpl());
		model.registerExtensions();
		super.launch(port);
	}

}
