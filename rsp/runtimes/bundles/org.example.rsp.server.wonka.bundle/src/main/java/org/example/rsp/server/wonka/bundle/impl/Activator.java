/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.spi.RSPExtensionBundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends RSPExtensionBundle {

	public static final String BUNDLE_ID = "org.example.rsp.server.wonka.bundle";

	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.info("Bundle {} starting...", context.getBundle().getSymbolicName());

		addExtensions(ServerCoreActivator.BUNDLE_ID, context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.info("Bundle {} stopping...", context.getBundle().getSymbolicName());

		removeExtensions(ServerCoreActivator.BUNDLE_ID, context);
	}

	@Override
	protected void addExtensions() {
		ExtensionHandler.addExtensions(LauncherSingleton.getDefault().getLauncher().getModel());
	}
	
	@Override
	protected void removeExtensions() {
		ExtensionHandler.removeExtensions(LauncherSingleton.getDefault().getLauncher().getModel());
	}
}
