/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.rsp.server.jetty.servertype.impl;

import org.jboss.tools.rsp.api.dao.DeployableState;
import org.jboss.tools.rsp.server.generic.jee.ContextRootSupport;

public class JettyContextRootSupport extends ContextRootSupport {

	@Override
	public String[] getDeploymentUrls(String strat, String baseUrl, 
			String deployableOutputName, DeployableState ds) {
		if( deployableOutputName.equalsIgnoreCase("root.war") || deployableOutputName.equalsIgnoreCase("root")) {
			return new String[] {baseUrl};
		}
		String noSuffix = removeWarSuffix(deployableOutputName);
		String ret = removeTrailingSlash(baseUrl) + ensureStartsWithSlash(noSuffix); 
		return new String[] { ret };
	}

	@Override
	protected String[] getCustomWebDescriptorsRelativePath() {
		return new String[] { };
	}

	@Override
	protected String findFromWebDescriptorString(String descriptorContents) {
		return null;
	}
	
}
