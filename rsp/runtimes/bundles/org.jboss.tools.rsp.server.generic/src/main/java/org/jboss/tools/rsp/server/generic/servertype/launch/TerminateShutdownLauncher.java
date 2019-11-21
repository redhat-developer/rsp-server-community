package org.jboss.tools.rsp.server.generic.servertype.launch;

import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.debug.core.DebugException;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.debug.core.model.IProcess;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class TerminateShutdownLauncher implements IServerShutdownLauncher {

	private GenericServerBehavior genericServerBehavior;
	private ILaunch startLaunch;

	public TerminateShutdownLauncher(GenericServerBehavior genericServerBehavior, ILaunch startLaunch) {
		this.genericServerBehavior = genericServerBehavior;
		this.startLaunch = startLaunch;
	}

	@Override
	public ILaunch launch(boolean force) throws CoreException {
		terminateAllProcesses(startLaunch);
		if( allProcessesTerminated(startLaunch)) {
			genericServerBehavior.setServerState(IServerDelegate.STATE_STOPPED);
		} else {
			genericServerBehavior.setServerState(IServerDelegate.STATE_STARTED);
		}
		return null;
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
	@Override
	public ILaunch getLaunch() {
		return null;
	}

	@Override
	public IServer getServer() {
		return this.genericServerBehavior.getServer();
	}

}
