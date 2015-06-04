/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

/**
 * Generated in case the resources are over-charged
 * or some other planned request refusal policy is triggered.
 *
 * @author erwin
 * @author puidir
 *
 */
public class TaskRefusedException extends SchedulerException {

	private static final long serialVersionUID = 1L;

	public TaskRefusedException(String message) {
		super(message);
	}

	public TaskRefusedException(String message, Throwable cause) {
		super(message, cause);
	}

}
