package org.jboss.tools.rsp.server.tomcat.beans.impl;

import java.io.File;

import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class TomcatBean9 extends ServerBeanType implements IServerConstants {
	public TomcatBean9() {
		super(ID_TOMCAT, NAME_TOMCAT);
	}

	@Override
	public boolean isServerRoot(File location) {
		return getFullVersion(location) != null;
	}

	@Override
	public String getFullVersion(File location) {
		String vers = JBossManifestUtility.getManifestPropFromJBossModulesFolder(
				new File[]{new File(location, MODULES)}, 
				"org.jboss.as.product", "wildfly-full/dir/META-INF", 
				MANIFEST_PROD_RELEASE_VERS);
		if( vers != null && vers.startsWith("9.")) {
			return vers;
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
		return IServerConstants.RUNTIME_TOMCAT_90;
	}

}
