/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.tomcat.impl;

import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerType;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;

public class TomcatServerDelegate extends GenericServerBehavior implements IServerDelegate {

	public TomcatServerDelegate(IServer server, JSONMemento behaviorMemento) {
		super(server, behaviorMemento);
	}
	@Override
	public void setDependentDefaults(IServerWorkingCopy server) {
		// Do nothing
		try {
			CommandLineDetails det = getStartLauncher().getLaunchCommand("run");
			String progArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_PROGRAM_ARGS);
			String vmArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_VM_ARGS);
			if(progArgs != null && !progArgs.isEmpty()) {
				progArgs = "";
			}
			if(vmArgs != null && !vmArgs.isEmpty()) {
				vmArgs = "";
			}
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_BOOLEAN, false);
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_PROGRAM_ARGS, progArgs);
			server.setAttribute(GenericServerType.JAVA_LAUNCH_OVERRIDE_VM_ARGS, vmArgs);
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}
}
