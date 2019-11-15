package org.jboss.tools.rsp.server.felix.impl;

import java.io.InputStream;

import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.generic.GenericServerActivator;
import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends GenericServerActivator {
	public static final String BUNDLE_ID = "org.jboss.tools.rsp.server.felix";
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

	protected GenericServerExtensionModel createGenericExtensionModel(IServerManagementModel rspModel) {
		// TODO replace with actual generic when fully implemented
		return new FelixGenericServerExtensionModel(rspModel, getServerTypeModelStream());
		//return new GenericServerExtensionModel(rspModel, getServerTypeModelStream());
	}
	@Override
	protected String getBundleId() {
		return BUNDLE_ID;
	}
	@Override
	protected InputStream getServerTypeModelStream() {
		return getServerTypeModelStreamImpl();
	}
	
	public static final InputStream getServerTypeModelStreamImpl() {
		return Activator.class.getResourceAsStream("/servers.json");
	}

}
