/*******************************************************************************
 * Copyright (c) 2018 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.example.rsp.server.wonka.bundle.impl;

import org.example.rsp.server.wonka.bundle.beans.impl.WonkaBean;
import org.jboss.tools.rsp.server.spi.discovery.IServerBeanTypeProvider;
import org.jboss.tools.rsp.server.spi.discovery.ServerBeanType;

public class WonkaServerBeanTypeProvider implements IServerBeanTypeProvider {


	public static final ServerBeanType WONKA = new WonkaBean();
	public static final ServerBeanType[] KNOWN_TYPES = {WONKA};
	
	@Override
	public ServerBeanType[] getServerBeanTypes() {
		return KNOWN_TYPES;
	}

}
