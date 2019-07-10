/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v20.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.example.rsp.server.wonka.bundle.beans.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;


public class WonkaBean extends ServerBeanType {
	public WonkaBean() {
		super("wonka.id", "Wonka Server");
	}
	@Override
	public String getServerAdapterTypeId(String version) {
		return "wonka.1.0";
	}
	
	private boolean isValidRoot(File f) {
		if( new File(f, "versions.txt").isFile()) {
			return true;
		}
		return false;
	}
	
	@Override
	public String getFullVersion(File root) {
		if( !isValidRoot(root))
			return null;
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(root));
			return p.getProperty("wonka.version");
		} catch(IOException ioe) {
		}
		return null;
	}
	@Override
	public String getUnderlyingTypeId(File root) {
		if( !isValidRoot(root)) 
			return null;
		if( new File(root, "product.properties").isFile()) {
			return "wonka.product";
		}
		return "OpenWonka";
	}
	@Override
	public boolean isServerRoot(File location) {
		return isValidRoot(location);
	}
}
