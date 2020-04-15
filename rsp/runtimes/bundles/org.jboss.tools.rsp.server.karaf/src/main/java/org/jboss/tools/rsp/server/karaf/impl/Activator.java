package org.jboss.tools.rsp.server.karaf.impl;

import java.io.InputStream;

import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.ServerCoreActivator;
import org.jboss.tools.rsp.server.generic.GenericServerActivator;
import org.jboss.tools.rsp.server.generic.GenericServerBehaviorProvider;
import org.jboss.tools.rsp.server.generic.IServerBehaviorProvider;
import org.jboss.tools.rsp.server.generic.IServerBehaviorFromJSONProvider;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.karaf.servertype.impl.KarafServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends GenericServerActivator {
	public static final String BUNDLE_ID = "org.jboss.tools.rsp.server.karaf";
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
	@Override
	public IServerBehaviorFromJSONProvider getDelegateProvider() {
		return getDelegateProviderImpl();
	}
	public static IServerBehaviorFromJSONProvider getDelegateProviderImpl() {
		return new IServerBehaviorFromJSONProvider() {
			@Override
			public IServerBehaviorProvider loadBehaviorFromJSON(String serverTypeId, JSONMemento behaviorMemento) {
				return new GenericServerBehaviorProvider(behaviorMemento) {
					@Override
					public GenericServerBehavior createServerDelegate(String typeId, IServer server) {
						return new KarafServerDelegate(server, behaviorMemento);
					}
				};
			}
		};
	}
	
}
