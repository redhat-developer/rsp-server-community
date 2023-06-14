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
import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.DefaultExternalVariableResolver;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager.IExternalVariableResolver;
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
		
		if( ServerManagementAPIConstants.STATE_STARTED == getServerState().getState()) {
			ServerActionWorkflow wfWipeAndRestart = KarafWipeCacheRestartAction.getInitialWorkflow(this);
			ServerActionWorkflow wfOpenShell = KarafOpenShellAction.getInitialWorkflow(this);
			allActions.add(wfWipeAndRestart);
			allActions.add(wfOpenShell);
		}
		
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
		setJavaLaunchDependentDefaults(server);
	}

	@Override
	protected IExternalVariableResolver getExternalVariableResolver() {
		return new KarafExternalVariableResolver(this);
	}
	
	protected class KarafExternalVariableResolver extends DefaultExternalVariableResolver {

		public KarafExternalVariableResolver(GenericServerBehavior genericServerBehavior) {
			super(genericServerBehavior);
		}
		@Override
		public String getNonServerKeyValue(String key) {
			String superRet = super.getNonServerKeyValue(key);
			if( superRet != null ) 
				return superRet;
			
			if( "karaf.MajorMinorMicro".equals(key)) {
				String serverHome = getServer().getAttribute("server.home.dir", (String)null);
				if( serverHome != null ) {
					File lib = new File(serverHome, "lib");
					File endorsed = new File(lib, "endorsed");
					File[] list = endorsed.listFiles();
					for( int i = 0; i < list.length; i++ ) {
						if( list[i].getName().startsWith("org.apache.karaf.specs.locator-")) {
							String ret = list[i].getName().substring("org.apache.karaf.specs.locator-".length());
							return ret.substring(0, ret.length()-4);
						}
					}
				}
			}
			return null;
		}
	}

}
