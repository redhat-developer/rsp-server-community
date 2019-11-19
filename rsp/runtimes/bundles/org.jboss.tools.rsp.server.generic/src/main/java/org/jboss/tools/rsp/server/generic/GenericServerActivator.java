package org.jboss.tools.rsp.server.generic;

import java.io.InputStream;

import org.jboss.tools.rsp.server.LauncherSingleton;
import org.jboss.tools.rsp.server.spi.RSPExtensionBundle;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericServerActivator extends RSPExtensionBundle {
	private static final Logger LOG = LoggerFactory.getLogger(GenericServerActivator.class);
	
	private IServerManagementModel rspModel;

	private GenericServerExtensionModel extensionModel;
	
	protected abstract String getBundleId();
	
	protected abstract InputStream getServerTypeModelStream();
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.info("Bundle {} starting...", context.getBundle().getSymbolicName());
		addExtensions(getBundleId(), context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.info("Bundle {} stopping...", context.getBundle().getSymbolicName());
		removeExtensions(getBundleId(), context);
	}

	@Override
	protected void addExtensions() {
		rspModel = LauncherSingleton.getDefault().getLauncher().getModel();
		addExtensions(rspModel);
	}

	@Override
	protected void removeExtensions() {
		removeExtensions(rspModel);
	}

	public void addExtensions(IServerManagementModel rspModel) {
		this.extensionModel = createGenericExtensionModel(rspModel);
		this.extensionModel.registerExtensions();
	}

	public void removeExtensions(IServerManagementModel rspModel) {
		this.extensionModel.unregisterExtensions();
	}
	
	protected GenericServerExtensionModel createGenericExtensionModel(IServerManagementModel rspModel) {
		return new GenericServerExtensionModel(rspModel, getDelegateProvider(), getServerTypeModelStream());
	}
	
	/*
	 * Subclass should override
	 */
	protected IServerDelegateProvider getDelegateProvider() {
		return null;
	}
}
