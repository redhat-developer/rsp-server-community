package org.jboss.tools.rsp.server.felix.servertype.impl;

import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.server.felix.impl.Activator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.AbstractPoller;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FelixServerDelegate extends AbstractServerDelegate {
	
	public static final String FELIX_START_LAUNCH_SHARED_DATA = "FelixServerDelegate.startLaunch";
	private static final Logger LOG = LoggerFactory.getLogger(FelixServerDelegate.class);
	private ILaunch startLaunch;

	public FelixServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	public IStatus canStop() {
		return Status.OK_STATUS;
	}
	
	@Override
	protected void processTerminated(IProcess p) {
		ILaunch l = p.getLaunch();
		if( l == getStartLaunch() ) {
			if( allProcessesTerminated(l)) {
				setMode(null);
				setStartLaunch(null);
				setServerState(IServerDelegate.STATE_STOPPED);
			}
		}
		fireServerProcessTerminated(getProcessId(p));
	}

	private void terminateAllProcesses(ILaunch launch) {
		if( launch != null ) {
			IProcess[] processes = launch.getProcesses();
			for( int i = 0; i < processes.length; i++ ) {
				if( !processes[i].isTerminated() && processes[i].canTerminate()) {
					try {
						processes[i].terminate();
					} catch( DebugException de) {
						// ignore
					}
				}
			}
		}
	}

	private boolean allProcessesTerminated(ILaunch launch) {
		if( launch != null ) {
			IProcess[] processes = launch.getProcesses();
			for( int i = 0; i < processes.length; i++ ) {
				if( !processes[i].isTerminated()) { 
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public IStatus stop(boolean force) {
		setServerState(IServerDelegate.STATE_STOPPING);
		ILaunch started = getStartLaunch();
		terminateAllProcesses(started);
		if( allProcessesTerminated(started)) {
			setServerState(IServerDelegate.STATE_STOPPED);
		} else {
			setServerState(IServerDelegate.STATE_STARTED);
		}
		return Status.OK_STATUS;
	}
	
	@Override
	public IStatus canStart(String launchMode) {
		if( !modesContains(launchMode)) {
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID,
					"Server may not be launched in mode " + launchMode);
		}
		if( getServerRunState() == IServerDelegate.STATE_STOPPED ) {
			IStatus v = validate().getStatus();
			if( !v.isOK() )
				return v;
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
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
			//launchPoller(IServerStatePoller.SERVER_STATE.UP);
			IServerStartLauncher launcher = getStartLauncher();
			startLaunch = launcher.launch(mode);
			launchedDetails = launcher.getLaunchedDetails();
			setStartLaunch(startLaunch);
			registerLaunch(startLaunch);
			setServerState(IServerDelegate.STATE_STARTED);
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
		final long beg = System.currentTimeMillis();
		return new AbstractPoller() {
			
			@Override
			protected SERVER_STATE onePing(IServer server) {
				long curr = System.currentTimeMillis();
				if( curr > (beg + 5000))
					return SERVER_STATE.UP;
				return SERVER_STATE.DOWN;
			}
			
			@Override
			protected String getThreadName() {
				return "Felix Poller";
			}
		};
	}
	
	private IServerStartLauncher getStartLauncher() {
		return new FelixStartLauncher(this);
	}
	
	private void setStartLaunch(ILaunch launch) {
		putSharedData(FELIX_START_LAUNCH_SHARED_DATA, launch);
	}

	private ILaunch getStartLaunch() {
		return (ILaunch)getSharedData(FELIX_START_LAUNCH_SHARED_DATA);
	}


}
