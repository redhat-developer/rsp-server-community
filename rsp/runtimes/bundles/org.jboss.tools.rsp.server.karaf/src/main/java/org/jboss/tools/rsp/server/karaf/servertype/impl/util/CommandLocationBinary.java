/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.rsp.server.karaf.servertype.impl.util;

import java.util.HashMap;

/**
 * A class representing a binary available on multiple platforms.
 */
public class CommandLocationBinary {
	private HashMap<String, String> commandMap;
	private HashMap<String, String> defaultLocMap;
	private String defaultPlatform;
	private String commandName;
	private String foundLoc = null;
	private boolean searchFailed = false;

	public CommandLocationBinary(String commandName) {
		commandMap = new HashMap<>();
		defaultLocMap = new HashMap<>();
		this.commandName = commandName;
	}

	/**
	 * Add a default command location for a given platform. 
	 * 
	 * @param platform
	 * @param command
	 * @param loc
	 */
	public void addPlatformLocation(String platform, String loc) {
		defaultLocMap.put(platform, loc);
	}

	public void addPlatformCommandName(String platform, String command) {
		commandMap.put(platform, command);
	}

	/**
	 * Set which command / default location should be used in the event that 
	 * the user is on an unexpected platform such as OS_AIX, it can use the command name 
	 * and default location of a differing platform, such as OS_LINUX
	 * 
	 * @param platform
	 */
	public void setDefaultPlatform(String platform) {
		this.defaultPlatform = platform;
	}

	public String getCommand(String platform) {
		return commandMap.containsKey(platform) ? commandMap.get(platform) : commandName;
	}

	public String getDefaultLoc(String platform) {
		return defaultLocMap.containsKey(platform) ? defaultLocMap.get(platform) : defaultLocMap.get(defaultPlatform);
	}

	public String findLocation() {
		return findLocation(2000);
	}

	public String findLocation(int timeout) {
		if (foundLoc != null || searchFailed)
			return foundLoc;

		String searched = CommandLocationLookupStrategy.get().search(this, timeout);
		if (searched == null) {
			searchFailed = true;
		}
		foundLoc = searched;
		return searched;
	}

	public String[] getPossibleSuffixes() {
		return CommandLocationLookupStrategy.get().getSuffixes();
	}
}
