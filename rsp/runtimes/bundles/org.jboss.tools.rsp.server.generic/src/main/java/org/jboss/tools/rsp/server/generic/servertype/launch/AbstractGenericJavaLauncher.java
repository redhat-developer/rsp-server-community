/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype.launch;

import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public abstract class AbstractGenericJavaLauncher extends AbstractJavaLauncher implements IServerStartLauncher {

	public AbstractGenericJavaLauncher(IServerDelegate serverDelegate) {
		super(serverDelegate);
	}

	protected abstract String getWorkingDirectory();

	protected abstract String getMainTypeName();

	protected abstract String getVMArguments();

	protected abstract String getProgramArguments();

	protected abstract String[] getClasspath();

	protected boolean terminateProcesses(ILaunch launch) {
		if( launch == null )
			return false;
		
		IProcess[] all = launch.getProcesses();
		for( int i = 0; i < all.length; i++ ) {
			try {
				all[i].terminate();
			} catch(DebugException de) {
				return false;
			}
		}
		return true;
	}

	@Override
	public IVMInstallRegistry getDefaultRegistry() {
		IVMInstallRegistry registry = null;
		if (LauncherSingleton.getDefault() != null
				&& LauncherSingleton.getDefault().getLauncher() != null
				&& LauncherSingleton.getDefault().getLauncher().getModel() != null) {
					registry = LauncherSingleton.getDefault().getLauncher().getModel().getVMInstallModel();
		}
		return registry;
	}
}
