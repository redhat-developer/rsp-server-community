package org.jboss.tools.rsp.server.generic.discovery;

import java.io.File;

import org.jboss.tools.rsp.server.generic.discovery.internal.ManifestUtility;

public class JarManifestDiscovery extends ExplodedManifestDiscovery {

	public JarManifestDiscovery(String id, String name, String serverAdapterTypeId, String fileContainingName,
			String nameKey, String requiredNamePrefix, String fileContainingVersion, String versionKey,
			String requiredVersionPrefix) {
		super(id, name, serverAdapterTypeId, fileContainingName, nameKey,
				requiredNamePrefix, fileContainingVersion, versionKey, requiredVersionPrefix);
	}

	@Override
	public String getFullVersion(File root) {
		return ManifestUtility.getManifestPropertyFromZip(new File(root, fileContainingVersion), versionKey);
	}
	
	public String getFullName(File root) {
		return ManifestUtility.getManifestPropertyFromZip(new File(root, fileContainingName), nameKey);
	}


}
