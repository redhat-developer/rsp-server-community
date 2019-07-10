package org.example.rsp.server.wonka.bundle.servertype.impl;

import java.io.File;

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
		return "com.example.wonka.App";
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
		File homeDir = new File(serverHome);
		File[] children = homeDir.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].getName().endsWith(".jar")) {
				IPath jar = new Path(serverHome).append(children[i].getName());
				return new String[] { jar.toOSString() };
			}
		}
		return new String[] {};
	}
}
