/**
 * 
 */
package com.isencia.passerelle.process.scheduler;

/**
 * @author puidir
 *
 */
public class SchedulerException extends Exception {

	private static final long serialVersionUID = 1L;

	public SchedulerException(String message) {
		super(message);
	}
	
	public SchedulerException(String message, Throwable cause) {
		super(message, cause);
	}
}
