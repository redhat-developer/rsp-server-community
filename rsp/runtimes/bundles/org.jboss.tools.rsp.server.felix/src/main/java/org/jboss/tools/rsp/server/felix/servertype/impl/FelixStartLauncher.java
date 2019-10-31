package org.jboss.tools.rsp.server.felix.servertype.impl;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FelixStartLauncher extends AbstractJavaLauncher implements IServerStartLauncher {
	
	private static final Logger LOG = LoggerFactory.getLogger(FelixStartLauncher.class);
	
	public FelixStartLauncher(IServerDelegate serverDelegate) {
		super(serverDelegate);
	}
	
	@Override
	protected String getWorkingDirectory() {
		String serverHome =  getServer().getAttribute(IFelixServerAttributes.SERVER_HOME, (String) null);
		return serverHome + "/bin";
	}

	@Override
	protected String getMainTypeName() {
		return "org.apache.felix.main.Main";
	}

	@Override
	protected String getVMArguments() {
		return "";
	}

	@Override
	protected String getProgramArguments() {
		return "";
	}
	
	@Override
	protected String[] getClasspath() {
		String serverHome = getServer().getAttribute(IFelixServerAttributes.SERVER_HOME, (String) null);
		String felixJar = serverHome + "/bin/felix.jar";
		return new String[] {felixJar};
	}

	@Override
	protected IVMInstallRegistry getDefaultRegistry() {
		IVMInstallRegistry registry = null;
		if (LauncherSingleton.getDefault() != null
				&& LauncherSingleton.getDefault().getLauncher() != null
				&& LauncherSingleton.getDefault().getLauncher().getModel() != null) {
					registry = LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
		}
		return registry;
	}
	
}
