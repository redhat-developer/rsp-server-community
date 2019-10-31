package org.jboss.tools.rsp.server.felix.impl;

import org.jboss.tools.rsp.server.felix.impl.discovery.FelixServerBeanTypeProvider;
import org.jboss.tools.rsp.server.felix.runtimes.download.DownloadRuntimesProvider;
import org.jboss.tools.rsp.server.felix.servertype.impl.FelixServerTypes;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;


public class ExtensionHandler {
	
	private static final IServerType[] TYPES = {
			FelixServerTypes.FELIX_60_SERVER_TYPE
	};

	private ExtensionHandler() {
		//inhibit instantiation
	}
	
	private static FelixServerBeanTypeProvider beanProvider = null;
	private static DownloadRuntimesProvider dlrtProvider = null;	
	public static void addExtensions(IServerManagementModel model) {
		beanProvider = new FelixServerBeanTypeProvider();
		dlrtProvider = new DownloadRuntimesProvider(model);
		model.getServerBeanTypeManager().addTypeProvider(beanProvider);
		model.getServerModel().addServerTypes(TYPES);
		model.getDownloadRuntimeModel().addDownloadRuntimeProvider(dlrtProvider);
	}
	
	public static void removeExtensions(IServerManagementModel model) {
		model.getServerBeanTypeManager().removeTypeProvider(new FelixServerBeanTypeProvider());
		model.getServerModel().removeServerTypes(TYPES);
		model.getDownloadRuntimeModel().removeDownloadRuntimeProvider(dlrtProvider);
	}
}
