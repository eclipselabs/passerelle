/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

import com.isencia.passerelle.process.model.Context;

/**
 * @author puidir
 *
 */
public interface TaskHandler {

	/**
	 * This method's implementation encapsulates
	 * the actual task processing.
	 * <br>
	 * When the processing is finished, the given resourceToken
	 * should be released (via its <code>release()</code> method).
	 *
	 * @param taskContext
	 * @param resourceToken
	 */
	void handle(Context taskContext, ResourceToken resourceToken);

}
