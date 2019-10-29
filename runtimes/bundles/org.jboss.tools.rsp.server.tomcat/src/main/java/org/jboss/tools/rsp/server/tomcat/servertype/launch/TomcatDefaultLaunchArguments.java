package org.jboss.tools.rsp.server.tomcat.servertype.launch;

import java.util.HashMap;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.tomcat.impl.util.ITomcatRuntimeConstants;
import org.jboss.tools.rsp.server.tomcat.impl.util.ITomcatRuntimeResourceConstants;
import org.jboss.tools.rsp.server.tomcat.servertype.impl.ITomcatServerAttributes;

public class TomcatDefaultLaunchArguments implements IDefaultLaunchArguments, ITomcatRuntimeConstants, ITomcatRuntimeResourceConstants {
	
	protected IServer server;
	private IPath serverHome;
	
	public TomcatDefaultLaunchArguments(IServer server) {
		this.server = server;
	}
	
	private void setServerHome(IPath path) {
		this.serverHome = path;
	}
	
	protected IPath getServerHome() {
		// If we have a set server home, use it
		if( serverHome != null)
			return serverHome;
		// Get from server-mode data (local/rse)
		String serverHome = server.getAttribute(ITomcatServerAttributes.SERVER_HOME, (String) null);
		return new Path(serverHome);
	}

	@Override
	public String getStartDefaultProgramArgs(IPath serverHome) {
		setServerHome(serverHome);
		return getStartDefaultProgramArgs();  
	}
	
	protected String getJavaTmpDir() {
		String ret = SYSPROP + JAVA_IO_TMPDIR + EQ +
				 getServerHome().toOSString() +
				 Path.SEPARATOR + TEMP;
		return ret;
	}
	
	protected String getCatalinaArgs() {
		IPath serverHome = getServerHome();
		String ret = SYSPROP + CATALINA_BASE + EQ + serverHome.toOSString() + SPACE;
		ret += SYSPROP + CATALINA_HOME + EQ + serverHome.toOSString() + SPACE;
		return ret;
	}

	@Override
	public String getStartDefaultVMArgs(IPath serverHome) {
		setServerHome(serverHome);
		return getStartDefaultVMArgs();
	}
	
	protected String getLoggingArgs() {
		String ret = SYSPROP + JAVA_LOGGING_CONFIG_FILE + EQ +  
				getServerHome().toOSString() +
				Path.SEPARATOR + CONF + Path.SEPARATOR + TM_LOGGING_PROPS + SPACE; //$NON-NLS-1$
		ret += SYSPROP + JAVA_LOGGING_MANAGER + EQ + APACHE_JULI_CLASSLOADER_MANAGER + SPACE; //$NON-NLS-1$
		return ret;
	}
	
	protected String getSecurityArgs() {
		String ret = SYSPROP + JDK_TLS_KEYSIZE + EQ + "2048" + SPACE;
		ret += SYSPROP + JAVA_PROTOCOL_HANDLER_PKGS + EQ + APACHE_CATALINA_WEBRESOURCES + SPACE;				
		ret += SYSPROP + APACHE_CATALINA_SECURITY_LISTENER_UMASK + EQ + "0027" + SPACE; //$NON-NLS-1$
		return ret;
	}
	
	protected String getIgnoreEndorsedDirs() {
		String ret = SYSPROP + IGNORE_ENDORSED_DIRS + EQ;
		return ret;
	}
	

	@Override
	public String getStartDefaultProgramArgs() {
		return getCatalinaArgs() + getJavaTmpDir() +  
				SPACE + CATALINA_STARTUP_BOOTSTRAP + SPACE + "start"; 
	}

	@Override
	public String getStartDefaultVMArgs() {
		return getLoggingArgs() + getSecurityArgs() + 
				getIgnoreEndorsedDirs();
	}

	@Override
	public HashMap<String, String> getDefaultRunEnvVars() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultStopArgs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDefaultStopVMArgs() {
		// TODO Auto-generated method stub
		return null;
	}

}
