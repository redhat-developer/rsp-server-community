package org.jboss.tools.rsp.server.generic;

import java.io.InputStream;
import java.util.ArrayList;

import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.server.generic.discovery.ExplodedManifestDiscovery;
import org.jboss.tools.rsp.server.generic.discovery.GenericServerBeanTypeProvider;
import org.jboss.tools.rsp.server.generic.discovery.JarManifestDiscovery;
import org.jboss.tools.rsp.server.generic.runtimes.download.GenericDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;

public class GenericServerExtensionModel {
	private GenericServerBeanTypeProvider myDiscovery;
	private GenericDownloadRuntimesProvider myDownloadRuntimeProvider;
	private IServerManagementModel rspModel;
	
	public GenericServerExtensionModel(IServerManagementModel rspModel, InputStream is) {
		this.rspModel = rspModel;
		
		JSONMemento memento = JSONMemento.createReadRoot(is);
		JSONMemento[] serverTypes = memento.getChild("servers").getChildren();
		for( int i = 0; i < serverTypes.length; i++ ) {
			String serverTypeId = serverTypes[i].getNodeName();
			JSONMemento discoveries = serverTypes[i].getChild("discoveries");
			loadDiscovery(serverTypeId, discoveries);
			JSONMemento downloads = serverTypes[i].getChild("downloads");
			loadDownloads(serverTypeId, downloads);
		}
	}

	private void loadDiscovery(String serverTypeId, JSONMemento discoveries) {
		if( discoveries == null )
			return;
		JSONMemento[] discoveryArray = discoveries.getChildren();
		if( discoveryArray == null )
			return;
		
		ArrayList<ServerBeanType> collector = new ArrayList<>();
		for( int i = 0; i < discoveryArray.length; i++ ) {
			JSONMemento oneDiscovery = discoveryArray[i];
			String discoveryType = oneDiscovery.getString("discoveryType");
			String id = oneDiscovery.getString("id");
			String name = oneDiscovery.getString("name");
			String fileContainingName = oneDiscovery.getString("nameFile");
			String nameKey = oneDiscovery.getString("nameKey");
			String requiredNamePrefix = oneDiscovery.getString("nameRequiredPrefix");
			String fileContainingVersion = oneDiscovery.getString("versionFile");
			String versionKey = oneDiscovery.getString("versionKey");
			String requiredVersionPrefix = oneDiscovery.getString("versionRequiredPrefix");
			
			if( discoveryType == null ) {
				continue;
			}
			if( "manifest".equals(discoveryType)) {
				collector.add(new ExplodedManifestDiscovery(id, name, serverTypeId, 
						fileContainingName, nameKey, requiredNamePrefix, 
						fileContainingVersion, versionKey, requiredVersionPrefix));
			} else if( "jarManifest".equals(discoveryType)) {
				collector.add(new JarManifestDiscovery(id, name, serverTypeId, 
						fileContainingName, nameKey, requiredNamePrefix, 
						fileContainingVersion, versionKey, requiredVersionPrefix));
			} else if( "properties".equals(discoveryType)) {
				collector.add(new JarManifestDiscovery(id, name, serverTypeId, 
						fileContainingName, nameKey, requiredNamePrefix, 
						fileContainingVersion, versionKey, requiredVersionPrefix));
			}
		}
		
		if( !collector.isEmpty()) {
			ServerBeanType[] allTypes = collector.toArray(new ServerBeanType[collector.size()]);
			this.myDiscovery = new GenericServerBeanTypeProvider(allTypes);
		}
		
	}
	
	private void loadDownloads(String serverTypeId, JSONMemento downloads) {
		if( downloads == null )
			return;
		JSONMemento[] downloadArray = downloads.getChildren();
		if( downloadArray == null )
			return;
		
		String downloadProviderId = downloads.getString("downloadProviderId");
		ArrayList<DownloadRuntime> collector = new ArrayList<>();
		for( int i = 0; i < downloadArray.length; i++ ) {
			JSONMemento oneDownload = downloadArray[i];
			
			String id = oneDownload.getString("id");
			String name = oneDownload.getString("name");
			String fullVersion = oneDownload.getString("fullVersion");
			String downloadUrl = oneDownload.getString("downloadUrl");
			String licenseUrl = oneDownload.getString("licenseUrl");
			String installationMethod = oneDownload.getString("installationMethod");
			
			DownloadRuntime oneDLRT = new DownloadRuntime(id,  name,  fullVersion, downloadUrl);
			oneDLRT.setLicenseURL(licenseUrl);
			oneDLRT.setInstallationMethod(installationMethod);
			collector.add(oneDLRT);
		}
				
		if( !collector.isEmpty()) {
			DownloadRuntime[] dlrtArr = collector.toArray(new DownloadRuntime[collector.size()]);
			GenericDownloadRuntimesProvider dlrtProvider =new GenericDownloadRuntimesProvider(
					downloadProviderId, serverTypeId, dlrtArr);
			dlrtProvider.setModel(getRspModel());
			this.myDownloadRuntimeProvider = dlrtProvider;
		}
		
	}

	public void registerExtensions() {
		if( myDownloadRuntimeProvider != null )
			getRspModel().getDownloadRuntimeModel().addDownloadRuntimeProvider(myDownloadRuntimeProvider);
		if( myDiscovery != null )
			getRspModel().getServerBeanTypeManager().addTypeProvider(myDiscovery);
	}

	public void unregisterExtensions() {
		if( myDownloadRuntimeProvider != null )
			getRspModel().getDownloadRuntimeModel().removeDownloadRuntimeProvider(myDownloadRuntimeProvider);
		if( myDiscovery != null )
			getRspModel().getServerBeanTypeManager().removeTypeProvider(myDiscovery);
	}

	public IServerManagementModel getRspModel() {
		return rspModel;
	}
}
