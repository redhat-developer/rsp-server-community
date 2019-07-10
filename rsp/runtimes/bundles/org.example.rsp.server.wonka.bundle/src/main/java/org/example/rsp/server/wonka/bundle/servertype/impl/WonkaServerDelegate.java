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
import org.example.rsp.server.wonka.bundle.servertype.AbstractWonkaServerDelegate;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.UpdateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.CreateServerValidation;
import org.jboss.tools.rsp.server.spi.servertype.IServer;

public class WonkaServerDelegate extends AbstractWonkaServerDelegate {
	public WonkaServerDelegate(IServer server) {
		super(server);
		setServerState(ServerManagementAPIConstants.STATE_STOPPED);
	}
	protected IServerStartLauncher getStartLauncher() {
		return new WonkaStartLauncher(this);
	}
	
	protected IServerShutdownLauncher getStopLauncher() {
		return new WonkaShutdownLauncher(this);
	}
	@Override
	public void updateServer(IServer dummyServer, UpdateServerResponse resp) {
		updateServer(dummyServer, resp, 
				new String[] {ServerManagementAPIConstants.SERVER_HOME_DIR});
	}
	@Override
	protected CreateServerValidation validate(IServer server) {
		CreateServerValidation vd = super.validate(server);
		if( !vd.getStatus().isOK()) {
			return vd;
		}
		String home = server.getAttribute(ServerManagementAPIConstants.SERVER_HOME_DIR, (String)null);
		if( !(new File(home).exists())) {
			return validationErrorResponse("Server home dir must exist", 
					ServerManagementAPIConstants.SERVER_HOME_DIR, Activator.BUNDLE_ID);
		}
		return new CreateServerValidation(Status.OK_STATUS, null);
	}
}
