package org.jboss.tools.rsp.server.generic;

import java.util.ArrayList;

import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.server.generic.discovery.ExplodedManifestDiscovery;
import org.jboss.tools.rsp.server.generic.discovery.GenericServerBeanTypeProvider;
import org.jboss.tools.rsp.server.generic.discovery.JarManifestDiscovery;
import org.jboss.tools.rsp.server.generic.runtimes.download.GenericDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerType;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;

public class GenericServerTypeExtensionModel implements IServerBehaviorFromJSONProvider {

	private IServerBehaviorFromJSONProvider delegateProvider;
	private GenericServerBeanTypeProvider myDiscovery;
	private GenericDownloadRuntimesProvider myDownloadRuntimeProvider;
	private GenericServerType myServerType;
	private IServerManagementModel rspModel;

	public GenericServerTypeExtensionModel(
			IServerManagementModel rspModel,
			IServerBehaviorFromJSONProvider delegateProvider,
			JSONMemento serverType) {
		this.rspModel = rspModel;
		this.delegateProvider = delegateProvider;
		if( this.delegateProvider == null )
			this.delegateProvider = this;
		
		String serverTypeId = serverType.getNodeName();
		JSONMemento discoveries = serverType.getChild("discoveries");
		loadDiscovery(serverTypeId, discoveries);
		
		JSONMemento downloads = serverType.getChild("downloads");
		loadDownloads(serverTypeId, downloads);
		
		JSONMemento type = serverType.getChild("type");
		JSONMemento behavior = type.getChild("behavior");
		IServerBehaviorProvider delegateProviderFromJson = this.delegateProvider.loadBehaviorFromJSON(serverTypeId, behavior);
		loadServerType(serverTypeId, delegateProviderFromJson, type);
	}

	public IServerBehaviorProvider loadBehaviorFromJSON(String serverTypeId, JSONMemento behaviorMemento) {
		return new GenericServerBehaviorProvider(behaviorMemento);
	}
	

	private void loadServerType(String serverTypeId, 
			IServerBehaviorProvider delegateProvider, JSONMemento type) {
		String name = type.getString("name");
		String desc = type.getString("description");
		String launchModes = type.getString("launchModes");
		
		JSONMemento attributes = type.getChild("attributes");
		JSONMemento required = null;
		JSONMemento optional = null;
		if( attributes != null ) {
			required = attributes.getChild("required");
			optional = attributes.getChild("optional");
		}
		this.myServerType = new GenericServerType(serverTypeId, name, 
				desc, launchModes, required, optional, delegateProvider);
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
			String id = oneDiscovery.getNodeName();
			String discoveryType = oneDiscovery.getString("discoveryType");
			String name = oneDiscovery.getString("name");
			String fileContainingName = oneDiscovery.getString("nameFile");
			String nameKey = oneDiscovery.getString("nameKey");
			String requiredNamePrefix = oneDiscovery.getString("nameRequiredPrefix");
			String fileContainingVersion = oneDiscovery.getString("versionFile");
			String versionKey = oneDiscovery.getString("versionKey");
			String requiredVersionPrefix = oneDiscovery.getString("versionRequiredPrefix");
			
			String nameFilePattern = oneDiscovery.getString("nameFilePattern");
			String versionFilePattern = oneDiscovery.getString("versionFilePattern");
			boolean nameIsPattern = nameFilePattern != null;
			boolean versionIsPattern = versionFilePattern != null;
			
			String nameString = nameIsPattern ? nameFilePattern : fileContainingName;
			String versionString = versionIsPattern ? versionFilePattern : fileContainingVersion;
			
			if( discoveryType == null ) {
				continue;
			}
			if( "manifest".equals(discoveryType)) {
				collector.add(new ExplodedManifestDiscovery(id, name, serverTypeId, 
						nameString, nameIsPattern, nameKey, requiredNamePrefix, 
						versionString, versionIsPattern, versionKey, requiredVersionPrefix));
			} else if( "jarManifest".equals(discoveryType)) {
				collector.add(new JarManifestDiscovery(id, name, serverTypeId, 
						nameString, nameIsPattern, nameKey, requiredNamePrefix, 
						versionString, versionIsPattern, versionKey, requiredVersionPrefix));
			} else if( "properties".equals(discoveryType)) {
				collector.add(new JarManifestDiscovery(id, name, serverTypeId, 
						nameString, nameIsPattern, nameKey, requiredNamePrefix, 
						versionString, versionIsPattern, versionKey, requiredVersionPrefix));
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
			String id = oneDownload.getNodeName();
			String name = oneDownload.getString("name");
			String fullVersion = oneDownload.getString("fullVersion");
			String downloadUrl = oneDownload.getString("downloadUrl");
			String licenseUrl = oneDownload.getString("licenseUrl");
			String installationMethod = oneDownload.getString("installationMethod");
			String size = oneDownload.getString("size");
			
			DownloadRuntime oneDLRT = new DownloadRuntime(id,  name,  fullVersion, downloadUrl);
			oneDLRT.setLicenseURL(licenseUrl);
			oneDLRT.setInstallationMethod(installationMethod);
			oneDLRT.setSize(size);
			collector.add(oneDLRT);
		}

		if( !collector.isEmpty()) {
			DownloadRuntime[] dlrtArr = collector.toArray(new DownloadRuntime[collector.size()]);
			GenericDownloadRuntimesProvider dlrtProvider =new GenericDownloadRuntimesProvider(
					downloadProviderId, serverTypeId, dlrtArr);
			dlrtProvider.setModel(this.rspModel);
			this.myDownloadRuntimeProvider = dlrtProvider;
		}
	}

	public IServerBehaviorFromJSONProvider getDelegateProvider() {
		return delegateProvider;
	}

	public GenericServerBeanTypeProvider getMyDiscovery() {
		return myDiscovery;
	}

	public GenericDownloadRuntimesProvider getMyDownloadRuntimeProvider() {
		return myDownloadRuntimeProvider;
	}

	public GenericServerType getMyServerType() {
		return myServerType;
	}
}
