package org.jboss.tools.rsp.server.felix.runtimes.download;

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
import org.jboss.tools.rsp.server.felix.servertype.impl.IServerConstants;
import org.jboss.tools.rsp.server.felix.servertype.impl.IFelixServerAttributes;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;

public class DownloadRuntimesProvider implements IDownloadRuntimesProvider {
	private static final String DLRT_ID_PREFIX = "felix-";
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
		DownloadRuntime felix60 = new DownloadRuntime("felix-6.0.3", "Apache Felix 6.0.3", "6.0.3", 
				"http://apache.mirrors.hoobly.com//felix/org.apache.felix.main.distribution-6.0.3.zip");
		return new DownloadRuntime[] {felix60};
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
					attributes.put(IFelixServerAttributes.SERVER_HOME, newHome);
					
					CreateServerResponse response = getServerModel().createServer(serverTypeId, chosenId, attributes);
					return StatusConverter.convert(response.getStatus());
				}
			};
		}
		return null;
	}
	
	private String getServerType(DownloadRuntime dlrt) {
		if( dlrt.getVersion().startsWith("6.0.")) {
			return IServerConstants.FELIX_6X_SERVER_TYPE_ID;
		}
		return null;
	}

}
