/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.felix.servertype.impl;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.ServerActionRequest;
import org.jboss.tools.rsp.api.dao.ServerActionWorkflow;
import org.jboss.tools.rsp.api.dao.ServerHandle;
import org.jboss.tools.rsp.api.dao.ServerState;
import org.jboss.tools.rsp.api.dao.WorkflowResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.launching.utils.IStatusRunnableWithProgress;
import org.jboss.tools.rsp.server.felix.impl.Activator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.model.ServerModelListenerAdapter;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class FelixWipeCacheRestartAction extends ServerActionWorkflow {
	public static final String ACTION_WIPE_CACHE_RESTART_LABEL = "Wipe container cache and restart server";
	public static final String ACTION_WIPE_CACHE_RESTART_ID = "FelixWipeCacheRestartAction";
	private IServerDelegate felixServerDelegate;

	public static ServerActionWorkflow getInitialWorkflow(IServerDelegate felixServerDelegate) {
		WorkflowResponse workflow = new WorkflowResponse();
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_WIPE_CACHE_RESTART_LABEL)));
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_WIPE_CACHE_RESTART_ID, ACTION_WIPE_CACHE_RESTART_LABEL, workflow);
		return action;
	}

	public FelixWipeCacheRestartAction(IServerDelegate felixServerDelegate) {
		this.felixServerDelegate = felixServerDelegate;
	}

	public WorkflowResponse handle(ServerActionRequest req) {
		String serverId = req.getServerId();
		if( serverId == null ) {
			return AbstractServerDelegate.cancelWorkflowResponse();
		}
		IServer server = this.felixServerDelegate.getServer().getServerModel().getServer(serverId);
		if( server == null ) {
			return AbstractServerDelegate.cancelWorkflowResponse();
		}
		IStatusRunnableWithProgress stopCleanRestart = new IStatusRunnableWithProgress() {
			@Override
			public IStatus run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				return backgroundJobImpl(server);
			}
		};
		server.getServerManagementModel().getJobManager().scheduleJob(
				ACTION_WIPE_CACHE_RESTART_LABEL, stopCleanRestart);
		
		return AbstractServerDelegate.okWorkflowResponse();
	}
	
	private IStatus backgroundJobImpl(IServer server) {
		boolean stopped = stopServerSynchronous(server);
		if( !stopped ) {
			return Status.CANCEL_STATUS;
		}
		
		// Now delete the cache
		Path p = getCacheFolder(server);
		deleteDirectory(p.toFile());
		
		// Now start the server 
		felixServerDelegate.start("run");
		return Status.OK_STATUS;
	}
	
	boolean deleteDirectory(File directoryToBeDeleted) {
		if( directoryToBeDeleted == null || !directoryToBeDeleted.exists())
			return false;
		
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}
	private Path getCacheFolder(IServer server) {
		String serverHome =  server.getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String) null);
		if( serverHome == null )
			return null;
		return new File(serverHome, "felix-cache").toPath();
	}

	private boolean stopServerSynchronous(IServer server) {
		if( felixServerDelegate.getServerState().getState() == IServerDelegate.STATE_STOPPED) {
			return true;
		}

		final String id = server.getId();
		final CountDownLatch stoppedProper = new CountDownLatch(1);
		server.getServerModel().addServerModelListener(new ServerModelListenerAdapter() {
			public void serverStateChanged(ServerHandle server, ServerState state) {
				if( server.getId().equals(id) && state.getState() == IServerDelegate.STATE_STOPPED) {
					stoppedProper.countDown();
				}
			}
		});
		felixServerDelegate.stop(false);
		try {
			boolean done = stoppedProper.await(15,TimeUnit.SECONDS);
			return done;
		} catch(InterruptedException ie) {
			return false;
		}
	}
}
