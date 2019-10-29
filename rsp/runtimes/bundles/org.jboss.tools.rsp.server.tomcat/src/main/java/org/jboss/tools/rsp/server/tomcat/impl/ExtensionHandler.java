package org.jboss.tools.rsp.server.tomcat.impl;

import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.tomcat.runtimes.download.DownloadRuntimesProvider;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.TomcatServerTypes;


public class ExtensionHandler {
	
	private static final IServerType[] TYPES = {
			TomcatServerTypes.TC9_SERVER_TYPE
	};

	private ExtensionHandler() {
		//inhibit instantiation
	}
	
	private static TomcatServerBeanTypeProvider beanProvider = null;
	private static DownloadRuntimesProvider dlrtProvider = null;	
	public static void addExtensions(IServerManagementModel model) {
		beanProvider = new TomcatServerBeanTypeProvider();
		dlrtProvider = new DownloadRuntimesProvider(model);
		model.getServerBeanTypeManager().addTypeProvider(beanProvider);
		model.getServerModel().addServerTypes(TYPES);
		model.getDownloadRuntimeModel().addDownloadRuntimeProvider(dlrtProvider);
	}
	
	public static void removeExtensions(IServerManagementModel model) {
		model.getServerBeanTypeManager().removeTypeProvider(new TomcatServerBeanTypeProvider());
		model.getServerModel().removeServerTypes(TYPES);
		model.getDownloadRuntimeModel().removeDownloadRuntimeProvider(dlrtProvider);
	}
}
