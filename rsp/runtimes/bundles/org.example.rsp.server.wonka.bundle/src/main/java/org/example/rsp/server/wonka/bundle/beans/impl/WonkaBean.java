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

import org.example.rsp.server.wonka.bundle.servertype.impl.WonkaServerTypes;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;


public class WonkaBean extends ServerBeanType {
	public WonkaBean() {
		super("wonka.id", "Wonka Server");
	}
	@Override
	public String getServerAdapterTypeId(String version) {
		return WonkaServerTypes.WONKA_SERVER_1_0_ID;
	}
	
	private boolean isValidRoot(File f) {
		// Very simple pattern matching, but you can do more
		File wonka = findWonkaFile(f);
		if( wonka == null )
			return false;
		return true;
	}
	
	private File findWonkaFile(File root) {
		File[] children = root.listFiles();
		for( int i = 0; i < children.length; i++ ) {
			if( children[i].getName().startsWith("wonka-runtime-")) {
				return children[i];
			}
		}
		return null;
	}
	
	@Override
	public String getFullVersion(File root) {
		if( !isValidRoot(root))
			return null;
		File wonka = findWonkaFile(root);
		String fName = wonka.getName();
		fName = fName.substring("wonka-runtime-".length());
		fName = fName.replace(".jar", "");
		return fName;
	}
	@Override
	public String getUnderlyingTypeId(File root) {
		// Check manifests, file tree, etc, whatever
		// to distinguish between a project and product, 
		// or other similar runtimes
		return "OpenWonka";
	}
	@Override
	public boolean isServerRoot(File location) {
		return isValidRoot(location);
	}
}
