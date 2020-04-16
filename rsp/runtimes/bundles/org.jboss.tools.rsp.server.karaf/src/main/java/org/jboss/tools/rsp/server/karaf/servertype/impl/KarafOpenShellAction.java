/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.karaf.servertype.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.karaf.impl.Activator;
import org.jboss.tools.rsp.server.karaf.servertype.impl.util.CommandLocationBinary;
import org.jboss.tools.rsp.server.karaf.servertype.impl.util.CommandLocationLookupStrategy.OSUtilWrapper;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class KarafOpenShellAction {
	public static final String ACTION_OPEN_SHELL_ID = "KarafOpenShellAction.openShellSSH";
	private static final String ACTION_OPEN_SHELL_LABEL = "Open Karaf Shell (SSH)";	

	public static ServerActionWorkflow getInitialWorkflow(KarafServerDelegate karafServerDelegate) {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_OPEN_SHELL_ID, ACTION_OPEN_SHELL_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);
		
		// Simple action entirely on the UI side
		String serverHome = karafServerDelegate.getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String)null);
		String args = getArgs(serverHome);
		String sshCommandPath = getSshCommandPath();
		
		
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_TERMINAL);
		Map<String,String> propMap = new HashMap<>();
		propMap.put(ServerManagementAPIConstants.WORKFLOW_TERMINAL_CMD, sshCommandPath + " " + args);
		item1.setProperties(propMap);
		item1.setId(ACTION_OPEN_SHELL_ID);
		item1.setLabel(ACTION_OPEN_SHELL_LABEL);
		items.add(item1);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.OK, Activator.BUNDLE_ID, ACTION_OPEN_SHELL_LABEL)));
		return action;
	}
	
	private static String getSshCommandPath() {
		return findSSHLocation();
	}

	private static String getArgs(String serverHome) {
		File serverHomeFile = new File(serverHome);
		File etc = new File(serverHomeFile, "etc");
		File shellCfg = new File(etc, "org.apache.karaf.shell.cfg");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(shellCfg));
			String port = props.getProperty("sshPort");
			String host = props.getProperty("sshHost");
			if( "0.0.0.0".equals(host)) {
				host = "localhost";
			}
			return "-p " + port + " karaf@" + host;
		} catch(IOException ioe) {
			
		}
		return null;
	}
	
	private static CommandLocationBinary sshBinary = null;
    public static String findSSHLocation() {
        if (sshBinary == null) {
        	sshBinary = new CommandLocationBinary("ssh");
        	sshBinary.addPlatformLocation(OSUtilWrapper.UNIX, "/usr/bin/ssh");
        	sshBinary.addPlatformLocation(OSUtilWrapper.WINDOWS, "C:\\Windows\\System32\\OpenSSH\\ssh.exe");
        	sshBinary.setDefaultPlatform(OSUtilWrapper.UNIX);
        }
        return sshBinary.findLocation();
}

}
