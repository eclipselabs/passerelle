package com.isencia.passerelle.process.model;

import com.isencia.passerelle.process.model.ContextEvent;

/**
 * This type of call-back can be used to react on events generated during the processing
 * of a {@link com.isencia.passerelle.process.model.Context}
 *
 * @author puidir
 */
public interface ContextProcessingCallback {

	/**
	 * Call-back method via which one can be notified
	 * that the context for the given key has started.
	 * <br/>
	 * This event is typically meaningful for tasks that are scheduled asynchronously.
	 * If there's a long time between the task's creation/submission, and it's being started,
	 * it means that it was queued for a significant time. This is an indication that the
	 * passerelle engine is under heavy load.
	 *
	 * @param event
	 */
	void contextStarted(ContextEvent event);

	/**
	 * Call-back method via which one can be notified
	 * that the context for the given key is finished.
	 *
	 * @param event
	 */
	void contextFinished(ContextEvent event);

	/**
	 * Call-back method via which one can be notified
	 * that the timeout for the given context has expired.
	 *
	 * @param event
	 */
	void contextTimeOut(ContextEvent event);

	/**
	 * Call-back method via which one can be notified
	 * that the context is 'pending completion' (has done its work but remains
	 * in 'ongoing' state until something else finishes it).
	 *
	 * @param event
	 */
	void contextPendingCompletion(ContextEvent event);
	
	/**
	 * Call-back method via which one can be notified
	 * that the processing of the context for the given key
	 * has encountered an error.
	 *
	 * @param event
	 * @param error can be null
	 */
	void contextError(ContextEvent event, Throwable error);

	/**
	 * Call-back method via which one can be notified
	 * that the processing of the context for the given key
	 * was interrupted.
	 *
	 * @param event
	 */	
	void contextInterrupted(ContextEvent event);
	
	/**
	 * Call-back method via which one can be notified
	 * that the processing of the context for the given key
	 * was cancelled
	 * 
	 * @param event
	 */
	void contextWasCancelled(ContextEvent event);
}
