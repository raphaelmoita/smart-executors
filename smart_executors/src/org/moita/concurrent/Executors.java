package org.moita.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.function.IntSupplier;

import org.moita.concurrent.intern.SmartExecutor;

/**
 * @author Raphael Moita.
 * @email raphael.moita@gmail.com
 */
public final class Executors {

	/**
	 * Creates a thread pool that dynamically changes the number of parallel
	 * threads operating based on rules defined by a supplier. Such supplier is
	 * called in a predefined time interval.
	 * 
	 * @param supplierSizeProvider
	 *            - return the pool size based by any internal rule.
	 * @param supplierCheckInterval
	 *            - time interval (in seconds) to reevaluate the thread pool
	 *            size.
	 * @return ExecutorService
	 * 
	 * @Exemplo: Dynamic thread pool that limits in 100 threads when it is
	 *           before moon, otherwise it limits in only 10 threads.
	 * 
	 *           <pre>
	 *           ExecutorService pool = newDinamicThreadPool(() -> getMaxThreads(), 2);
	 * 
	 *           int getMaxThreads() {
	 *           	int maxThreads = 0;
	 *           	final Calendar MOON = Calendar.getInstance();
	 *           	MOON.set(Calendar.HOUR_OF_DAY, 12);
	 *           	if (Calendar.getInstance().before(MOON)) {
	 *           		maxThreads = 100;
	 *           	} else {
	 *           		maxThreads = 10;
	 *           	}
	 *           	return maxThreads;
	 *           }
	 *           </pre>
	 * 
	 */
	public static ExecutorService newDinamicThreadPool(IntSupplier supplierSizeProvider, int supplierCheckInterval) {
		return SmartExecutor.create(supplierSizeProvider, supplierCheckInterval);
	}
}
