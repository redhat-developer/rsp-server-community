package org.jboss.tools.rsp.server.generic.discovery;

import java.io.File;

import org.jboss.tools.rsp.server.generic.discovery.internal.ManifestUtility;

public class PropertiesFileDiscovery extends ExplodedManifestDiscovery {

	public PropertiesFileDiscovery(String id, String name, String serverAdapterTypeId, String fileContainingName,
			String nameKey, String requiredNamePrefix, String fileContainingVersion, String versionKey,
			String requiredVersionPrefix) {
		super(id, name, serverAdapterTypeId, fileContainingName, nameKey,
				requiredNamePrefix, fileContainingVersion, versionKey, requiredVersionPrefix);
	}

	@Override
	public String getFullVersion(File root) {
		return ManifestUtility.getPropertyFromPropertiesFile(new File(root, fileContainingVersion), versionKey);
	}
	
	public String getFullName(File root) {
		return ManifestUtility.getPropertyFromPropertiesFile(new File(root, fileContainingName), nameKey);
	}


}
