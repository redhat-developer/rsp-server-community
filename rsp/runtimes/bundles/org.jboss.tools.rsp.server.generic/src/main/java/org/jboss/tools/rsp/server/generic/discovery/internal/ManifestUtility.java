/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.generic.discovery.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.tools.rsp.launching.utils.FileUtil;

public class ManifestUtility {

	protected ManifestUtility() {
		// prevent instantiation 
	}

	public static String getPropertyFromManifestFile(File manifestFile, String propertyName) {
		try {
			String contents = FileUtil.getContents(manifestFile);
			if( contents != null ) {
				Manifest mf = new Manifest(new ByteArrayInputStream(contents.getBytes()));
				Attributes a = mf.getMainAttributes();
				String val = a.getValue(propertyName);
				return val;
			}
		} catch(IOException ioe) {
			// 
		}
		return null;
	}

	public static String getPropertyFromPropertiesFile(File file, String propertyName) {
		try {
			return searchPropertiesInputStream(new FileInputStream(file), new String[] {propertyName});
		} catch(IOException ioe) {
			// 
		}
		return null;
	}


	public static String getManifestPropertiesFromZip(File systemJarFile, String[] manifestAttributes) {
		if (systemJarFile.isDirectory()) {
			return null;
		}

		if(systemJarFile.canRead()) {
			try(ZipFile jar = new ZipFile(systemJarFile)) {
				ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");//$NON-NLS-1$
				String ret = searchPropertiesInputStream(jar.getInputStream(manifest), manifestAttributes);
				if( ret != null )
					return ret;
			} catch (IOException e) {
				// It's already null, and would fall through to return null,
				// but hudson doesn't like empty catch blocks.
			}
		}
		return null;  
	}

	public static String getManifestPropertyFromZip(File toSearch, String versionKey) {
		return getManifestPropertiesFromZip(toSearch, new String[] {versionKey});
	}
	
	public static String searchPropertiesInputStream(InputStream stream, String[] keysToSearch) throws IOException {
		Properties props = new Properties();
		props.load(stream);
		String ret = null;
		for( int i = 0; i < keysToSearch.length; i++ ) {
			ret = props.getProperty(keysToSearch[i]); //$NON-NLS-1$
			if (ret != null && ret.trim().length() > 0) {
				return ret;
			}
			ret = (String)props.get(keysToSearch[i]);
			if (ret != null && ret.trim().length() > 0) {
				return ret;
			}
		}
		return null;
	}
}
