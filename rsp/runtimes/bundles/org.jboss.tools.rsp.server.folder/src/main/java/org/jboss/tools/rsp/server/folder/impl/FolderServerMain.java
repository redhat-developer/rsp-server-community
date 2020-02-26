package org.jboss.tools.rsp.server.folder.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerManagementServerLauncher;
import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;

public class FolderServerMain extends ServerManagementServerLauncher {

	public static void main(String[] args) throws Exception {
		FolderServerMain instance = new FolderServerMain(args[0]);
		LauncherSingleton.getDefault().setLauncher(instance);
		instance.launch();
		instance.shutdownOnInput();
	}
	
	public FolderServerMain(String string) {
		super(string);
	}
	
	@Override
	public void launch(int port) throws Exception {
		GenericServerExtensionModel folderModel = new GenericServerExtensionModel(
				serverImpl.getModel(), Activator.getDelegateProviderImpl(), Activator.getServerTypeModelStreamImpl());
		folderModel.registerExtensions();
		super.launch(port);
	}

}
