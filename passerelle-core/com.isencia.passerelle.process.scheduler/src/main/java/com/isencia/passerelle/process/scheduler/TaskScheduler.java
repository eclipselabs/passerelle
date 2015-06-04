/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.process.model.Context;

/**
 * <p>
 * Contract for a service that provides a scheduling mechanism
 * for handling tasks.
 * </p>
 * <p>
 * All tasks should be delivered to the scheduler via the accept(...) method.
 * <br>
 * The component that is responsible for the actual handling of the tasks is
 * identified by the handler that is also passed as argument.
 * <br>
 * When the given task has been selected for treatment, the matching handler
 * will be notified with the entity.
 * </p>
 * <p>
 * This contract is in a similar problem domain as a <code>java.util.concurrent.ExecutorService</code>.
 * We've taken e.g. the shutdown()-related stuff from there.
 * We should investigate if a <code>TaskScheduler</code> can not be considered as an
 * extension/specialization of an <code>ExecutorService</code>...
 * </p>
 *
 * @author erwin, Davy De Durpel
 * @author puidir
 *
 */
// Remark: left out: monitoring through ResourceUsageReporter
public interface TaskScheduler {

	/**
	 * <p>
	 * Accept the new task context by its designated handler,
	 * and buffer it.
	 * </p>
	 * <p>
	 * When the given task context has been selected for treatment,
	 * the handler will be notified.
	 * </p>
	 *
	 * @param handler
	 * @throws TaskRefusedException in case the resources are over-charged
	 * or some other planned request refusal policy is triggered
	 */
	void accept(Context taskContext, TaskHandler handler) throws TaskRefusedException;

	/**
	 * Clears all pending tasks and returns them,
	 * i.e. the tasks that were not yet delivered to their handlers.
	 *
	 * @return list of pending tasks that have been cleared
	 */
	List<Context> clearPending();
	
	/**
	 * Initiate a nice shutdown, i.e. allow all pending tasks to be processed first,
	 * but do not accept extra tasks in the meantime.
	 * <br>
	 * Remark that this method returns immediately, without blocking until all tasks
	 * have been processed.
	 *
	 * @see awaitTermination()
	 */
	void shutdown();

	/**
	 * Initiate an immediate shutdown and return all pending tasks,
	 * i.e. the tasks that were not yet delivered to their handlers.
	 * <br>
	 * Remark that this method returns immediately, without blocking until all tasks
	 * have been processed.
	 *
	 * @see awaitTermination()
	 *
	 * @return list of pending requests
	 */
	List<Context> shutdownNow();

	/**
	 * Block until the shutdown() sequence has terminated, or the given timeout has expired.
	 * <br>
	 * If no shutdown() is ongoing, returns immediately.
	 *
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
