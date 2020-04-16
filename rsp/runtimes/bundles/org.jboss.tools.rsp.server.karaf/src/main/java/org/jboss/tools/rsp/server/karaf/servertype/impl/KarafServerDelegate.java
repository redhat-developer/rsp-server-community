/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.karaf.servertype.impl;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerType;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class KarafServerDelegate extends GenericServerBehavior {
	public KarafServerDelegate(IServer server, JSONMemento behaviorMemento) {
		super(server, behaviorMemento);
	}
	
	@Override
	public ListServerActionResponse listServerActions() {
		ListServerActionResponse ret = new ListServerActionResponse();
		ret.setStatus(StatusConverter.convert(Status.OK_STATUS));
		List<ServerActionWorkflow> allActions = new ArrayList<>();
		ServerActionWorkflow wfWipeAndRestart = KarafWipeCacheRestartAction.getInitialWorkflow(this);
		ServerActionWorkflow wfOpenShell = KarafOpenShellAction.getInitialWorkflow(this);
		allActions.add(wfWipeAndRestart);
		allActions.add(wfOpenShell);
		ret.setWorkflows(allActions);
		return ret;
	}
	
	@Override
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( KarafWipeCacheRestartAction.ACTION_WIPE_CACHE_RESTART_ID.equals(req.getActionId() )) {
			return new KarafWipeCacheRestartAction(this).handle(req);
		}
//		if( KarafOpenShellAction.ACTION_OPEN_SHELL_ID.equals(req.getActionId() )) {
//			return new KarafOpenShellAction(this).handle(req);
//		}
		return cancelWorkflowResponse();
	}
	@Override
	public void setDependentDefaults(IServerWorkingCopy server) {
		// Do nothing
		try {
			CommandLineDetails det = getStartLauncher().getLaunchCommand("run");
			String progArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_PROGRAM_ARGS);
			String vmArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_VM_ARGS);
			if(progArgs != null && !progArgs.isEmpty()) {
				progArgs = "";
			}
			if(vmArgs != null && !vmArgs.isEmpty()) {
				vmArgs = "";
			}
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_BOOLEAN, false);
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_PROGRAM_ARGS, progArgs);
			server.setAttribute(GenericServerType.JAVA_LAUNCH_OVERRIDE_VM_ARGS, vmArgs);
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}

}
