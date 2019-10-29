package org.jboss.tools.rsp.server.tomcat.runtimes.download;

import org.jboss.jdf.stacks.model.Stacks;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeRunner;
import org.jboss.tools.rsp.runtime.core.model.IRuntimeInstaller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.jboss.jdf.stacks.parser.Parser;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.server.redhat.download.stacks.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.TomcatServerTypes;
import org.jboss.tools.rsp.stacks.core.model.StacksManager;

public class DownloadRuntimesProvider extends AbstractStacksDownloadRuntimesProvider {
	
	private static final String TOMCAT_YAML_DEFAULT_URL = 
    		"https://raw.githubusercontent.com/jboss-developer/jboss-stacks/1.0.0.Final/minishift.yaml";
    private static final String URL_PROPERTY_TOMCAT_STACKS = "org.jboss.tools.stacks.tomcat.url";
    private static final String TOMCAT_YAML_URL = System.getProperty(URL_PROPERTY_TOMCAT_STACKS, TOMCAT_YAML_DEFAULT_URL);
	private IServerManagementModel model;

	public DownloadRuntimesProvider(IServerManagementModel model) {
		this.model = model;
	}
	
	@Override
	public String getId() {
		return "tomcat";
	}
	protected File getDataFolder() {
		return model.getDataStoreModel().getDataLocation();
	}

	@Override
	protected Stacks[] getStacks(IProgressMonitor monitor) {
		//Stacks ret = new StacksManager(getDataFolder()).getStacks(TOMCAT_YAML_URL, 
		//		"Loading Tomcat Downloadable Runtimes", monitor);
		///////for testing purpose
			Stacks ret = null;
			File f = new File("/home/luca/Public/github.com/redhat-developer/rsp-server/minishifttest.yaml");
			if (f != null && f.exists()) {
				try(FileInputStream fis = new FileInputStream(f)) {
					Parser p = new Parser();
					ret = p.parse(fis);
				}catch(Exception ex) {
					String s = "";
				}
			}
		return ret == null ? null : new Stacks[] {ret};
	}

	@Override
	protected String getLegacyId(String id) {
		return null;
	}

	@Override
	protected boolean requiresDisclaimer(String runtimeId) {
		return false;
	}

	@Override
	protected boolean runtimeTypeIsRegistered(String runtimeId) {
		return TomcatServerTypes.RUNTIME_TO_SERVER.get(runtimeId) != null;
	}

	@Override
	protected void traverseStacks(Stacks stacks, List<DownloadRuntime> list, IProgressMonitor monitor) {
		traverseStacks(stacks, list, "TOMCAT", monitor);
	}

	@Override
	public IDownloadRuntimeRunner getDownloadRunner(DownloadRuntime dr) {
		DownloadRuntime dlrt = findDownloadRuntime(dr.getId());
		if( dlrt == null || !dlrt.equals(dr))
			return null;
		
		String installer = (dr.getInstallationMethod() == null ? 
				IRuntimeInstaller.EXTRACT_INSTALLER : dr.getInstallationMethod());
		// TODO verify installer exists? 

		return new TomcatLicenseOnlyDownloadExecutor(dr, model);
	}

}
