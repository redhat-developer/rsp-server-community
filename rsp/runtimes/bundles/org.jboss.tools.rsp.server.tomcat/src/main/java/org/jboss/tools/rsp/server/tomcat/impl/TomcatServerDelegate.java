/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.tomcat.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableReference;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.IPublishControllerWithOptions;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerSuffixPublishController;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerType;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.ITomcatServerAttributes;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.TomcatContextRootSupport;

public class TomcatServerDelegate extends GenericServerBehavior implements IServerDelegate {

	public TomcatServerDelegate(IServer server, JSONMemento behaviorMemento) {
		super(server, behaviorMemento);
	}
	@Override
	public void setDependentDefaults(IServerWorkingCopy server) {
		// Do nothing
		try {
			String baseDir = server.getAttribute(ITomcatServerAttributes.SERVER_BASE_DIR, (String)null); 
			if( baseDir == null || baseDir.length() == 0) {
				String currentHome = server.getAttribute(ITomcatServerAttributes.SERVER_HOME, (String)null);
				server.setAttribute(ITomcatServerAttributes.SERVER_BASE_DIR, currentHome);
			}

			CommandLineDetails det = getStartLauncher().getLaunchCommand("run");
			String progArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_PROGRAM_ARGS);
			String vmArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_VM_ARGS);
			if(progArgs == null || progArgs.isEmpty()) {
				progArgs = "";
			}
			if(vmArgs == null || vmArgs.isEmpty()) {
				vmArgs = "";
			}
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_BOOLEAN, false);
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_PROGRAM_ARGS, progArgs);
			server.setAttribute(GenericServerType.JAVA_LAUNCH_OVERRIDE_VM_ARGS, vmArgs);
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}
	
	@Override
	public String[] getDeploymentUrls(String strat, String baseUrl, String deployableOutputName, DeployableState ds) {
		return new TomcatContextRootSupport().getDeploymentUrls(strat, baseUrl, deployableOutputName, ds); 
	}

	// If tomcat is stopped nad user removes blah.war, We need to delete the exploded folder as well

	protected IPublishControllerWithOptions createPublishController() {
		JSONMemento publishMemento = this.getBehaviorMemento().getChild("publish");
		String deployPath = publishMemento.getString("deployPath");
		String approvedSuffixes = publishMemento.getString("approvedSuffixes");
		String[] suffixes = approvedSuffixes == null ? null : approvedSuffixes.split(",", -1);
		String supportsExploded = publishMemento.getString("supportsExploded");
		boolean exploded = (supportsExploded == null ? false : Boolean.parseBoolean(supportsExploded));
		return new TomcatServerSuffixPublishController(
				getServer(), this, 
				suffixes, deployPath, exploded);
	}
	
	private static class TomcatServerSuffixPublishController extends GenericServerSuffixPublishController {
		public TomcatServerSuffixPublishController(IServer server, IServerDelegate delegate, String[] approvedSuffixes,
				String deploymentPath, boolean supportsExploded) {
			super(server, delegate, approvedSuffixes, deploymentPath, supportsExploded);
		}


		protected int removeModule(DeployableReference reference, 
				int publishRequestType, int modulePublishState) throws CoreException {
			int runState = getServer().getDelegate().getServerState().getState();
			if( runState == IServerDelegate.STATE_STOPPED) {
				// clean up exploded folder from zipped deployment
				Path destPath = getDestinationPath(reference);
				if( destPath.toFile().isFile()) {
					// we are deleting a zip. Need to also delete the exploded folder
					String fname = destPath.getFileName().toString();
					if( fname.contains(".")) {
						int lastDot = fname.lastIndexOf(".");
						String prefix = lastDot == -1 ? fname : fname.substring(0, lastDot);
						Path explodedDirPossible = new File(destPath.getParent().toFile(), prefix).toPath();
						if( explodedDirPossible.toFile().exists()) {
							try {
								super.completeDelete(explodedDirPossible);
							} catch(IOException ioe) {
								// ignore
							}
						}
					}
				}
			}
			return super.removeModule(reference, publishRequestType, modulePublishState);
		}
	}
	
}
