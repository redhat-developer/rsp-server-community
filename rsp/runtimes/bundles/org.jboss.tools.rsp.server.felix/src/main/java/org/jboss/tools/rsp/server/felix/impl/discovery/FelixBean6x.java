package org.jboss.tools.rsp.server.felix.impl.discovery;

import java.io.File;

import org.jboss.tools.rsp.server.felix.impl.util.ManifestUtility;
import org.jboss.tools.rsp.server.felix.servertype.impl.IServerConstants;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class FelixBean6x extends ServerBeanType implements IServerConstants {
	protected String systemJarPath;
	public FelixBean6x() {
		super(ID_FELIX, NAME_FELIX);
		this.systemJarPath = "bin/felix.jar";
	}

	@Override
	public boolean isServerRoot(File location) {
		File felixJar = new File(location, systemJarPath);
		if (felixJar.exists() && felixJar.isFile()) {
			String title = ManifestUtility.getJarProperty(felixJar, BUNDLE_NAME);
			boolean isFelix = title != null && title.trim().equals(NAME_FELIX);
			return isFelix;
		}
		return false;
	}

	@Override
	public String getFullVersion(File location) {
		return ManifestUtility.getFullServerVersionFromZipLegacy(new File(location, systemJarPath), new String[]{
				"Bundle-Version", "Specification-Version", "Implementation-Version"});
	}

	@Override
	public String getUnderlyingTypeId(File root) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerAdapterTypeId(String version) {
		return IServerConstants.FELIX_6X_SERVER_TYPE_ID;
	}

}
