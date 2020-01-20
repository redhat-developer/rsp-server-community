package org.jboss.tools.rsp.server.generic.servertype.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.eclipse.core.runtime.Path;
import org.jboss.tools.rsp.eclipse.debug.core.ILaunch;
import org.jboss.tools.rsp.eclipse.jdt.launching.IVMInstall;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.variables.IDynamicVariable;
import org.jboss.tools.rsp.server.generic.servertype.variables.IStringVariableManager;
import org.jboss.tools.rsp.server.generic.servertype.variables.IValueVariable;
import org.jboss.tools.rsp.server.generic.servertype.variables.StringSubstitutionEngine;
import org.jboss.tools.rsp.server.spi.launchers.IServerShutdownLauncher;
import org.jboss.tools.rsp.server.spi.launchers.IServerStartLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;

public class GenericJavaLauncher extends AbstractGenericJavaLauncher
		implements IServerStartLauncher, IServerShutdownLauncher {

	private JSONMemento startupMemento;

	public GenericJavaLauncher(IServerDelegate serverDelegate, JSONMemento startupMemento) {
		super(serverDelegate);
		this.startupMemento = startupMemento;
	}

	/*
	 * Entry point for shutdown launcher
	 */
	@Override
	public ILaunch launch(boolean force) throws CoreException {
		IServerDelegate delegate = getDelegate();
		ILaunch launch = (ILaunch) delegate.getSharedData(GenericServerBehavior.START_LAUNCH_SHARED_DATA);
		if (force && terminateProcesses(launch)) {
			return null;
		}
		return launch("run");
	}

	private String getDefaultWorkingDirectory() {
		String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,(String) null);
		if( serverHome != null )
			return serverHome;
		
		String serverHomeFile = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_FILE,(String) null);
		if( serverHomeFile != null )
			return new File(serverHomeFile).getParent();

		return null;
	}
	
	@Override
	protected String getWorkingDirectory() {
		JSONMemento launchProperties = startupMemento.getChild("launchProperties");
		if (launchProperties != null) {
			String wd = launchProperties.getString("workingDirectory");
			if( wd == null ) {
				return getDefaultWorkingDirectory();
			}
			
			String postSub = null;
			try {
				postSub = applySubstitutions(wd);
			} catch(CoreException ce) {
				return getDefaultWorkingDirectory();
			}
			
			Path p = new Path(postSub);
			if( p.isAbsolute())
				return p.toOSString();
			
			String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,(String) null);
			if (serverHome != null && !serverHome.trim().isEmpty()) {
				return new Path(serverHome).append(p).toOSString();
			}
		}
		return null;
	}

	@Override
	protected String getMainTypeName() {
		JSONMemento launchProperties = startupMemento.getChild("launchProperties");
		if (launchProperties != null) {
			String main = launchProperties.getString("mainType");
			if( main != null ) {
				try {
					return applySubstitutions(main);
				} catch(CoreException ce) {
					return main;
				}
			}
		}
		return null;
	}

	@Override
	protected String getVMArguments() {
		JSONMemento launchProperties = startupMemento.getChild("launchProperties");
		if (launchProperties != null) {
			String vmArgs = launchProperties.getString("vmArgs");
			if( vmArgs != null ) {
				try {
					return applySubstitutions(vmArgs);
				} catch(CoreException ce) {
					return vmArgs;
				}
			}
		}
		return null;
	}

	@Override
	protected String getProgramArguments() {
		JSONMemento launchProperties = startupMemento.getChild("launchProperties");
		if (launchProperties != null) {
			String programArgs = launchProperties.getString("programArgs");
			if( programArgs != null ) {
				try {
					return applySubstitutions(programArgs);
				} catch(CoreException ce) {
					return programArgs;
				}
			}
		}
		return null;
	}

	@Override
	protected String[] getClasspath() {
		String serverHome = getDelegate().getServer().getAttribute(DefaultServerAttributes.SERVER_HOME_DIR,
				(String) null);
		JSONMemento launchProperties = startupMemento.getChild("launchProperties");
		if (launchProperties != null) {
			String cpFromJson = launchProperties.getString("classpath");
			if (cpFromJson != null && !cpFromJson.isEmpty()) {
				// First apply substitutions
				String postSub = cpFromJson;
				try {
					postSub = applySubstitutions(postSub);
				} catch(CoreException ce) {
				}
				return convertStringToClasspathEntries(serverHome, postSub);
			}
		}
		return null;
	}
	
	private String[] convertStringToClasspathEntries(String serverHome, String postSub) {
		String[] relatives = postSub.split(";");
		List<String> ret = new ArrayList<String>();
		File absolute = null;
		for (int i = 0; i < relatives.length; i++) {
			absolute = new File(serverHome, relatives[i]);
			if( absolute.isFile()) {
				ret.add(absolute.getAbsolutePath());
			} else if( absolute.isDirectory()) {
				File[] children = absolute.listFiles();
				for( int j = 0; j < children.length; j++ ) {
					if( children[j].getName().endsWith(".jar")) {
						ret.add(children[j].getAbsolutePath());
					}
				}
			}
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	private String applySubstitutions(String input) throws CoreException {
		return new StringSubstitutionEngine().performStringSubstitution(input, 
				true, true, new ServerStringVariableManager(getServer()));
	}

	private class ServerStringVariableManager implements IStringVariableManager {
		private IServer server;

		public ServerStringVariableManager(IServer server) {
			this.server = server;

		}

		@Override
		public IValueVariable getValueVariable(String name) {
			return new IValueVariable() {
				@Override
				public String getValue() {
					String nonServerVal = getNonServerKeyValues(name);
					if( nonServerVal != null ) {
						return nonServerVal;
					}
					return server.getAttribute(name, (String) null);
				}
			};
		}

		public String getNonServerKeyValues(String name) {
			if( "java.home".equals(name)) {
				IVMInstall vmi = getVMInstall(getDelegate());
				if( vmi != null ) {
					return vmi.getInstallLocation().getAbsolutePath();
				}
			}
			return null;
		}
		
		@Override
		public IDynamicVariable getDynamicVariable(String name) {
			// TODO Auto-generated method stub
			return null;
		}

	}
}