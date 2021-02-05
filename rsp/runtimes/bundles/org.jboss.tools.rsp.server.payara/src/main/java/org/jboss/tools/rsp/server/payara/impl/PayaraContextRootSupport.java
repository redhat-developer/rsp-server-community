/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.payara.impl;

import java.io.ByteArrayInputStream;

import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.launching.memento.IMemento;
import org.jboss.tools.rsp.launching.memento.XMLMemento;
import org.jboss.tools.rsp.server.generic.jee.ContextRootSupport;

public class PayaraContextRootSupport extends ContextRootSupport {
	
	public String[] getDeploymentUrls(String strat, String baseUrl, 
			String deployableOutputName, DeployableState ds) {
		String noSuffix = removeWarSuffix(deployableOutputName);
		String[] fromDescriptor = findFromDescriptor(ds);
		// Default case, nothing in descriptor, use app name
		if( fromDescriptor == null || fromDescriptor.length == 0) 
			return new String[] {append(noSuffix, baseUrl)};
		
		// Found something in descriptor. 
		return append(fromDescriptor, baseUrl);
	}
	
	@Override
	protected String findFromWebDescriptorString(String descriptorContents) {
		XMLMemento mem = XMLMemento.createReadRoot(new ByteArrayInputStream(descriptorContents.getBytes()));
		IMemento[] children = mem.getChildren("context-root");
		if( children != null && children.length == 1 ) {
			return ((XMLMemento)children[0]).getTextData();
		}
		return null;
	}
	
	@Override
	protected String[] getCustomWebDescriptorsRelativePath() {
		return new String[] { 
                    "payara-web.xml", "glassfish-web.xml", "sun-web.xml",
                    "WEB-INF/payara-web.xml", "WEB-INF/glassfish-web.xml", "WEB-INF/sun-web.xml"
                };
	}
}
