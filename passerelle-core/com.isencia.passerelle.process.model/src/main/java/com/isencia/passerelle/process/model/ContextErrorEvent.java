/**
 * 
 */
package com.isencia.passerelle.process.model;

/**
 * Specific type of event to maintain information related to
 * an error that occurred during the processing of a request/task.
 * 
 * @author erwin
 *
 */
public interface ContextErrorEvent extends ContextEvent {

  /**
   * 
   * @return
   */
  ErrorItem getErrorItem();
}
