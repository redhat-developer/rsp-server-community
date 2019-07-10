/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.servertype.impl;

import java.io.File;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.NullProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.Launch;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.StandardVMType;
import org.jboss.tools.rsp.foundation.core.launchers.CommandConfig;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.launchers.GenericServerProcessRunner;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class WonkaShutdownLauncher implements IServerShutdownLauncher {
	private IServerDelegate delegate;
	private ILaunch launch;
	private CommandLineDetails launchedDetails = null;
	private GenericServerProcessRunner runner;

	public WonkaShutdownLauncher(IServerDelegate msDelegate) {
		this.delegate = msDelegate;
	}

	public IServerDelegate getDelegate() {
		return delegate;
	}

	public IServer getServer() {
		return delegate.getServer();
	}

	public ILaunch launch(String mode) throws CoreException {
		getLaunchCommand(mode);
		configureRunner();
		launchedDetails = runner.runWithDetails(launch, new NullProgressMonitor());
		return launch;
	}

	public CommandLineDetails getLaunchCommand(String mode) throws CoreException {
		IStatus preReqs = checkPrereqs(mode);
		if (!preReqs.isOK())
			throw new CoreException(preReqs);

		launch = createLaunch(mode);
		configureRunner();
		launchedDetails = runner.getCommandLineDetails(launch, new NullProgressMonitor());
		return launchedDetails;
	}

	public CommandLineDetails getLaunchedDetails() {
		return launchedDetails;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	private ILaunch createLaunch(String mode) {
		return new Launch(this, mode, null);
	}

	protected IStatus checkPrereqs(String mode) {
		return Status.OK_STATUS;

	}

	public GenericServerProcessRunner configureRunner() {
		if (runner == null) {
			runner = new GenericServerProcessRunner(delegate, getCommandConfig());
		}
		return runner;
	}

	protected CommandConfig getCommandConfig() {
		IVMInstall vm = LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel().getDefaultVMInstall();
		File exe = StandardVMType.findJavaExecutable(vm.getInstallLocation());
		String wonkaJar = findWonkaJarPath();
		
		String cmd = exe.getAbsolutePath();
		String[] args = new String[] {
				"-jar",
				wonkaJar, 
				"shutdown"
		};
		String wd = getWorkingDirectory();
		String[] env = new String[] {};
		CommandConfig details = new CommandConfig(cmd, wd, args, env);
		return details;
	}
	
	private String findWonkaJarPath() {
		String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String) null);
		File homeDir = new File(serverHome);
		File[] children = homeDir.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].getName().endsWith(".jar")) {
				IPath jar = new Path(serverHome).append(children[i].getName());
				return jar.toOSString();
			}
		}
		return null;
	}


	public String getWorkingDirectory() {
		return getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,
				(String) null);
	}

	public ILaunch launch(boolean force) throws CoreException {
		return launch("run");
	}
}
