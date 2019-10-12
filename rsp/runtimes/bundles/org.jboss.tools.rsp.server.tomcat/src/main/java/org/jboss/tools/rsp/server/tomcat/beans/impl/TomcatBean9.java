package org.jboss.tools.rsp.server.tomcat.beans.impl;

import java.io.File;

import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;
import org.jboss.tools.rsp.server.tomcat.beans.impl.ManifestUtility;

public class TomcatBean9 extends ServerBeanType implements IServerConstants {
	protected String systemJarPath;
	public TomcatBean9() {
		super(ID_TOMCAT, NAME_TOMCAT);
		this.systemJarPath = BIN_TWIDDLE_PATH;
	}

	@Override
	public boolean isServerRoot(File location) {
		File tomcatJar = new File(location, systemJarPath);
		if (tomcatJar.exists() && tomcatJar.isFile()) {
			String title = ManifestUtility.getJarProperty(tomcatJar, IMPLEMENTATION_TITLE);
			boolean isTomcat = title != null && title.contains(ID_TOMCAT); //$NON-NLS-1$
			return !isTomcat;
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
		return IServerConstants.RUNTIME_TOMCAT_90;
	}

}
