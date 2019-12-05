package org.jboss.tools.rsp.server.generic.servertype.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponseItem;
import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.impl.Activator;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class EditServerConfigurationActionHandler {
	public static final String ACTION_ID = "EditServerConfigurationActionHandler.actionId";
	public static final String ACTION_LABEL = "Edit Configuration File...";

	public static final ServerActionWorkflow getInitialWorkflow(GenericServerBehavior genericServerDelegate2) {
		return new EditServerConfigurationActionHandler(genericServerDelegate2).getInitialWorkflowInternal();
	}
	
	private GenericServerBehavior genericServerDelegate;
	public EditServerConfigurationActionHandler(GenericServerBehavior genericServerDelegate) {
		this.genericServerDelegate = genericServerDelegate;
	}
	
	protected ServerActionWorkflow getInitialWorkflowInternal() {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);

		String configFilePath = getConfigurationFile();
		if( !(new File(configFilePath).exists())) {
			workflow.setStatus(StatusConverter.convert(
					new Status(IStatus.CANCEL, Activator.BUNDLE_ID, ACTION_LABEL)));
			return action;
		}
		
		// Simple action entirely on the UI side
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_EDITOR);
		Map<String,String> propMap = new HashMap<>();
		propMap.put(ServerManagementAPIConstants.WORKFLOW_EDITOR_PROPERTY_PATH, configFilePath);
		item1.setProperties(propMap);
		item1.setId(ACTION_ID);
		item1.setLabel(ACTION_LABEL);
		
		items.add(item1);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.OK, Activator.BUNDLE_ID, ACTION_LABEL)));
		return action;
	}

	protected String getConfigurationFile() {
		String home = getDefaultWorkingDirectory();
		String configFile = "";
		JSONMemento actions = this.genericServerDelegate.getActionsJSON();
		if (actions != null) {
			JSONMemento editConfigAction = actions.getChild("editserverconfiguration");
			if (editConfigAction != null) {
				configFile = editConfigAction.getString("path");
			}			
		}
		IPath configFilePath = new Path(home).append(configFile);
		return configFilePath.toOSString();
	}
	
	private String getDefaultWorkingDirectory() {
		String serverHome = this.genericServerDelegate.getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,(String) null);
		if( serverHome != null )
			return serverHome;
		
		String serverHomeFile = this.genericServerDelegate.getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_FILE,(String) null);
		if( serverHomeFile != null )
			return new File(serverHomeFile).getParent();

		return null;
	}

	public WorkflowResponse handle(ServerActionRequest req) {
		if( req == null || req.getData() == null ) 
			return AbstractServerDelegate.okWorkflowResponse();
		return null;
	}
	
}
