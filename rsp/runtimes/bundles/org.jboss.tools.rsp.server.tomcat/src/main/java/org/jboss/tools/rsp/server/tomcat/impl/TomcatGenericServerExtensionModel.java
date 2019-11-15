package org.jboss.tools.rsp.server.tomcat.impl;

import java.io.InputStream;

import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.TomcatServerTypes;

public class TomcatGenericServerExtensionModel extends GenericServerExtensionModel {

	public TomcatGenericServerExtensionModel(IServerManagementModel rspModel, InputStream is) {
		super(rspModel, is);
	}
	// TODO remove this stuff and make the server type part of api too
	public void registerExtensions() {
		super.registerExtensions();
		getRspModel().getServerModel().addServerTypes(new IServerType[] {TomcatServerTypes.TC9_SERVER_TYPE});
	}
	public void unregisterExtensions() {
		super.unregisterExtensions();
		getRspModel().getServerModel().removeServerTypes(new IServerType[] {TomcatServerTypes.TC9_SERVER_TYPE});
	}
}