/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.servertype;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.rsp.server.wonka.bundle.impl.Activator;
import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.api.dao.LaunchParameters;
import org.jboss.tools.rsp.api.dao.ServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerStartingAttributes;
import org.jboss.tools.rsp.api.dao.StartServerResponse;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.MultiStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.eclipse.osgi.util.NLS;
import org.jboss.tools.rsp.server.ServerCoreActivator;
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
import org.jboss.tools.rsp.server.wildfly.servertype.IJBossServerAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractWonkaServerDelegate extends AbstractServerDelegate {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractWonkaServerDelegate.class);
	public static final String START_LAUNCH_SHARED_DATA = "AbstractWonkaServerDelegate.startLaunch";
	
	public AbstractWonkaServerDelegate(IServer server) {
		super(server);
	}

	protected abstract IServerStartLauncher getStartLauncher();
	
	protected abstract IServerShutdownLauncher getStopLauncher();

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
	
	protected void launchPoller(IServerStatePoller.SERVER_STATE expectedState) {
		IPollResultListener listener = expectedState == IServerStatePoller.SERVER_STATE.DOWN ? 
				shutdownServerResultListener() : launchServerResultListener();
		IServerStatePoller poller = getPoller(expectedState);
		PollThreadUtils.pollServer(getServer(), expectedState, poller, listener);
	}
	
	/*
	 * Default implementation, subclasses can override.
	 */
	protected IServerStatePoller getPoller(IServerStatePoller.SERVER_STATE expectedState) {
		return new AbstractPoller() {
			@Override
			protected SERVER_STATE onePing(IServer server) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected String getThreadName() {
				return "Wonka Poller";
			}
			
		};
	}
	
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
	
	@Override
	protected void setServerState(int state, boolean fire) {
		if( state == IServerDelegate.STATE_STOPPED ) {
			markAllDeploymentsStopped();
		}
		super.setServerState(state, fire);
	}

	protected void markAllDeploymentsStopped() {
		for( DeployableState ds : getServerPublishModel().getDeployableStates()) {
			getServerPublishModel().setDeployableState(ds.getReference(), IServerDelegate.STATE_STOPPED);
		}
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

	public void updateServer(IServer dummyServer, UpdateServerResponse resp,
			String[] unchangeableFields) {
		if( preUpdateServerValidationErrors(dummyServer, resp, unchangeableFields)) {
			return;
		}

		// Now, perform any changes that need to be done at the delegate level
		List<DeployableState> existing = getServerPublishModel().getDeployableStates();
		List<DeployableState> updated = dummyServer.getDelegate().getServerPublishModel().getDeployableStates();
		
		for( DeployableState ds : existing ) {
			getServerPublishModel().fillOptionsFromCache(ds.getReference());
		}

		for( DeployableState ds : updated ) {
			dummyServer.getDelegate().getServerPublishModel().fillOptionsFromCache(ds.getReference());
		}

		// Calculate the delta on modules?
		List<DeployableReference> unchanged = new ArrayList<>();
		List<DeployableReference> removed = new ArrayList<>();
		for( DeployableState ds : existing ) {
			DeployableState matching = findExactMatch(ds.getReference(), updated);
			if( matching == null ) {
				removed.add(ds.getReference());
			} else {
				unchanged.add(ds.getReference());
				updated.remove(ds);
			}
		}
		MultiStatus ret = new MultiStatus(ServerCoreActivator.BUNDLE_ID, 0, 
				NLS.bind("Updating Server {0}...", getServer().getName()), null);

		// Handle removals
		for( DeployableReference removed1 : removed ) {
			ret.add(getServerPublishModel().removeDeployable(removed1));
		}
		// Whatever is left is 'added'
		for( DeployableState ds : updated ) {
			ret.add(getServerPublishModel().addDeployable(ds.getReference()));
		}
		
		if( !ret.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(ret));
		}
	}
	
	private boolean preUpdateServerValidationErrors(
			IServer dummyServer, UpdateServerResponse resp,
			String[] unchangeableFields) {
		
		// First, validate the changes
		IStatus stat = verifyUnchanged(dummyServer, getServer(), unchangeableFields);
		
		// We've already got errors? Return
		if( !stat.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(stat));
			return true;
		}
		
		// Do next level validation
		CreateServerValidation validation = validate(dummyServer);
		if( !validation.getStatus().isOK()) {
			resp.setValidation(validation.toDao());
			return true;
		}
		
		stat = verifyDeploymentChanges(dummyServer, getServer());
		if( !stat.isOK()) {
			resp.getValidation().setStatus(StatusConverter.convert(stat));
			return true;
		}
		return false;
	}
	private DeployableState findExactMatch(DeployableReference reference, List<DeployableState> updated) {
		String label = reference.getLabel();
		String path = reference.getPath();
		for( DeployableState ds : updated) {
			if( !ds.getReference().getLabel().equals(label))
				continue;
			if( !ds.getReference().getPath().equals(path))
				continue;
			
			Map<String, Object> origOptions = reference.getOptions();
			Map<String, Object> updatedOptions = ds.getReference().getOptions();
			if( origOptions == null && updatedOptions == null )
				return ds;
			if( origOptions == null )
				continue;
			if( origOptions.equals(updatedOptions)) 
				return ds;
		}
		return null;
	}

	private IStatus verifyDeploymentChanges(IServer dummyServer, IServer server) {
		return Status.OK_STATUS;
	}

	private boolean isEqual(String one, String two) {
		return one == null ? two == null : one.equals(two);
	}
	
	@Override
	public void setDefaults(IServerWorkingCopy server) {
		server.setAttribute(IJBossServerAttributes.LAUNCH_OVERRIDE_BOOLEAN, false);
	}

}
