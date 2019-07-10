/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.impl;

import org.example.rsp.server.wonka.bundle.runtimes.download.DownloadRuntimesProvider;
import org.example.rsp.server.wonka.bundle.servertype.impl.WonkaServerTypes;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class ExtensionHandler {

	private static final IServerType[] TYPES = {
			WonkaServerTypes.WONKA_1_0_TYPE
	};

	private ExtensionHandler() {
		// inhibit instantiation
	}

	private static WonkaServerBeanTypeProvider beanProvider = null;
	private static DownloadRuntimesProvider dlrtProvider = null;
	public static void addExtensions(IServerManagementModel model) {
		beanProvider = new WonkaServerBeanTypeProvider();
		dlrtProvider = new DownloadRuntimesProvider(model);
		model.getServerBeanTypeManager().addTypeProvider(beanProvider);
		model.getServerModel().addServerTypes(TYPES);
		model.getDownloadRuntimeModel().addDownloadRuntimeProvider(dlrtProvider);
	}
	
	public static void removeExtensions(IServerManagementModel model) {
		model.getServerBeanTypeManager().removeTypeProvider(beanProvider);
		model.getServerModel().removeServerTypes(TYPES);
		model.getDownloadRuntimeModel().removeDownloadRuntimeProvider(dlrtProvider);
	}
}