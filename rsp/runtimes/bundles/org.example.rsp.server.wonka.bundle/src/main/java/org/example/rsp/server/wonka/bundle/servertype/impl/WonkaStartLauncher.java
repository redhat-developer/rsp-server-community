package org.example.rsp.server.wonka.bundle.servertype.impl;

import org.example.rsp.server.wonka.bundle.servertype.SimpleVMRegistryDiscovery;
import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class WonkaStartLauncher extends AbstractJavaLauncher {

	public WonkaStartLauncher(IServerDelegate serverDelegate) {
		super(serverDelegate);
	}

	@Override
	protected IVMInstallRegistry getDefaultRegistry() {
		return new SimpleVMRegistryDiscovery().getDefaultRegistry();
	}

	@Override
	protected String getWorkingDirectory() {
		return getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String) null);	
	}

	@Override
	protected String getMainTypeName() {
		return "org.example.wonka.StartWonka";
	}

	@Override
	protected String getVMArguments() {
		return ""; // No args
	}

	@Override
	protected String getProgramArguments() {
		return ""; // No args
	}

	@Override
	protected String[] getClasspath() {
		String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String) null);
		IPath jar = new Path(serverHome).append("runWonka.jar");
		return new String[] { jar.toOSString() };
	}
}
