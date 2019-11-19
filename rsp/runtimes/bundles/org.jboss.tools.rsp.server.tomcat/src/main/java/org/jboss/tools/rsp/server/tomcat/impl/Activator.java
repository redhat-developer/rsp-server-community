package org.jboss.tools.rsp.server.tomcat.impl;

import java.io.InputStream;

import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.generic.GenericServerActivator;
import org.jboss.tools.rsp.server.generic.IServerDelegateProvider;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.ITomcatServerAttributes;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.TomcatServerDelegate;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends GenericServerActivator {
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

	protected IServerDelegateProvider getDelegateProvider() {
		return getDelegateProviderImpl();
	}
	public static IServerDelegateProvider getDelegateProviderImpl() {
		return new IServerDelegateProvider() {
			@Override
			public IServerDelegate createServerDelegate(String typeId, IServer server) {
				if( ITomcatServerAttributes.TOMCAT_90_SERVER_TYPE_ID.equals(typeId)) {
					return new TomcatServerDelegate(server);
				}
				return null;
			}
		};
	}

}
