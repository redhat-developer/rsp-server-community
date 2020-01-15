package org.jboss.tools.rsp.server.generic.discovery;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.tools.rsp.server.generic.discovery.internal.ManifestUtility;
import org.jboss.tools.rsp.server.generic.matchers.GlobScanner;

public class JarManifestDiscovery extends ExplodedManifestDiscovery {

	public JarManifestDiscovery(String id, String name, String serverAdapterTypeId, String nameFileString,
			boolean nameFileStringIsPattern, String nameKey, String requiredNamePrefix, String versionFileString,
			boolean versionFileStringIsPattern, String versionKey, String requiredVersionPrefix) {
		super(id, name, serverAdapterTypeId, nameFileString, nameFileStringIsPattern, nameKey, requiredNamePrefix,
				versionFileString, versionFileStringIsPattern, versionKey, requiredVersionPrefix);
	}

	@Override
	public String getFullVersion(File root) {
		if( !versionFileStringIsPattern ) 
			return ManifestUtility.getManifestPropertyFromZip(new File(root, versionFileString), versionKey);
		
		List<String> includes = Arrays.asList(new String[]{versionFileString});
		GlobScanner gs = new GlobScanner(root,includes, Collections.EMPTY_LIST, true);
		List<String> results = gs.matches();
		if( results != null && results.size() > 0 ) {
			return ManifestUtility.getManifestPropertyFromZip(new File(root, results.get(0)), versionKey);
		}
		return null;
	}
	
	public String getFullName(File root) {
		if( !nameFileStringIsPattern ) 
			return ManifestUtility.getManifestPropertyFromZip(new File(root, nameFileString), nameKey);
		
		List<String> includes = Arrays.asList(new String[]{nameFileString});
		GlobScanner gs = new GlobScanner(root,includes, Collections.EMPTY_LIST, true);
		List<String> results = gs.matches();
		if( results != null && results.size() > 0 ) {
			return ManifestUtility.getManifestPropertyFromZip(new File(root, results.get(0)), nameKey);
		}
		return null;
	}


}
