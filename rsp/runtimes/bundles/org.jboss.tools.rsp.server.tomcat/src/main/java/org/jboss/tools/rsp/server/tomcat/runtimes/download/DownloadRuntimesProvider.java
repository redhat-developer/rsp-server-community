package org.jboss.tools.rsp.server.tomcat.runtimes.download;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimeRunner;
import org.jboss.tools.rsp.runtime.core.model.IDownloadRuntimesProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.eclipse.core.runtime.IProgressMonitor;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;

public class DownloadRuntimesProvider implements IDownloadRuntimesProvider {
	private IServerManagementModel model;
	
	public DownloadRuntimesProvider(IServerManagementModel model) {
		// TODO Auto-generated constructor stub
		this.model = model;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "tomcat";
	}

	@Override
	public DownloadRuntime[] getDownloadableRuntimes(IProgressMonitor monitor) {
		//to be modified
		DownloadRuntime dlrt = new DownloadRuntime("tomcat.1.0", "Tomcat Server", 
				"1.0.0.Final", "https://github.com/robstryker/extend-rsp-example/raw/master/wonka-runtime/releases/wonka-runtime-1.0-SNAPSHOT.jar");
		//dlrt.setInstallationMethod(IRuntimeInstaller.BINARY_INSTALLER);
		//dlrt.setLicenseURL("https://www.gnu.org/licenses/lgpl-3.0.txt");
		return new DownloadRuntime[] { dlrt };
	}

	@Override
	public IDownloadRuntimeRunner getDownloadRunner(DownloadRuntime dr) {
		return new AbstractLicenseOnlyDownloadExecutor(dr, model) {

			@Override
			protected IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm) {
				Map<String, Object> attributes = new HashMap<>();
				attributes.put(DefaultServerAttributes.SERVER_HOME_DIR, newHome);
				
				Set<String> serverIds = getServerModel().getServers().keySet();
				String suggestedId = new File(newHome).getName();
				String chosenId = getUniqueServerId(suggestedId, serverIds);
				
				CreateServerResponse response = getServerModel().createServer(serverType, id, attributes);
				return StatusConverter.convert(response.getStatus());
			}
			
		};
	}

}
