package org.jboss.tools.rsp.server.tomcat.impl;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.spi.RSPExtensionBundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends RSPExtensionBundle {
	public static final String BUNDLE_ID = "org.jboss.tools.rsp.server.tomcat";
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
