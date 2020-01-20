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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jboss.tools.rsp.launching.utils.OSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLocationLookupStrategy {
	private static final Logger LOG = LoggerFactory.getLogger(CommandLocationLookupStrategy.class);
	
	
	private static final String LINUX_WHICH = "which";
	private static final String WINDOWS_WHERE = "where";
	private static final String LINUX_PATHVAR = "PATH";
	private static final String WINDOWS_PATHVAR = "Path";

	private static final String LINUX_SEPARATOR = ":";
	private static final String WIN_SEPARATOR = ";";

	public static final CommandLocationLookupStrategy WINDOWS_STRATEGY = new CommandLocationLookupStrategy(
			WINDOWS_WHERE, WIN_SEPARATOR,
			// Windows can use either separator or path variable
			// based on whether eclipse was launched from cygwin or not
			System.getenv().get(WINDOWS_PATHVAR) == null ? LINUX_PATHVAR : WINDOWS_PATHVAR,
			new String[] { ".exe", ".com" }, null);
	public static final CommandLocationLookupStrategy LINUX_STRATEGY = new CommandLocationLookupStrategy(LINUX_WHICH,
			LINUX_SEPARATOR, LINUX_PATHVAR, new String[] {}, null);
	public static final CommandLocationLookupStrategy MAC_STRATEGY = new CommandLocationLookupStrategy(LINUX_WHICH,
			LINUX_SEPARATOR, LINUX_PATHVAR, new String[] {}, new String[] { "bash", "-l", "-c", "echo $PATH" }, true);

	public static CommandLocationLookupStrategy get() {
		if (OSUtils.isWindows()) {
			return WINDOWS_STRATEGY;
		}
		if (OSUtils.isMac()) {
			return MAC_STRATEGY;
		}
		return LINUX_STRATEGY;
	}

	private String which, delim, pathvar;
	private String[] pathCommand;
	private String[] suffixes;
	private boolean preferSystemPath;

	public CommandLocationLookupStrategy(String which, String delim, String pathvar, String[] suffixes,
			String[] pathCommand) {
		this(which, delim, pathvar, suffixes, pathCommand, false);
	}

	public CommandLocationLookupStrategy(String which, String delim, String pathvar, String[] suffixes,
			String[] pathCommand, boolean preferSystemPath) {
		this.which = which;
		this.delim = delim;
		this.pathvar = pathvar;
		this.suffixes = suffixes;
		this.pathCommand = pathCommand;
		this.preferSystemPath = preferSystemPath;
	}

	public String search(CommandLocationBinary binary) {
		return search(binary, 2000);
	}

	public String search(CommandLocationBinary binary, int timeout) {
		String cmd = binary.getCommand(OSUtilWrapper.getOs());
		String defaultLoc = binary.getDefaultLoc(OSUtilWrapper.getOs());
		return findLocation(defaultLoc, cmd, which, delim, pathvar, timeout);
	}

	/**
	 * This method will try to find the given command. 
	 * 
	 * If the default location exists, it will use that. 
	 * 
	 * It will then attempt to search for the command name (with all possible suffixes)
	 * somewhere in the system path. 
	 * 
	 * If that still fails, it will run one where / which command to locate the command. 
	 * This will be called without the suffix. 
	 * 
	 * @param defaultLoc
	 * @param cmd
	 * @param which
	 * @param delim
	 * @param pathvar
	 * @param timeout
	 * @return
	 */
	private String findLocation(String defaultLoc, String cmd, String which, String delim, String pathvar,
			int timeout) {
		if (defaultLoc != null && new File(defaultLoc).exists()) {
			return defaultLoc;
		}
		String ret = searchPath(System.getenv(pathvar), delim, cmd);
		if (ret == null) {
			// run which / where
			ret = runCommandAndVerify(new String[] { which, cmd }, timeout);
		}
		return ret;
	}

	/**
	 * Ensure the given folder is on the path in the provided environment map, 
	 * or append to existing path in provided environment.
	 *   
	 * If the provided environment has no path variable, fetch an environment from
	 * either the currently running environment (if preferSystemPath is false) or 
	 * from the system via pathCommand member variable) if preferSystemPath is true. 
	 * 
	 * @param env
	 * @param folder
	 */
	public void ensureOnPath(Map<String, String> env, String folder) {
		HashMap<String, String> processEnv = new HashMap<>(System.getenv());
		if (env.get(pathvar) == null) {
			if (preferSystemPath) {
				String pathresult = runCommand(pathCommand);
				processEnv.put(pathvar, pathresult);
			}
			String newPath = ensureFolderOnPath(processEnv.get(pathvar), folder);
			env.put(pathvar, newPath);
		} else {
			String newPath = ensureFolderOnPath(env.get(pathvar), folder);
			env.put(pathvar, newPath);
		}
	}

	/**
	 * Append the given folder to the existing path if not already present
	 * 
	 * @param existingPath
	 * @param folder
	 * @return
	 */
	private String ensureFolderOnPath(String existingPath, String folder) {
		existingPath = (existingPath == null ? "" : existingPath);
		String[] roots = existingPath.split(delim);
		ArrayList<String> list = new ArrayList<>(Arrays.asList(roots));
		if (!list.contains(folder)) {
			list.add(folder);
		}
		return String.join(delim, list);
	}

	/**
	 * Get all possible command names by appending the various suffixes to the command name
	 * @param commandName
	 * @return
	 */
	private String[] getPossibleCommandNames(String commandName) {
		ArrayList<String> ret = new ArrayList<>(5);
		ret.add(commandName);
		for (int i = 0; i < suffixes.length; i++) {
			ret.add(commandName + suffixes[i]);
		}
		return (String[]) ret.toArray(new String[ret.size()]);
	}

	public String[] getSuffixes() {
		return suffixes == null ? new String[0] : suffixes;
	}

	public String[] getPossibleCommandNames(CommandLocationBinary binary) {
		return getPossibleCommandNames(binary.getCommand(OSUtilWrapper.getOs()));
	}

	private String searchPath(String path, String delim, String commandName) {
		String[] roots = path.split(delim);
		String[] withSuffixes = getPossibleCommandNames(commandName);
		for (int i = 0; i < roots.length; i++) {
			for (int j = 0; j < withSuffixes.length; j++) {
				File test = new File(roots[i], withSuffixes[j]);
				if (test.exists()) {
					return test.getAbsolutePath();
				}
			}
		}
		return null;
	}

	private String runCommandAndVerify(final String[] cmd, int timeout) {
		if (timeout == -1) {
			return runCommandAndVerify(cmd);
		} else {
			String path = ThreadUtils.runWithTimeout(timeout, new Callable<String>() {
				@Override
				public String call() throws Exception {
					return runCommandAndVerify(cmd);
				}
			});
			return path;
		}
	}

	private String runCommandAndVerify(String[] cmd) {
		Process p = createProcess(cmd, preferSystemPath);
		if (p != null) {
			String result = readProcess(p);
			// verify the output is a file path that exists
			if (result != null && !result.isEmpty() && new File(result).exists())
				return result;
		}
		return null;
	}

	private String runCommand(String cmd[]) {
		Process p = createProcess(cmd);
		if (p != null) {
			String result = readProcess(p);
			return result;
		}
		return null;
	}

	/**
	 * Use runtime.exec(String[]) to run a command.
	 * This method *does not* respect the preferSystemPath  member variable
	 * and will run with whatever the default environment is. 
	 * 
	 * @param cmd
	 * @return
	 */
	private Process createProcess(String cmd[]) {
		try {
			return Runtime.getRuntime().exec(cmd);
		} catch (IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
			return null;
		}
	}

	/**
	 * Use runtime.exec(String) 
	 * @param cmd
	 * @return
	 */
	private Process createProcess(String[] cmd, boolean useSystemPath) {
		try {
			if (useSystemPath) {
				String pathresult = runCommand(pathCommand);
				ProcessBuilder pb = new ProcessBuilder(cmd);
				Map<String, String> env = pb.environment();
				env.put(pathvar, pathresult);
				return pb.start();
			} else {
				return Runtime.getRuntime().exec(cmd);
			}
		} catch (IOException ioe) {
			LOG.error(ioe.getMessage(), ioe);
			return null;
		}
	}

	private String readProcess(Process p) {
		try {
			p.waitFor();
		} catch (InterruptedException ie) {
			// Ignore, expected
		}
		InputStream is = null;
		if (p.exitValue() == 0) {
			is = p.getInputStream();
		} else {
			// For debugging only
			//is = p.getErrorStream();
		}
		if (is != null) {
			try {
				java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
				String cmdOutput = s.hasNext() ? s.next() : "";
				if (!cmdOutput.isEmpty()) {
					cmdOutput = trim(cmdOutput);
					return cmdOutput;
				}
			} finally {
				try {
					if (p != null) {
						p.destroy();
					}
					is.close();
				} catch (IOException ioe) {
					// ignore
				}
			}
		}
		return null;
	}
    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }
    
    public static class OSUtilWrapper {
    	public static final String MAC="MAC";
    	public static final String WINDOWS="WINDOWS";
    	public static final String UNIX="UNIX";
    	public static final String SOLARIS="SOLARIS";
    	public static final String UNKNOWN="UNKNOWN";
    	
    	public static String getOs() {
    		if( OSUtils.isMac())
    			return MAC;
    		if( OSUtils.isWindows())
    			return WINDOWS;
    		if( OSUtils.isUnix())
    			return UNIX;
    		if( OSUtils.isSolaris())
    			return SOLARIS;
    		return UNKNOWN;
    	}
    }
}
