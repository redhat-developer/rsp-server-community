package org.jboss.tools.rsp.server.tomcat.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.model.polling.WebPortPoller;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.tomcat.impl.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatServerDelegate extends AbstractServerDelegate {
	
	public static final String TOMCAT_START_LAUNCH_SHARED_DATA = "TomcatServerDelegate.startLaunch";
	private static final Logger LOG = LoggerFactory.getLogger(TomcatServerDelegate.class);
	private ILaunch startLaunch;

	public TomcatServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	
	@Override
	public IStatus canStart(String launchMode) {
		if( !modesContains(launchMode)) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID,
					"Server may not be launched in mode " + launchMode);
		}
		/*String javaCompatError = null; //getJavaCompatibilityError();
		if( javaCompatError != null ) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID,
					"Server can not be started: " + javaCompatError);
		}*/
		if( getServerRunState() == IServerDelegate.STATE_STOPPED ) {
			IStatus v = validate().getStatus();
			if( !v.isOK() )
				return v;
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}
	
	private IServerStartLauncher getStartLauncher() {
		return new TomcatStartLauncher(this);
	}
	
	private void setStartLaunch(ILaunch launch) {
		putSharedData(TOMCAT_START_LAUNCH_SHARED_DATA, launch);
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
			startLaunch = launcher.launch(mode);
			launchedDetails = launcher.getLaunchedDetails();
			setStartLaunch(startLaunch);
			registerLaunch(startLaunch);
		} catch(CoreException ce) {
			if( startLaunch != null ) {
				IProcess[] processes = startLaunch.getProcesses();
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
	
	protected void launchPoller(IServerStatePoller.SERVER_STATE expectedState) {
		IPollResultListener listener = expectedState == IServerStatePoller.SERVER_STATE.DOWN ? 
				shutdownServerResultListener() : launchServerResultListener();
		IServerStatePoller poller = getPoller(expectedState);
		PollThreadUtils.pollServer(getServer(), expectedState, poller, listener);
	}
	
	protected IServerStatePoller getPoller(IServerStatePoller.SERVER_STATE expectedState) {
		return getDefaultWebPortPoller();
	}
	
	private IServerStatePoller getDefaultWebPortPoller() {
		return new WebPortPoller("Web Poller: " + this.getServer().getName()) {
			@Override
			protected String getURL(IServer server) {
				return getPollURL(server);
			}
		};
	}
	
	public String getPollURL(IServer server) {
		String host = server.getAttribute(ITomcatServerAttributes.TOMCAT_SERVER_HOST, 
				ITomcatServerAttributes.TOMCAT_SERVER_HOST_DEFAULT);
		int port = server.getAttribute(ITomcatServerAttributes.TOMCAT_SERVER_PORT, 
				ITomcatServerAttributes.TOMCAT_SERVER_PORT_DEFAULT);
		String url = NLS.bind("http://{0}:{1}", host, port);
		return url;
	}

}
