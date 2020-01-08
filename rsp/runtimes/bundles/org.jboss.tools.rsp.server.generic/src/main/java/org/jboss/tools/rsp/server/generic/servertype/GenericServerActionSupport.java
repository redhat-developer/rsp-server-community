/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.servertype;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.actions.EditServerConfigurationActionHandler;
import org.jboss.tools.rsp.server.generic.servertype.actions.ShowInBrowserActionHandler;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class GenericServerActionSupport {
	private GenericServerBehavior behavior;
	private JSONMemento behaviorMemento;

	public GenericServerActionSupport(GenericServerBehavior behavior, JSONMemento behaviorMemento) {
		this.behavior = behavior;
		this.behaviorMemento = behaviorMemento;
	}

	public ListServerActionResponse listServerActions() {
		ListServerActionResponse ret = new ListServerActionResponse();
		ret.setStatus(StatusConverter.convert(Status.OK_STATUS));
		List<ServerActionWorkflow> allActions = new ArrayList<>();
		JSONMemento props = behaviorMemento.getChild("actions");
		if( props != null ) {
			JSONMemento[] actionsToAdd = props.getChildren();
			for (JSONMemento actionToAdd : actionsToAdd) {
				ServerActionWorkflow wf1 = null;
				if (actionToAdd.getNodeName().equals("showinbrowser")) {
					wf1 = ShowInBrowserActionHandler.getInitialWorkflow(behavior);
				}
				if (actionToAdd.getNodeName().equals("editserverconfiguration")) {
					wf1 = EditServerConfigurationActionHandler.getInitialWorkflow(behavior);
				}
				if (wf1 != null) {
					allActions.add(wf1);
				}				
			}
		}		
		ret.setWorkflows(allActions);
		return ret;
	}
	
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_ID.equals(req.getActionId() )) {
			return new ShowInBrowserActionHandler(behavior).handle(req);
		}
		if( EditServerConfigurationActionHandler.ACTION_ID.equals(req.getActionId() )) {
			return new EditServerConfigurationActionHandler(behavior).handle(req);
		}
		return cancelWorkflowResponse();
	}
	
	public static WorkflowResponse cancelWorkflowResponse() {
		WorkflowResponse resp = new WorkflowResponse();
		resp.setStatus(StatusConverter.convert(Status.CANCEL_STATUS));
		resp.setItems(new ArrayList<>());
		return resp;
	}

}
