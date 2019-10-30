package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.tomcat.servertype.launch.IDefaultLaunchArguments;
import org.jboss.tools.rsp.server.tomcat.servertype.launch.TomcatDefaultLaunchArguments;

public class TomcatShutdownLauncher extends TomcatStartLauncher implements IServerShutdownLauncher {

	public TomcatShutdownLauncher(IServerDelegate serverDelegate) {
		super(serverDelegate);
	}

	@Override
	public ILaunch launch(boolean force) throws CoreException {
		return super.launch("run");
	}
	@Override
	protected String getProgramArguments() {
		IDefaultLaunchArguments largs = new TomcatDefaultLaunchArguments(getServer());
		String pmArgs = largs.getDefaultStopArgs();
		return pmArgs;
	}

}
