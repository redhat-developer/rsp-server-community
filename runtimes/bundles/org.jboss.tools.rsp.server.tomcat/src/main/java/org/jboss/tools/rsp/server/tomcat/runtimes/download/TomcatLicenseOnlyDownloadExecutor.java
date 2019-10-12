package org.jboss.tools.rsp.server.tomcat.runtimes.download;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.rsp.api.dao.CreateServerResponse;
import org.jboss.tools.rsp.eclipse.core.runtime.IStatus;
import org.jboss.tools.rsp.foundation.core.tasks.TaskModel;
import org.jboss.tools.rsp.runtime.core.model.DownloadRuntime;
import org.jboss.tools.rsp.server.redhat.download.stacks.AbstractStacksDownloadRuntimesProvider;
import org.jboss.tools.rsp.server.spi.model.IServerManagementModel;
import org.jboss.tools.rsp.server.spi.runtimes.AbstractLicenseOnlyDownloadExecutor;
import org.jboss.tools.rsp.server.spi.util.StatusConverter;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.ITomcatServerAttributes;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.TomcatServerTypes;

public class TomcatLicenseOnlyDownloadExecutor extends AbstractLicenseOnlyDownloadExecutor {

	public TomcatLicenseOnlyDownloadExecutor(DownloadRuntime dlrt, IServerManagementModel model) {
		super(dlrt, model);
	}

	protected IStatus createServer(DownloadRuntime dlrt, String newHome, TaskModel tm) {
		// The wtp-runtime id is used in stacks.yaml, 
		String wtpRuntimeId = dlrt.getProperty(AbstractStacksDownloadRuntimesProvider.PROP_WTP_RUNTIME);
		
		// but rsp-server doesn't really have a server / runtime split. 
		// So now we need to get the rsp-server server type id
		String serverType = TomcatServerTypes.RUNTIME_TO_SERVER.get(wtpRuntimeId);
		
		// Now we have to somehow create this thing... ... ... 
		Set<String> serverIds = getServerModel().getServers().keySet();
		String suggestedId = new File(newHome).getName();
		String chosenId = getUniqueServerId(suggestedId, serverIds);
		
		Map<String,Object> attributes = new HashMap<>();
		attributes.put(ITomcatServerAttributes.SERVER_HOME, newHome);
		
		CreateServerResponse response = getServerModel().createServer(serverType, chosenId, attributes);
		return StatusConverter.convert(response.getStatus());
	}

}