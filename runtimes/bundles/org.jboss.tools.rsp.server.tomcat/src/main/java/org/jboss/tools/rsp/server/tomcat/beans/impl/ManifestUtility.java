/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.tomcat.beans.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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

	/**
	 * Scans the jars in the folder until a jar with a 
	 * manifest and a matching property key is found.  
	 * 
	 * If the given prefix is a prefix of the property value, 
	 * there is a match, and a 'true' is returned. 
	 * 
	 * Search 
	 * @param location  a root folder
	 * @param mainFolder a path leading to a subfolder of the location
	 * @param property a property to search for in manifest.mf
	 * @param propPrefix a prefix to check against for a match. 
	 * @return true if there is a match, false otherwise. 
	 */
	public static boolean scanFolderJarsForManifestProp(File location, String mainFolder, String property, String propPrefix) {
		String value = getManifestPropFromFolderJars(location, mainFolder, property);
		return value != null && value.trim().startsWith(propPrefix);
	}

	public static String getManifestPropFromFolderJars(File location, String mainFolder, String property) {
		File f = new File(location, mainFolder);
		if( f.exists() ) {
			File[] children = f.listFiles();
			for( int i = 0; i < children.length; i++ ) {
				if( children[i].getName().endsWith(".jar")) {
					return getJarProperty(children[i], property);
				}
			}
		}
		return null;
	}

	
	public static FileFilter jarFilter() {
		return (File pathname) -> 
				pathname.isFile() && pathname.getName().endsWith(".jar");
	}

	public static FileFilter manifestFilter() {
		return (File pathname) -> 
				pathname.isFile() && pathname.getName().equalsIgnoreCase("manifest.mf");
	}
	
	/**
	 * This method will check a jar file for a manifest, and, if it has it, 
	 * find the value for the given property. 
	 * 
	 * If either the jar, manifest or the property are not found, 
	 * return null.
	 * 
	 * @param systemJarFile
	 * @param propertyName
	 * @return
	 */
	public static String getJarProperty(File systemJarFile, String propertyName) {
		if (systemJarFile.canRead()) {
			try(ZipFile jar = new ZipFile(systemJarFile)) {
				ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");//$NON-NLS-1$
				Properties props = new Properties();
				props.load(jar.getInputStream(manifest));
				String value = (String) props.get(propertyName);
				return value;
			} catch (IOException e) {
				// Intentionally empty
				return null; 
			}
		} 
		return null;
	}
	

	public static String getManifestProperty(File manifestFile, String propertyName) {
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
	

	public static String getFullServerVersionFromZipLegacy(File systemJarFile, String[] manifestAttributes) {

		if (systemJarFile.isDirectory()) {
			File[] files = systemJarFile.listFiles((File dir, String name) -> name.endsWith(".jar"));
			if (files != null && files.length == 1) {
				systemJarFile = files[0];
			}
		}

		String version = null;
		if(systemJarFile.canRead()) {
			try(ZipFile jar = new ZipFile(systemJarFile)) {
				ZipEntry manifest = jar.getEntry("META-INF/MANIFEST.MF");//$NON-NLS-1$
				Properties props = new Properties();
				props.load(jar.getInputStream(manifest));
				
				for( int i = 0; i < manifestAttributes.length; i++ ) {
					version = props.getProperty(manifestAttributes[i]); //$NON-NLS-1$
					if (version != null && version.trim().length() > 0) {
						return version;
					}
					version = (String)props.get(manifestAttributes[i]);
					if (version != null && version.trim().length() > 0) {
						return version;
					}
				}
			} catch (IOException e) {
				// It's already null, and would fall through to return null,
				// but hudson doesn't like empty catch blocks.
				return null;  
			}
		}
		return version;
	}
}
