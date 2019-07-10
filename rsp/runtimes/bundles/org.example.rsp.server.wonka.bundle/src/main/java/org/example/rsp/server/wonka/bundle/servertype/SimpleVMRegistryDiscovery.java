/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.servertype;

import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstallRegistry;
import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class SimpleVMRegistryDiscovery {

	public IVMInstall findVMInstall(IServerDelegate delegate) {
		return findDefaultRegistry(delegate).getDefaultVMInstall();
	}

	private IVMInstallRegistry findDefaultRegistry(IServerDelegate delegate) {
		if( delegate != null && delegate.getServer() != null 
				&& delegate.getServer().getServerManagementModel() != null) {
			return delegate.getServer().getServerManagementModel().getVMInstallModel();
		}
		return getDefaultRegistry();
	}
	
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
