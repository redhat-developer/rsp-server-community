package org.jboss.tools.rsp.server.generic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;

public class GenericServerExtensionModel {
	private IServerManagementModel rspModel;
	private HashMap<String, GenericServerTypeExtensionModel> map;
	public GenericServerExtensionModel(IServerManagementModel rspModel, 
			IServerDelegateProvider delegateProvider, InputStream is) {

		this.rspModel = rspModel;
		this.map = new HashMap<>();
		
		JSONMemento memento = JSONMemento.createReadRoot(is);
		JSONMemento[] serverTypes = memento.getChild("serverTypes").getChildren();
		for( int i = 0; i < serverTypes.length; i++ ) {
			String id = serverTypes[i].getNodeName();
			GenericServerTypeExtensionModel oneType = 
					loadOneServer(serverTypes[i], delegateProvider);
			this.map.put(id, oneType);
		}
	}

	private GenericServerTypeExtensionModel loadOneServer(JSONMemento serverMemento, IServerDelegateProvider delegateProvider) {
		return new GenericServerTypeExtensionModel(getRspModel(), delegateProvider, serverMemento);
	}


	public void registerExtensions() {
		ArrayList<GenericServerTypeExtensionModel> sub = new ArrayList<>(map.values());
		for( GenericServerTypeExtensionModel one : sub ) {
			if( one.getMyDownloadRuntimeProvider() != null ) 
				getRspModel().getDownloadRuntimeModel().addDownloadRuntimeProvider(one.getMyDownloadRuntimeProvider());
			if( one.getMyDiscovery() != null )
				getRspModel().getServerBeanTypeManager().addTypeProvider(one.getMyDiscovery());
			if( one.getMyServerType() != null )
				getRspModel().getServerModel().addServerType( one.getMyServerType() );
		}
	}

	public void unregisterExtensions() {
		ArrayList<GenericServerTypeExtensionModel> sub = new ArrayList<>(map.values());
		for( GenericServerTypeExtensionModel one : sub ) {
			if( one.getMyDownloadRuntimeProvider() != null ) 
				getRspModel().getDownloadRuntimeModel().removeDownloadRuntimeProvider(one.getMyDownloadRuntimeProvider());
			if( one.getMyDiscovery() != null )
				getRspModel().getServerBeanTypeManager().removeTypeProvider(one.getMyDiscovery());
			if( one.getMyServerType() != null )
				getRspModel().getServerModel().removeServerType( one.getMyServerType() );
		}
	}

	public IServerManagementModel getRspModel() {
		return rspModel;
	}
}
