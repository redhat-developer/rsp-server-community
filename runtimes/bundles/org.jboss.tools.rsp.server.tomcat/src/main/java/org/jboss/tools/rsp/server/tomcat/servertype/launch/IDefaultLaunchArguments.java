package org.jboss.tools.rsp.server.tomcat.servertype.launch;

import java.util.HashMap;

import org.jboss.tools.rsp.eclipse.core.runtime.IPath;

/**
 * This interface is provisional api and may add or 
 * remove methods at any time. Be aware. 
 *
 */
public interface IDefaultLaunchArguments {
	/**
	 * Get a string representative of the default program arguments
	 * for the backing runtime or server
	 * 
	 * This method is likely to be removed once additional API
	 * is added elsewhere to calculate the local / rse 
	 * server home.
	 * 
	 * @param serverHome
	 * @return
	 */
	public String getStartDefaultProgramArgs(IPath serverHome);
	
	/**
	 * Get a string representative of the default VM arguments
	 * for the backing runtime or server
	 * 
	 * This method is likely to be removed once additional API
	 * is added elsewhere to calculate the local / rse 
	 * server home.
	 * 
	 * @param serverHome
	 * @return
	 */
	public String getStartDefaultVMArgs(IPath serverHome);
	
	/**
	 * Get a string representative of the default program arguments
	 * for the backing runtime or server
	 * 
	 * @return
	 */
	public String getStartDefaultProgramArgs();
	
	/**
	 * Get a string representative of the default VM arguments
	 * for the backing runtime or server
	 * 
	 * @return
	 */
	public String getStartDefaultVMArgs();


	
	/**
	 * Get the default environment variables for the backing server or runtime
	 * @return
	 */
	public HashMap<String, String> getDefaultRunEnvVars();
	
	/**
	 * Get the default shutdown args for this server
	 * @return
	 */
	public String getDefaultStopArgs();
	
	public String getDefaultStopVMArgs();
}
