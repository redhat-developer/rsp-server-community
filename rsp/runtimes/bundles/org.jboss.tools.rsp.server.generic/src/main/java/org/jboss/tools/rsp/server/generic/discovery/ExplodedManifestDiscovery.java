package org.jboss.tools.rsp.server.generic.discovery;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.tools.rsp.server.generic.discovery.internal.ManifestUtility;
import org.jboss.tools.rsp.server.generic.matchers.GlobScanner;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class ExplodedManifestDiscovery extends ServerBeanType {
	protected String nameFileString;
	protected String nameKey;
	protected String requiredNamePrefix;
	protected String versionFileString;
	protected String versionKey;
	protected String requiredVersionPrefix;
	protected String serverAdapterTypeId;
	protected boolean nameFileStringIsPattern;
	protected boolean versionFileStringIsPattern;

	/**
	 * 
	 * @param id An id for this discovery
	 * @param name A name for this discovery
	 * @param serverAdapterTypeId A server type id associated with this discovery
	 * @param fileContainingName Relative path to the jar which contains a manifest entry for the name
	 * @param nameKey The manifest key for the name
	 * @param requiredNamePrefix The required manifest name prefix
	 * @param fileContainingVersion Relative path to the jar which contains a manifest entry for the version
	 * @param versionKey The manifest key for the version
	 * @param requiredVersionPrefix The required manifest version prefix
	 */	
	public ExplodedManifestDiscovery( String id, String name, String serverAdapterTypeId, 
			String nameFileString, boolean nameFileStringIsPattern, 
			String nameKey, String requiredNamePrefix,
			String versionFileString, boolean versionFileStringIsPattern, 
			String versionKey, String requiredVersionPrefix) {
		super(id, name);
		this.serverAdapterTypeId = serverAdapterTypeId;
		this.nameFileString = nameFileString;
		this.nameFileStringIsPattern = nameFileStringIsPattern;
		this.nameKey = nameKey;
		this.requiredNamePrefix = requiredNamePrefix;
		this.versionFileStringIsPattern = versionFileStringIsPattern;
		this.versionFileString = versionFileString;
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
		if( versionFileStringIsPattern ) 
			return ManifestUtility.getPropertyFromManifestFile(new File(root, versionFileString), versionKey);
		
		List<String> includes = Arrays.asList(new String[]{versionFileString});
		GlobScanner gs = new GlobScanner(root,includes, Collections.EMPTY_LIST, true);
		List<String> results = gs.matches();
		if( results != null && results.size() > 0 ) {
			return ManifestUtility.getPropertyFromManifestFile(new File(root, results.get(0)), versionKey);
		}
		return null;
	}
	
	public String getFullName(File root) {
		if( nameFileStringIsPattern ) 
			return ManifestUtility.getPropertyFromManifestFile(new File(root, nameFileString), nameKey);

		List<String> includes = Arrays.asList(new String[]{nameFileString});
		GlobScanner gs = new GlobScanner(root,includes, Collections.EMPTY_LIST, true);
		List<String> results = gs.matches();
		if( results != null && results.size() > 0 ) {
			return ManifestUtility.getPropertyFromManifestFile(new File(root, results.get(0)), nameKey);
		}
		return null;
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
