package org.jboss.tools.rsp.server.karaf.servertype.impl;

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
import org.jboss.tools.rsp.server.karaf.impl.Activator;
import org.jboss.tools.rsp.server.model.AbstractServerDelegate;
import org.jboss.tools.rsp.server.spi.model.ServerModelListenerAdapter;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class KarafWipeCacheRestartAction extends ServerActionWorkflow {
	public static final String ACTION_WIPE_CACHE_RESTART_LABEL = "Wipe container cache and restart server";
	public static final String ACTION_WIPE_CACHE_RESTART_ID = "KarafWipeCacheRestartAction";
	private IServerDelegate karafServerDelegate;

	public static ServerActionWorkflow getInitialWorkflow(IServerDelegate felixServerDelegate) {
		WorkflowResponse workflow = new WorkflowResponse();
		workflow.setStatus(StatusConverter.convert(
				new Status(IStatus.INFO, Activator.BUNDLE_ID, ACTION_WIPE_CACHE_RESTART_LABEL)));
		ServerActionWorkflow action = new ServerActionWorkflow(
				ACTION_WIPE_CACHE_RESTART_ID, ACTION_WIPE_CACHE_RESTART_LABEL, workflow);
		return action;
	}

	public KarafWipeCacheRestartAction(IServerDelegate karafServerDelegate) {
		this.karafServerDelegate = karafServerDelegate;
	}

	public WorkflowResponse handle(ServerActionRequest req) {
		String serverId = req.getServerId();
		if( serverId == null ) {
			return AbstractServerDelegate.cancelWorkflowResponse();
		}
		IServer server = this.karafServerDelegate.getServer().getServerModel().getServer(serverId);
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
		karafServerDelegate.start("run");
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
		return new File(serverHome, "data").toPath();
	}

	private boolean stopServerSynchronous(IServer serverOutter) {
		if( karafServerDelegate.getServerState().getState() == IServerDelegate.STATE_STOPPED) {
			return true;
		}

		final String id = serverOutter.getId();
		final CountDownLatch stoppedProper = new CountDownLatch(1);
		serverOutter.getServerModel().addServerModelListener(new ServerModelListenerAdapter() {
			public void serverStateChanged(ServerHandle server, ServerState state) {
				if( server.getId().equals(id) && state.getState() == IServerDelegate.STATE_STOPPED) {
					serverOutter.getServerModel().removeServerModelListener(this);
					stoppedProper.countDown();
				}
			}
		});
		karafServerDelegate.stop(false);
		try {
			boolean done = stoppedProper.await(15,TimeUnit.SECONDS);
			return done;
		} catch(InterruptedException ie) {
			return false;
		}
	}
}
