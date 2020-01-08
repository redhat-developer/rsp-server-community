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
import org.jboss.tools.rsp.api.dao.WorkflowPromptDetails;
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
	public static final String ACTION_EDIT_FILE_PROMPT_ID = "EditServerConfigurationActionHandler.selection.id";
	public static final String ACTION_EDIT_FILE_PROMPT_LABEL = "EditServerConfigurationActionHandler.selection.label";
	
	public static final ServerActionWorkflow getInitialWorkflow(GenericServerBehavior genericServerDelegate2) {
		return new EditServerConfigurationActionHandler(genericServerDelegate2).getInitialWorkflowInternal();
	}
	
	private GenericServerBehavior genericServerDelegate;
	public EditServerConfigurationActionHandler(GenericServerBehavior genericServerDelegate) {
		this.genericServerDelegate = genericServerDelegate;
	}
	
	protected ServerActionWorkflow getInitialWorkflowInternal() {
		String[] possiblePaths = getConfigurationFilesRelative();
		ArrayList<String> asList = new ArrayList<String>();
		String home = getDefaultWorkingDirectory();

		if( possiblePaths != null ) {
			for( int i = 0; i < possiblePaths.length; i++ ) {
				IPath tmpPath = new Path(home).append(possiblePaths[i]);
				if( tmpPath.toFile().isFile()) {
					asList.add(possiblePaths[i]);
				}
			}
		}
		
		if( asList.size() == 0 ) {
			return cancelWorkflow();
		}
		
		if( asList.size() == 1 ) {
			return executePath(asList.get(0));
		}
		
		return fileChoiceWorkflow(asList);
	}
	
	private ServerActionWorkflow fileChoiceWorkflow(ArrayList<String> choices) {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);

		WorkflowPromptDetails prompt = new WorkflowPromptDetails();
		prompt.setResponseSecret(false);
		prompt.setResponseType(ServerManagementAPIConstants.ATTR_TYPE_STRING);
		prompt.setValidResponses(choices);
		
		// Simple action entirely on the UI side
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_PROMPT_SMALL);
		item1.setPrompt(prompt);
		item1.setId(ACTION_EDIT_FILE_PROMPT_ID);
		item1.setLabel(ACTION_EDIT_FILE_PROMPT_LABEL);
		
		items.add(item1);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_LABEL)));
		return action;
	}

	private ServerActionWorkflow cancelWorkflow() {
		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);


		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.CANCEL, Activator.BUNDLE_ID, ACTION_LABEL)));
		return action;
	}

	protected ServerActionWorkflow executePath(String relative) {
		String home = getDefaultWorkingDirectory();
		IPath tmpPath = new Path(home).append(relative);
		if(!tmpPath.toFile().isFile()) {
			return cancelWorkflow();
		}
		
		// Simple action entirely on the UI side
		WorkflowResponseItem item1 = new WorkflowResponseItem();
		item1.setItemType(ServerManagementAPIConstants.WORKFLOW_TYPE_OPEN_EDITOR);
		Map<String,String> propMap = new HashMap<>();
		propMap.put(ServerManagementAPIConstants.WORKFLOW_EDITOR_PROPERTY_PATH, tmpPath.toOSString());
		item1.setProperties(propMap);
		item1.setId(ACTION_ID);
		item1.setLabel(ACTION_LABEL);

		WorkflowResponse workflow = new WorkflowResponse();
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_ID, ACTION_LABEL, workflow);
		
		List<WorkflowResponseItem> items = new ArrayList<>();
		workflow.setItems(items);


		items.add(item1);
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.OK, Activator.BUNDLE_ID, ACTION_LABEL)));
		return action;
	}
	

	protected String[] getConfigurationFilesRelative() {
		String configFiles = "";
		JSONMemento actions = this.genericServerDelegate.getActionsJSON();
		if (actions != null) {
			JSONMemento editConfigAction = actions.getChild("editServerConfiguration");
			if (editConfigAction != null) {
				configFiles = editConfigAction.getString("paths");
			}			
		}
		if( configFiles == null || configFiles.length() == 0 )
			return null;
		String[] filesArr = configFiles.split(",");
		return (filesArr == null || filesArr.length == 0 ) ? null : filesArr;
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
		if( req == null || req.getData() == null || req.getData().size() == 0 ) 
			return AbstractServerDelegate.okWorkflowResponse();
		String path = (String)req.getData().get(ACTION_EDIT_FILE_PROMPT_ID);
		if( path != null ) {
			return executePath(path).getActionWorkflow();
		}
		return cancelWorkflow().getActionWorkflow();
	}
	
}
