/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.servertype.impl;

import java.io.File;

import org.example.rsp.server.wonka.bundle.impl.Activator;
import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.model.polling.AbstractPoller;
import org.jboss.tools.rsp.server.spi.model.polling.IPollResultListener;
import org.jboss.tools.rsp.server.spi.model.polling.IServerStatePoller;
import org.jboss.tools.rsp.server.spi.model.polling.PollThreadUtils;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WonkaServerDelegate extends AbstractServerDelegate {

	private static final Logger LOG = LoggerFactory.getLogger(WonkaServerDelegate.class);
	public static final String START_LAUNCH_SHARED_DATA = "WonkaServerDelegate.startLaunch";
	
	public WonkaServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	

	/*
	 * A server type might choose to set some specific flag default values 
	 * explicitly during creation even if the user has not chosen to set them. 
	 */
	@Override
	public void setDefaults(IServerWorkingCopy server) {
		server.setAttribute("wonka.color.default", "blue");
	}

	/*
	 * The delegate should be able to validate a server configuration, 
	 * its properties, and return an error if a critical field has an invalid value.
	 */

	@Override
	public CreateServerValidation validate() {
		return validate(getServer());
	}
	
	protected CreateServerValidation validate(IServer server) {
		String home = server.getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String)null);
		if( null == home ) {
			return validationErrorResponse("Server home must not be null", DefaultServerAttributes.SERVER_HOME_DIR, Activator.BUNDLE_ID);
		}
		if(!(new File(home).exists())) {
			return validationErrorResponse("Server home must exist", DefaultServerAttributes.SERVER_HOME_DIR, Activator.BUNDLE_ID);
		}
		return new CreateServerValidation(Status.OK_STATUS, null);
	}
	
	
	/*
	 * Starting a server:
	 * 
	 * This is the primary task of a server adapter and any rsp extension.  
	 * This usually entails running some command line to kick off an additional
	 * process. 
	 * 
	 * A server delegate has an opportunity to veto a startup if 
	 * the server is not in a valid state in which to begin starting up, 
	 * such as invalid fields, unable to locate required resources, etc. 
	 */
	
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

	/*
	 * We choose to isolate most of the launch code itself in its own class. 
	 * Launchers can be java-based or simple command based. We'll see examples
	 * of both of them here. The start launcher will be java based. 
	 * The shutdown launcher will issue a java command as well, however, 
	 * it will do so using the more primitive generic command launcher.  
	 *
	 */
	protected IServerStartLauncher getStartLauncher() {
		return new WonkaStartLauncher(this);
	}
	
	/*
	 * The process for starting a server can be a bit involved. 
	 * First, you'll want to verify again that the server is in position to be started. 
	 * Second, you'll want to set the mode that your server is being run in (run, debug, etc)
	 * 
	 * Next, you must set your server to a 'starting' state. 
	 * 
	 * 
	 * After that, before issuing the command to start, we launch a polling mechanism. 
	 * Each runtime may have a different polling mechanism to know when their server is up. 
	 * Some runtimes may create a marker or lock file when the server is running, and 
	 * delete it on shutdown. Others may require a remote connection over a remote API 
	 * like RMI, JMX, or similar, in order to verify the server is up and running.  
	 * Some may simply check a web url where they expect to be able to verify the server 
	 * is now listening for connections. 
	 * 
	 * Next, the command to start the server is issued (see launcher.launch(mode))
	 * 
	 * We then store the results of this launch and register it in a model so 
	 * we don't lose track of it. 
	 * 
	 * If the startup fails or has an obvious error, we should terminate the process
	 * and set the server to stopped. Otherwise, we let the poller check every so often 
	 * to see whether the startup completion signal has arrived or not. 
	 * 
	 */
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

	/*
	 * The following two methods are used if a client simply asks the RSP
	 * for the launch command directly and chooses to launch the Wonka Server
	 * on its own, rather than through the RSP.  You may choose to reject
	 * these requests by returning a not-ok status to alert the 
	 * client that you do not like what they are doing ;)  
	 */
	@Override
	public IStatus clientSetServerStarting(ServerStartingAttributes attr) {
		setServerState(STATE_STARTING, true);
		if( attr.isInitiatePolling()) {
			launchPoller(IServerStatePoller.SERVER_STATE.UP);
		}
		return Status.OK_STATUS;
	}

	@Override
	public IStatus clientSetServerStarted(LaunchParameters attr) {
		setServerState(STATE_STARTED, true);
		return Status.OK_STATUS;
	}
	/*
	 * Shutting down the server is similar to starting up. 
	 * First, we set the state to stopping. Then we launch our 
	 * polling mechanism. Finally, we launch the shutdown command. 
	 * If the attempt fails, we mark the server back in its started state. 
	 * Otherwise, the poller will inform us when it has completed its shutdown. 
	 */

	protected IServerShutdownLauncher getStopLauncher() {
		return new WonkaShutdownLauncher(this);
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
	
	protected void launchPoller(IServerStatePoller.SERVER_STATE expectedState) {
		IPollResultListener listener = expectedState == IServerStatePoller.SERVER_STATE.DOWN ? 
				shutdownServerResultListener() : launchServerResultListener();
		IServerStatePoller poller = getPoller(expectedState);
		PollThreadUtils.pollServer(getServer(), expectedState, poller, listener);
	}
	
	/*
	 * Our server "Wonka Server" will create a marker file "wonka.started" when it starts, 
	 * and delete that file during shutdown to indicate the server is terminated. 
	 * 
	 * Therefore, checking for the existence of that file is enough to tell you if the 
	 * server is currently up or down. 
	 */
	protected IServerStatePoller getPoller(IServerStatePoller.SERVER_STATE expectedState) {
		return new AbstractPoller() {
			@Override
			protected SERVER_STATE onePing(IServer server) {
				String home = server.getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String)null);
				if( new File(home, "wonka.started").exists()) {
					return SERVER_STATE.UP;
				}
				return SERVER_STATE.DOWN;
			}

			@Override
			protected String getThreadName() {
				return "Wonka Poller";
			}
			
		};
	}
	
	/*
	 * In the event that the server process is terminated, we choose to set
	 * the server state to stopped. 
	 *
	 * This will not be true for all servers. For some servers, the process 
	 * that launches a server simply kicks off other processes but eventually
	 * terminates even while the server is still up and running. 
	 * 
	 * How you handle a terminated launch process should be dependent on 
	 * your server implementation and its expected workflow. 
	 */
	@Override
	protected void processTerminated(IProcess p) {
		ILaunch l = p.getLaunch();
		if( l == getStartLaunch() ) {
			IProcess[] all = l.getProcesses();
			boolean allTerminated = true;
			for( int i = 0; i < all.length; i++ ) {
				allTerminated &= all[i].isTerminated();
			}
			if( allTerminated ) {
				setMode(null);
				setStartLaunch(null);
				setServerState(IServerDelegate.STATE_STOPPED);
			}
		}
		fireServerProcessTerminated(getProcessId(p));
	}

	

	
	/*
	 * Updating a server:
	 * The server delegate has an opportunity to veto any changes a 
	 * user makes to the server configuration if those changes might be 
	 * invalid or unacceptable changes.
	 * 
	 *  If some of the changes require additional work to update 
	 *  some custom backing models, the server delegate also has 
	 *  the opportunity to do so here. 
	 */

	@Override
	public void updateServer(IServer dummyServer, UpdateServerResponse resp) {
		String[] unchangeable = new String[] {ServerManagementAPIConstants.SERVER_HOME_DIR};
		// First, validate the changes
		IStatus stat = verifyUnchanged(dummyServer, getServer(), unchangeable);
		
		// We've already got errors? Return
		if( !stat.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(stat));
			return;
		}
		
		// Do next level validation
		CreateServerValidation validation = validate(dummyServer);
		if( !validation.getStatus().isOK()) {
			resp.setValidation(validation.toDao());
			return;
		}

	}
	protected IStatus verifyUnchanged(IServer ds, IServer server, String[] unchangeable) {
		for( int i = 0; i < unchangeable.length; i++ ) {
			String dsType = ds.getAttribute(unchangeable[i], (String)null);
			String type = server.getAttribute(unchangeable[i], (String)null);
			if( !isEqual(dsType, type)) {
				return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Field " + unchangeable[i] + " may not be changed");
			}
		}
		return Status.OK_STATUS;
	}

	
	
	/*
	 * Deployment, publishing, etc
	 * Not implemented in this example
	 */

	@Override
	public IStatus canAddDeployable(DeployableReference ref) {
		// No publishing allowed
		return Status.CANCEL_STATUS;
	}
	
	@Override
	public IStatus canRemoveDeployable(DeployableReference reference) {
		// No publishing allowed
		return Status.CANCEL_STATUS;
	}
	
	@Override
	public IStatus canPublish() {
		return Status.CANCEL_STATUS;
	}

	@Override
	protected void publishStart(int publishType) throws CoreException {
		throw new CoreException(Status.CANCEL_STATUS);
	}

	@Override
	protected void publishFinish(int publishType) throws CoreException {
		throw new CoreException(Status.CANCEL_STATUS);
	}

	@Override
	protected void publishDeployable(DeployableReference reference, 
			int publishRequestType, int modulePublishState) throws CoreException {
		throw new CoreException(Status.CANCEL_STATUS);
	}

	@Override
	public Attributes listDeploymentOptions() {
		CreateServerAttributesUtility util = new CreateServerAttributesUtility();
		return util.toPojo();
	}
	
	
	
	private boolean isEqual(String one, String two) {
		return one == null ? two == null : one.equals(two);
	}
	
}
