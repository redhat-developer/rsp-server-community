package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.tomcat.servertype.launch.TomcatDefaultLaunchArguments;
import org.jboss.tools.rsp.server.tomcat.servertype.launch.IDefaultLaunchArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatStartLauncher extends AbstractJavaLauncher implements IServerStartLauncher {
	
	private static final Logger LOG = LoggerFactory.getLogger(TomcatStartLauncher.class);
	
	public TomcatStartLauncher(IServerDelegate serverDelegate) {
		super(serverDelegate);
	}
	
	@Override
	protected String getWorkingDirectory() {
		String serverHome =  getServer().getAttribute(ITomcatServerAttributes.SERVER_HOME, (String) null);
		return serverHome + "/bin";
	}

	@Override
	protected String getMainTypeName() {
		return "org.apache.catalina.startup.Bootstrap";
	}

	@Override
	protected String getVMArguments() {		
		IDefaultLaunchArguments largs = new TomcatDefaultLaunchArguments(getServer());
		String serverHome = getServer().getAttribute(ITomcatServerAttributes.SERVER_HOME, (String) null);
		String vmArgs = largs.getStartDefaultVMArgs(new Path(serverHome));
		return vmArgs;
	}

	@Override
	protected String getProgramArguments() {
		IDefaultLaunchArguments largs = new TomcatDefaultLaunchArguments(getServer());
		String serverHome = getServer().getAttribute(ITomcatServerAttributes.SERVER_HOME, (String) null);
		String pmArgs = largs.getStartDefaultProgramArgs(new Path(serverHome));
		return pmArgs;
	}
	
	@Override
	protected String[] getClasspath() {
		String serverHome = getServer().getAttribute(ITomcatServerAttributes.SERVER_HOME, (String) null);
		String bootstrapModule = serverHome + "/bin/bootstrap.jar";
		String tcjuliModule = serverHome + "/bin/tomcat-juli.jar";
		return new String[] { bootstrapModule, tcjuliModule };
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
