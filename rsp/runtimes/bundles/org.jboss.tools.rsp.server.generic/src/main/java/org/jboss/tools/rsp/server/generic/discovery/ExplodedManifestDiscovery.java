package org.jboss.tools.rsp.server.generic.discovery;

import java.io.File;

import org.jboss.tools.rsp.server.generic.discovery.internal.ManifestUtility;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class ExplodedManifestDiscovery extends ServerBeanType {
	protected String fileContainingName;
	protected String nameKey;
	protected String requiredNamePrefix;
	protected String fileContainingVersion;
	protected String versionKey;
	protected String requiredVersionPrefix;
	protected String serverAdapterTypeId;

	/**
	 * 
	 * @param homeDir The root of the installation
	 * @param fileContainingName Relative path to the jar which contains a manifest entry for the name
	 * @param nameKey The manifest key for the name
	 * @param requiredNamePrefix The required manifest name prefix
	 * @param fileContainingVersion Relative path to the jar which contains a manifest entry for the version
	 * @param versionKey The manifest key for the version
	 * @param requiredVersionPrefix The required manifest version prefix
	 */
	public ExplodedManifestDiscovery( String id, String name, String serverAdapterTypeId, 
			String fileContainingName, String nameKey, String requiredNamePrefix,
			String fileContainingVersion, String versionKey, String requiredVersionPrefix) {
		super(id, name);
		this.serverAdapterTypeId = serverAdapterTypeId;
		this.fileContainingName = fileContainingName;
		this.nameKey = nameKey;
		this.requiredNamePrefix = requiredNamePrefix;
		this.fileContainingVersion = fileContainingVersion;
		this.versionKey = versionKey;
		this.requiredVersionPrefix = requiredVersionPrefix;		
	}

	@Override
	public boolean isServerRoot(File location) {
		if( nameKey != null ) {
			String name = getFullName(location);
			if( name == null || !name.startsWith(requiredNamePrefix)) {
				return false;
			}
		}
		String vers = getFullVersion(location);
		if( vers == null || !vers.startsWith(requiredVersionPrefix)) {
			return false;
		}
		return true;
	}

	@Override
	public String getFullVersion(File root) {
		return ManifestUtility.getPropertyFromManifestFile(new File(root, fileContainingVersion), versionKey);
	}
	
	public String getFullName(File root) {
		return ManifestUtility.getPropertyFromManifestFile(new File(root, fileContainingName), nameKey);
	}


	@Override
	public String getUnderlyingTypeId(File root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		return serverAdapterTypeId;
	}
	
	
}
