/*************************************************************************************
 * Copyright (c) 2013-2018 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     JBoss by Red Hat - Initial implementation.
 ************************************************************************************/
package org.example.rsp.server.wonka.bundle.runtimes.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.example.rsp.server.wonka.bundle.servertype.impl.WonkaServerTypes;
import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeRunner;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;
import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

/**
 * Pull runtimes from a stacks file and return them to runtimes framework
 */
public class DownloadRuntimesProvider implements IDownloadRuntimesProvider {
	private IServerManagementModel model;

	public DownloadRuntimesProvider(IServerManagementModel model) {
		this.model = model;
	}
	@Override
	public IDownloadRuntimeRunner getDownloadRunner(DownloadRuntime arg0) {
		return new AbstractLicenseOnlyDownloadExecutor(arg0, model) {
			@Override
			protected IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm) {
				Map<String,Object> attributes = new HashMap<>();
				attributes.put(DefaultServerAttributes.SERVER_HOME_DIR, newHome);
				
				Set<String> serverIds = getServerModel().getServers().keySet();
				String suggestedId = new File(newHome).getName();
				String chosenId = getUniqueServerId(suggestedId, serverIds);

				CreateServerResponse response = getServerModel().createServer(
						WonkaServerTypes.WONKA_1_0_TYPE.getId(), 
						chosenId, attributes);
				return StatusConverter.convert(response.getStatus());
			}
		};
	}

	@Override
	public DownloadRuntime[] getDownloadableRuntimes(IProgressMonitor arg0) {
		DownloadRuntime dlrt = new DownloadRuntime("wonka.1.0", "Wonka Server 1.0", 
				"1.0.0.Final", "https://github.com/robstryker/extend-rsp-example/raw/master/wonka-runtime/releases/wonka-runtime-1.0-SNAPSHOT.jar");
		dlrt.setInstallationMethod(IRuntimeInstaller.BINARY_INSTALLER);
		dlrt.setLicenseURL("https://www.gnu.org/licenses/lgpl-3.0.txt");
		return new DownloadRuntime[] { dlrt };
	}

	@Override
	public String getId() {
		return "wonka.dlrt";
	}

}
