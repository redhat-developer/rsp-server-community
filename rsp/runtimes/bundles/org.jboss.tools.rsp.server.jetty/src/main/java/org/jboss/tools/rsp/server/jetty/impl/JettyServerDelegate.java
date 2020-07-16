/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.jetty.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.jboss.tools.rsp.api.DefaultServerAttributes;
import org.jboss.tools.rsp.api.dao.CommandLineDetails;
import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.eclipse.core.runtime.CoreException;
import org.jboss.tools.rsp.launching.memento.JSONMemento;
import org.jboss.tools.rsp.server.generic.servertype.DefaultExternalVariableResolver;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerBehavior;
import org.jboss.tools.rsp.server.generic.servertype.GenericServerType;
import org.jboss.tools.rsp.server.generic.servertype.variables.ServerStringVariableManager.IExternalVariableResolver;
import org.jboss.tools.rsp.server.jetty.servertype.impl.IJettyServerAttributes;
import org.jboss.tools.rsp.server.jetty.servertype.impl.JettyContextRootSupport;
import org.jboss.tools.rsp.server.spi.launchers.AbstractJavaLauncher;
import org.jboss.tools.rsp.server.spi.servertype.IServer;
import org.jboss.tools.rsp.server.spi.servertype.IServerDelegate;
import org.jboss.tools.rsp.server.spi.servertype.IServerWorkingCopy;

public class JettyServerDelegate extends GenericServerBehavior implements IServerDelegate {

	public JettyServerDelegate(IServer server, JSONMemento behaviorMemento) {
		super(server, behaviorMemento);
	}
	@Override
	public void setDependentDefaults(IServerWorkingCopy server) {
		// Do nothing
		try {
			CommandLineDetails det = getStartLauncher().getLaunchCommand("run");
			String progArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_PROGRAM_ARGS);
			String vmArgs = det.getProperties().get(AbstractJavaLauncher.PROPERTY_VM_ARGS);
			if(progArgs == null || progArgs.isEmpty()) {
				progArgs = "";
			}
			if(vmArgs == null || vmArgs.isEmpty()) {
				vmArgs = "";
			}
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_BOOLEAN, false);
			server.setAttribute(GenericServerType.LAUNCH_OVERRIDE_PROGRAM_ARGS, progArgs);
			server.setAttribute(GenericServerType.JAVA_LAUNCH_OVERRIDE_VM_ARGS, vmArgs);
		} catch(CoreException ce) {
			ce.printStackTrace();
		}
	}
	
	@Override
	protected IExternalVariableResolver getExternalVariableResolver() {
		return new JettyExternalVariableResolver(this);
	}
	
	protected class JettyExternalVariableResolver extends DefaultExternalVariableResolver {

		public JettyExternalVariableResolver(GenericServerBehavior genericServerBehavior) {
			super(genericServerBehavior);
		}
		@Override
		public String getNonServerKeyValue(String key) {
			String superRet = super.getNonServerKeyValue(key);
			if( superRet != null ) 
				return superRet;
			
			if( IJettyServerAttributes.JETTY_HOST.equals(key)) {
				return resolveJettyHost();
			}

			if( IJettyServerAttributes.JETTY_PORT.equals(key)) {
				return resolveJettyPort();
			}

			return null;
		}

		private String resolveJettyHost() {
			File basedir = getBaseDir();
			File startd = new File(basedir, "start.d");
			File http_ini = new File(startd, "http.ini");
			if( http_ini.exists()) {
				try (InputStream input = new FileInputStream(http_ini)) {
		            Properties prop = new Properties();
		            prop.load(input);
		            if( prop.getProperty("jetty.http.host") != null ) {
		            	return prop.getProperty("jetty.http.host");
		            }
				} catch( IOException ioe) {
					// TODO 
				}
			}
			return "localhost";
		}

		private String resolveJettyPort() {
			File basedir = getBaseDir();
			File startd = new File(basedir, "start.d");
			File http_ini = new File(startd, "http.ini");
			if( http_ini.exists()) {
				try (InputStream input = new FileInputStream(http_ini)) {
		            Properties prop = new Properties();
		            prop.load(input);
		            if( prop.getProperty("jetty.http.port") != null ) {
		            	return prop.getProperty("jetty.http.port");
		            }
				} catch( IOException ioe) {
					// TODO 
				}
			}
			return "8080";
		}
		protected File getBaseDir() {
			String homeDir = getGenericServerBehavior().getServer().getAttribute(
					DefaultServerAttributes.SERVER_HOME_DIR, (String)null);
			String basedirVal = getGenericServerBehavior().getServer().getAttribute(
					IJettyServerAttributes.JETTY_BASEDIR, (String)null);
			return new File(homeDir).toPath().resolve(basedirVal).toFile();
			
		}
	}
	
	public String[] getDeploymentUrls(String strat, String baseUrl, String deployableOutputName, DeployableState ds) {
		return new JettyContextRootSupport().getDeploymentUrls(strat, baseUrl, deployableOutputName, ds); 
	}

}
