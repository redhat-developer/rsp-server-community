package org.jboss.tools.rsp.server.tomcat.runtimes.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.eclipse.core.runtime.Status;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeRunner;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.tomcat.beans.impl.IServerConstants;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.ITomcatServerAttributes;

public class DownloadRuntimesProvider implements IDownloadRuntimesProvider {
	private static final String DLRT_ID_PREFIX = "tomcat-";
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
	public DownloadRuntime[] getDownloadableRuntimes(IProgressMonitor monitor) {
		// id, name, version, url
		DownloadRuntime tomcat9 = new DownloadRuntime("tomcat-9.0.27", "Tomcat 9.0.27", "9.0.27", "http://apache.osuosl.org/tomcat/tomcat-9/v9.0.27/bin/apache-tomcat-9.0.27.zip");
		return new DownloadRuntime[] {tomcat9};
	}

	@Override
	public IDownloadRuntimeRunner getDownloadRunner(DownloadRuntime dr) {
		if( dr != null && dr.getId().startsWith(DLRT_ID_PREFIX)) {
			return new AbstractLicenseOnlyDownloadExecutor(dr, model) {
				@Override
				protected IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm) {
					String serverTypeId = getServerType(dlrt);
					if( serverTypeId == null ) {
						return Status.CANCEL_STATUS;
					}
					// Now we have to somehow create this thing... ... ... 
					Set<String> serverIds = getServerModel().getServers().keySet();
					String suggestedId = new File(newHome).getName();
					String chosenId = getUniqueServerId(suggestedId, serverIds);
					
					Map<String,Object> attributes = new HashMap<>();
					attributes.put(ITomcatServerAttributes.SERVER_HOME, newHome);
					
					CreateServerResponse response = getServerModel().createServer(serverTypeId, chosenId, attributes);
					return StatusConverter.convert(response.getStatus());
				}
			};
		}
		return null;
	}
	
	private String getServerType(DownloadRuntime dlrt) {
		if( dlrt.getVersion().startsWith("9.")) {
			return IServerConstants.TOMCAT_90_SERVER_TYPE_ID;
		}
		return null;
	}

}
