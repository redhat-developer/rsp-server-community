/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.jetty.servertype.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.generic.IStringSubstitutionProvider;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.jetty.impl.Activator;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class InitializeJettyActionHandler {
	public static final String ACTION_ID = "InitializeJettyActionHandler.actionId";
	public static final String ACTION_LABEL = "Initialize Jetty";
	
	public static final ServerActionWorkflow getInitialWorkflow(GenericServerBehavior genericServerDelegate2) {
		return new InitializeJettyActionHandler(genericServerDelegate2).getInitialWorkflowInternal();
	}
	
	private GenericServerBehavior genericServerDelegate;
	public InitializeJettyActionHandler(GenericServerBehavior genericServerDelegate) {
		this.genericServerDelegate = genericServerDelegate;
	}
	
	protected ServerActionWorkflow getInitialWorkflowInternal() {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_LABEL)));
		return action;
	}

	private ServerActionWorkflow okWorkflow() {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.OK, Activator.BUNDLE_ID, ACTION_LABEL)));
		return action;
	}

	public WorkflowResponse handle(ServerActionRequest req) {
		initializeServer(genericServerDelegate);
		return okWorkflow().getActionWorkflow();
	}

	private static String applySubstitutions(IServerDelegate del, String input) throws CoreException {
		return (del instanceof IStringSubstitutionProvider) ? 
				((IStringSubstitutionProvider)del).applySubstitutions(input) : input;
	}
	public static final void initializeServer(GenericServerBehavior behavior) {
		AbstractGenericJavaLauncher launcher = createLauncher(behavior);
		try {
			launcher.launch("run");
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}
	
	private static AbstractGenericJavaLauncher createLauncher(GenericServerBehavior behavior) {
		AbstractGenericJavaLauncher ret = new AbstractGenericJavaLauncher(behavior) {

			@Override
			protected String getWorkingDirectory() {
				String wd = behavior.getServer().getAttribute(IJettyServerAttributes.JETTY_BASEDIR, (String)null);
				try {
					wd = applySubstitutions(behavior, wd);
				} catch( CoreException ce) {
					ce.printStackTrace();
				}
				File f = new File(wd);
				if( !f.exists()) {
					f.mkdirs();
				}
				return wd;
			}

			@Override
			protected String getMainTypeName() {
				return "org.eclipse.jetty.start.Main";
			}

			@Override
			protected String getVMArguments() {
				return "";
			}

			@Override
			protected String getProgramArguments() {
				return "--add-module=server,http,deploy";
			}

			@Override
			protected String[] getClasspath() {
				String home = behavior.getServer().getAttribute(
						IJettyServerAttributes.SERVER_HOME, (String)null);
				return new String[] {
						new File(home, "start.jar").getAbsolutePath()};
			}
			
		};
		return ret;
	}
	
}
