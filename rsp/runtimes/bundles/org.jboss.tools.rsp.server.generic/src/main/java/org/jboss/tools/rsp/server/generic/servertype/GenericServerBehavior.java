package org.jboss.tools.rsp.server.generic.servertype;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ListServerActionResponse;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.IPublishControllerWithOptions;
import org.jboss.tools.rsp.server.generic.servertype.actions.EditServerConfigurationActionHandler;
import org.jboss.tools.rsp.server.generic.servertype.actions.ShowInBrowserActionHandler;
import org.jboss.tools.rsp.server.generic.servertype.launch.GenericJavaLauncher;
import org.jboss.tools.rsp.server.generic.servertype.launch.TerminateShutdownLauncher;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.model.polling.WebPortPoller;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller.SERVER_STATE;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericServerBehavior extends AbstractServerDelegate {
	private static final Logger LOG = LoggerFactory.getLogger(GenericServerBehavior.class);
	public static final String START_LAUNCH_SHARED_DATA = "GenericServerBehavior.startLaunch";

	private JSONMemento behaviorMemento;
	private IPublishControllerWithOptions publishController;

	public GenericServerBehavior(IServer server, JSONMemento behaviorMemento) {
		super(server);
		this.behaviorMemento = behaviorMemento;
		setServerState(IServerDelegate.STATE_STOPPED);
	}

	protected ILaunch getStartLaunch() {
		return (ILaunch)getSharedData(START_LAUNCH_SHARED_DATA);
	}
	
	protected void setStartLaunch(ILaunch launch) {
		putSharedData(START_LAUNCH_SHARED_DATA, launch);
	}
	
	@Override
	public CommandLineDetails getStartLaunchCommand(String mode, ServerAttributes params) {
		try {
			return getStartLauncher().getLaunchCommand(mode);
		} catch(CoreException ce) {
			LOG.error(ce.getMessage(), ce);
			return null;
		}
	}

	@Override
	public StartServerResponse start(String mode) {
		IStatus stat = canStart(mode);
		if( !stat.isOK()) {
			org.jboss.tools.rsp.api.dao.Status s = StatusConverter.convert(stat);
			return new StartServerResponse(s, null);
		}
		
		setMode(mode);
		setServerState(IServerDelegate.STATE_STARTING);
		
		CommandLineDetails launchedDetails = null;
		try {
			launchPoller(IServerStatePoller.SERVER_STATE.UP);
			IServerStartLauncher launcher = getStartLauncher();
			ILaunch startLaunch2 = launcher.launch(mode);
			launchedDetails = launcher.getLaunchedDetails();
			setStartLaunch(startLaunch2);
			registerLaunch(startLaunch2);
		} catch(CoreException ce) {
			if( getStartLaunch() != null ) {
				IProcess[] processes = getStartLaunch().getProcesses();
				for( int i = 0; i < processes.length; i++ ) {
					try {
						processes[i].terminate();
					} catch(DebugException de) {
						LOG.error(de.getMessage(), de);
					}
				}
			}
			setServerState(IServerDelegate.STATE_STOPPED);
			org.jboss.tools.rsp.api.dao.Status s = StatusConverter.convert(ce.getStatus());
			return new StartServerResponse(s, launchedDetails);
		}
		return new StartServerResponse(StatusConverter.convert(Status.OK_STATUS), launchedDetails);
	}

	
	@Override
	public IStatus stop(boolean force) {
		setServerState(IServerDelegate.STATE_STOPPING);
		ILaunch stopLaunch = null;
		launchPoller(IServerStatePoller.SERVER_STATE.DOWN);
		try {
			stopLaunch = getStopLauncher().launch(force);
			if( stopLaunch != null)
				registerLaunch(stopLaunch);
		} catch(CoreException ce) {
			// Dead code... but I feel it's not dead?  idk :( 
//			if( stopLaunch != null ) {
//				IProcess[] processes = startLaunch.getProcesses();
//				for( int i = 0; i < processes.length; i++ ) {
//					try {
//						processes[i].terminate();
//					} catch(DebugException de) {
//						LaunchingCore.log(de);
//					}
//				}
//			}
			setServerState(IServerDelegate.STATE_STARTED);
			return ce.getStatus();
		}
		return Status.OK_STATUS;

	}
	private IServerStartLauncher getStartLauncher() {
		JSONMemento startupMemento = behaviorMemento.getChild("startup");
		// TODO casting is dumb. Should be smarter than this
		return (IServerStartLauncher)getLauncher(startupMemento);
	}
	protected IServerShutdownLauncher getStopLauncher() {
		JSONMemento shutdownMemento = behaviorMemento.getChild("shutdown");
		return getLauncher(shutdownMemento);
	}
	public JSONMemento getActionsJSON() {
		return behaviorMemento.getChild("actions");
	}
	
	protected IServerShutdownLauncher getLauncher(JSONMemento memento) {
		String launchType = memento.getString("launchType");
		if( "java-launch".equals(launchType)) {
			return new GenericJavaLauncher(this, memento);
		}
		if( "terminateProcess".equals(launchType)) {
			ILaunch startLaunch = getStartLaunch();
			return new TerminateShutdownLauncher(this, startLaunch);
		}
		return null;
	}

	
	@Override
	public IStatus clientSetServerStarting(ServerStartingAttributes attr) {
		setServerState(STATE_STARTING, true);
		if( attr.isInitiatePolling()) {
			launchPoller(IServerStatePoller.SERVER_STATE.UP);
		}
		return Status.OK_STATUS;
	}

	private void launchPoller(SERVER_STATE upOrDown) {
		if( upOrDown == IServerStatePoller.SERVER_STATE.UP) {
			JSONMemento startupMemento = behaviorMemento.getChild("startup");
			String poller = startupMemento.getString("poller");
			if( poller != null && !poller.isEmpty()) {
				launchPoller(upOrDown, poller, startupMemento);
			}
		} else if( upOrDown == IServerStatePoller.SERVER_STATE.DOWN) {
			JSONMemento shutdownMemento = behaviorMemento.getChild("shutdown");
			String poller = shutdownMemento.getString("poller");
			if( poller != null && !poller.isEmpty()) {
				launchPoller(upOrDown, poller, shutdownMemento);
			}
		}
		
	}

	private void launchPoller(SERVER_STATE upOrDown, String pollerId, JSONMemento startupMemento) {
		// TODO eventually break this out
		if("automaticSuccess".equals(pollerId)) {
			if( upOrDown == IServerStatePoller.SERVER_STATE.UP)
				setServerState(STATE_STARTED, true);
			if( upOrDown == IServerStatePoller.SERVER_STATE.DOWN)
				setServerState(STATE_STOPPED, true);
			return;
		}
		
		if("webPoller".equals(pollerId)) {
			JSONMemento props = startupMemento.getChild("pollerProperties");
			if( props != null ) {
				String url = props.getString("url");
				IPollResultListener listener = upOrDown == IServerStatePoller.SERVER_STATE.DOWN ? 
						shutdownServerResultListener() : launchServerResultListener();
				WebPortPoller toRun = new WebPortPoller("Web Poller: " + this.getServer().getName()) {
					@Override
					protected String getURL(IServer server) {
						return url;
					}
				};
				PollThreadUtils.pollServer(getServer(), upOrDown, toRun, listener);
			}
			return;
		}
		
	}

	@Override
	public IStatus clientSetServerStarted(LaunchParameters attr) {
		setServerState(STATE_STARTED, true);
		return Status.OK_STATUS;
	}
	
	
	
	/*
	 * Publishing
	 */
	protected IPublishControllerWithOptions getPublishController() {
		if( publishController == null ) {
			JSONMemento publishMemento = behaviorMemento.getChild("publish");
			String deployPath = publishMemento.getString("deployPath");
			String approvedSuffixes = publishMemento.getString("approvedSuffixes");
			String[] suffixes = approvedSuffixes == null ? null : approvedSuffixes.split(",");
			String supportsExploded = publishMemento.getString("supportsExploded");
			boolean exploded = (supportsExploded == null ? false : Boolean.parseBoolean(supportsExploded));
			this.publishController = new GenericServerSuffixPublishController(
					getServer(), this, 
					suffixes, deployPath, exploded);
		}
		return publishController;
	}
		
	@Override
	public IStatus canAddDeployable(DeployableReference ref) {
		return getPublishController().canAddDeployable(ref);
	}
	
	@Override
	public IStatus canRemoveDeployable(DeployableReference reference) {
		return getPublishController().canRemoveDeployable(getServerPublishModel().fillOptionsFromCache(reference));
	}
	
	@Override
	public IStatus canPublish() {
		return getPublishController().canPublish();
	}

	@Override
	protected void publishStart(int publishType) throws CoreException {
		getPublishController().publishStart(publishType);
	}

	@Override
	protected void publishFinish(int publishType) throws CoreException {
		getPublishController().publishFinish(publishType);
		super.publishFinish(publishType);
	}

	@Override
	public Attributes listDeploymentOptions() {
		return getPublishController().listDeploymentOptions();
	}

	
	@Override
	protected void publishDeployable(DeployableReference reference, 
			int publishRequestType, int modulePublishState) throws CoreException {
		int syncState = getPublishController()
				.publishModule(reference, publishRequestType, modulePublishState);
		setDeployablePublishState(reference, syncState);
		
		// TODO launch a module poller?!
		setDeployableState(reference, ServerManagementAPIConstants.STATE_STARTED);
	}

	@Override
	protected void processTerminated(IProcess p) {
		ILaunch l = p.getLaunch();
		if( l == getStartLaunch() ) {
			JSONMemento startup = behaviorMemento.getChild("startup");
			if( startup != null ) {
				String action = startup.getString("onProcessTerminated");
				if( action != null ) {
					handleOnProcessTerminated(p, action);
				}
			}
		}
		fireServerProcessTerminated(getProcessId(p));
	}

	private void handleOnProcessTerminated(IProcess p, String action) {
		if( "setServerStateStopped".equals(action)) {
			setMode(null);
			setStartLaunch(null);
			setServerState(IServerDelegate.STATE_STOPPED);
		}
		if( "setServerStateStarted".equals(action)) {
			setMode(null);
			setStartLaunch(null);
			setServerState(IServerDelegate.STATE_STARTED);
		}
	}

	public void setServerState(int state) {
		super.setServerState(state);
	}

	public void setServerState(int state, boolean fire) {
		super.setServerState(state, fire);
	}
	
	public String getPollURL(IServer server) {
		JSONMemento startupMemento = behaviorMemento.getChild("startup");
		if (startupMemento != null) {
			JSONMemento props = startupMemento.getChild("pollerProperties");
			if( props != null ) {
				String url = props.getString("url");
				return url;
			}
		}		
		return null;
	}
	
	@Override
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
					wf1 = ShowInBrowserActionHandler.getInitialWorkflow(this);
				}
				if (actionToAdd.getNodeName().equals("editserverconfiguration")) {
					wf1 = EditServerConfigurationActionHandler.getInitialWorkflow(this);
				}
				if (wf1 != null) {
					allActions.add(wf1);
				}				
			}
		}		
		ret.setWorkflows(allActions);
		return ret;
	}
	
	@Override
	public WorkflowResponse executeServerAction(ServerActionRequest req) {
		if( ShowInBrowserActionHandler.ACTION_SHOW_IN_BROWSER_ID.equals(req.getActionId() )) {
			return new ShowInBrowserActionHandler(this).handle(req);
		}
		if( EditServerConfigurationActionHandler.ACTION_ID.equals(req.getActionId() )) {
			return new EditServerConfigurationActionHandler(this).handle(req);
		}
		return cancelWorkflowResponse();
	}

}
