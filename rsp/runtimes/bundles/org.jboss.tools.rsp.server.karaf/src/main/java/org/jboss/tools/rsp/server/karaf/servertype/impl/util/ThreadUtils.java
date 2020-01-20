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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Andre Dietisheim
 */
public class ThreadUtils {

	/**
	 * Runs and blocking waits for the given callable to finish for the given
	 * time. Returns <code>null</code> if timeouts waiting for callable value.
	 * 
	 * @param millisTimeout
	 * @param callable
	 * @return
	 */
	public static <R> R runWithTimeout(long millisTimeout, Callable<R> callable) {
		ExecutorService singleThreadExecutor = Executors.newFixedThreadPool(1);
		Future<R> future = singleThreadExecutor.submit(callable);
		try {
			return future.get(millisTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
		} finally {
			singleThreadExecutor.shutdown();
		}
		return null;
	}

}
