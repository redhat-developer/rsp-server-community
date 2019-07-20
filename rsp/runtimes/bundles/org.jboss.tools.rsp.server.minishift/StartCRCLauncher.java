/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.minishift.servertype.impl;

import org.jboss.tools.rsp.server.minishift.servertype.AbstractLauncher;
import org.jboss.tools.rsp.server.minishift.servertype.MinishiftPropertyUtility;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class StartCRCLauncher extends AbstractLauncher {
	
	public StartCRCLauncher(IServerDelegate jBossServerDelegate) {
		super(jBossServerDelegate);
	}

	@Override
	public String getProgramArguments() {
		IServer server = getServer();
				
		String cpu = MinishiftPropertyUtility.getMinishiftCPU(server, 4);
		String cpuArg = isEmpty(cpu) ? "" : " --cpus=" + cpu;
		
		String memory = MinishiftPropertyUtility.getMinishiftMemory(server, 8192);
		String memoryArg = isEmpty(memory) ? "" : " --memory=" + memory;
		
		String pullSecret = MinishiftPropertyUtility.getMinishiftImagePullSecret(server);
		String pullSecretArg = isEmpty(pullSecret) ? "" : " --pull-secret-file '" + pullSecret + "'";
		
		String vmd = getAdditionalVMArgs(server);
				
		return "start" + cpuArg + memoryArg + vmd + pullSecretArg;
	}
	protected String getAppendedArguments() {
		String append = getServer().getAttribute(
				MinishiftServerDelegate.STARTUP_PROGRAM_ARGS_STRING, (String)null);
		return append == null ? "" : append;
	}
	
	private String getAdditionalVMArgs(IServer server) {
		
		String vmDriver = MinishiftPropertyUtility.getMinishiftVMDriver(server);
		String vmd = isEmpty(vmDriver) ? "" : " --vm-driver=" + vmDriver;
		
		String bundle = MinishiftPropertyUtility.getCRCBundle(server);
		String bundleArg = isEmpty(bundle) ? "" : " --bundle=" + bundle;
		
		boolean shouldOverride = MinishiftPropertyUtility.getShouldOverride(server);
		
		if( shouldOverride &&
				vmd != null && 
				vmd.trim().length() > 0 &&
				bundleArg != null &&
				bundleArg.trim().length() > 0
				) {
			return vmd + bundleArg;
		}
		
		return "";
		
	}
	
	private boolean isEmpty(String s) {
		return s == null ? true : s.isEmpty();
	}
}
