package org.jboss.tools.rsp.server.felix.impl;

import java.io.InputStream;

import org.jboss.tools.rsp.server.felix.servertype.impl.FelixServerTypes;
import org.jboss.tools.rsp.server.generic.GenericServerExtensionModel;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.servertype.IServerType;

public class FelixGenericServerExtensionModel extends GenericServerExtensionModel {

	public FelixGenericServerExtensionModel(IServerManagementModel rspModel, InputStream is) {
		super(rspModel, is);
	}


	// TODO remove this stuff and make the server type part of api too
	public void registerExtensions() {
		super.registerExtensions();
		getRspModel().getServerModel().addServerTypes(new IServerType[] {FelixServerTypes.FELIX_60_SERVER_TYPE});
	}
	public void unregisterExtensions() {
		super.unregisterExtensions();
		getRspModel().getServerModel().removeServerTypes(new IServerType[] {FelixServerTypes.FELIX_60_SERVER_TYPE});
	}}
