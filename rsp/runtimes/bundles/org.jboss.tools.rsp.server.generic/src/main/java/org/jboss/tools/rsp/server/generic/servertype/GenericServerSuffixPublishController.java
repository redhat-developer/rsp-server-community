package org.jboss.tools.rsp.server.generic.servertype;

import java.io.File;
import java.nio.file.Path;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.ServerManagementAPIConstants;
import org.jboss.tools.rsp.api.dao.Attributes;
import org.jboss.tools.rsp.api.dao.util.CreateServerAttributesUtility;
import org.jboss.tools.rsp.server.generic.IPublishControllerWithOptions;
import org.jboss.tools.rsp.server.spi.publishing.AbstractFilesystemPublishController;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericServerSuffixPublishController extends AbstractFilesystemPublishController
		implements IPublishControllerWithOptions {

	private String[] approvedSuffixes;
	private String deploymentPath;
	private boolean supportsExploded;

	public GenericServerSuffixPublishController(IServer server, IServerDelegate delegate,
			String[] approvedSuffixes, String deploymentPath, boolean supportsExploded) {
		super(server, delegate);
		this.approvedSuffixes = approvedSuffixes;
		this.deploymentPath = deploymentPath;
		this.supportsExploded = supportsExploded;
	}

	@Override
	public Attributes listDeploymentOptions() {
		CreateServerAttributesUtility util = new CreateServerAttributesUtility();
		util.addAttribute(ServerManagementAPIConstants.DEPLOYMENT_OPTION_OUTPUT_NAME, 
				ServerManagementAPIConstants.ATTR_TYPE_STRING,
				"Customize the output name including extension for this deployment. Example: sample.war (Leave blank for default)", null);
		return util.toPojo();
	}
	
	protected boolean supportsExplodedDeployment() {
		return supportsExploded;
	}
	
	@Override
	protected String[] getSupportedSuffixes() {
		return approvedSuffixes;
	}

	@Override
	protected Path getDeploymentFolder() {
		String home = getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR, (String)null);
		return home == null ? null : new File(home).toPath().resolve(deploymentPath);
	}

}
