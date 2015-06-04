package com.isencia.passerelle.process.service;

import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.runtime.ProcessHandle;

public interface ProcessManager {


  String REPORT_EVENT_TYPE = "REPORTED";
  String RESTARTING = "restarting";

  /**
   * Obtain the manager's ID, via a shortcut for invoking <code>getHandle.getProcessId()</code>.
   * 
   * @return the manager's ID.
   */
  String getId();

  /**
   * @return the factory to be used for creating new process model entities
   */
  ProcessFactory getFactory();

  /**
   * @return the persister to be used to save/update/find/... entities in a persistent storage
   */
  ProcessPersister getPersister();

  /**
   * @return the handle to the flow-based process behind this ProcessManager
   */
  ProcessHandle getHandle();

  /**
   * 
   * @return the Request entity that is being handled in this manager's process
   */
  Request getRequest();

  /**
   * Finds the given Task within the Request Context's collection of finished/ongoing tasks.
   * 
   * @param id
   * @return the Task entity that was (or is being) executed as part of this manager's process
   */
  Task getTask(long id);
  
  /**
   * 
   * @param scopeGroup
   * @param scope
   * @param ctxt
   */
  void registerScopedProcessContext(String scopeGroup, String scope, Context ctxt);
  
  /**
   * 
   * @param scopeGroup
   * @param scope
   * @return
   */
  Context getScopedProcessContext(String scopeGroup, String scope);

  /**
   * 
   * @param task
   * @return the process context for the given task, which might be a scoped one if the task's actor is on a scoped branch in the flow
   */
  Context getProcessContextForTask(Task task);
  
  /**
   * 
   * @param scopeGroup
   * @param scope
   * @return
   */
  Context removeScopedProcessContext(String scopeGroup, String scope);

  /**
   * Notify listeners that the processing of the request was cancelled.
   */
  void notifyCancelled();

  /**
   * Notify listeners that the processing of a task was cancelled.
   * 
   * @param task
   *          Task that got cancelled
   */
  void notifyCancelled(Task task);

  /**
   * Notify listeners that the processing of the request has finished with an error.
   * 
   * @param error
   *          The error that happened during processing
   */
  void notifyError(ErrorItem error, Throwable cause);

  /**
   * Notify listeners that the processing of a task has finished with an error.
   * 
   * @param task
   *          Task that finished with an error
   * @param error
   *          The error that happened during processing
   */
  void notifyError(Task task, ErrorItem error, Throwable cause);

  /**
   * Notify listeners that the processing of a task has finished with an error.
   * 
   * @param task
   *          Task that finished with an error
   * @param error
   *          The error that happened during processing
   */
  void notifyError(Task task, Throwable error);

  /**
   * Notify listeners that the processing of the request has finished with an error.
   * 
   * @param error
   *          The error that happened during processing
   */
  void notifyError(Throwable error);

  /**
   * Notify all listeners about a given context event.
   * 
   * @param event
   */
  void notifyEvent(ContextEvent event);

  /**
   * Notify all listeners about a given context event on the request.
   * 
   * @param eventType
   * @param message
   */
  void notifyEvent(String eventType, String message);

  /**
   * Notify all listeners about a given context event on a task.
   * 
   * @param task
   * @param eventType
   * @param message
   */
  void notifyEvent(Task task, String eventType, String message);

  /**
   * Notify listeners that the processing of the request has finished.
   */
  void notifyFinished();

  /**
   * Notify listeners that the processing of a task has finished.
   * 
   * @param task
   *          Task that finished
   */
  void notifyFinished(Task task);

  /**
   * Notify listeners that the processing of the request is pending completion. It has done its work but remains in
   * 'ongoing' state until something else finishes it.
   */
  void notifyPendingCompletion();

  /**
   * Notify listeners that the processing of a task is pending completion. It has done its work but remains in 'ongoing'
   * state until something else finishes it.
   * 
   * @param task
   *          Task that is pending completion
   */
  void notifyPendingCompletion(Task task);

  /**
   * Notify listeners that the processing of a task was restarted.
   * 
   * @param task
   *          Task that restarted
   */
  void notifyRestarted(Task task);

  /**
   * Notify listeners that the processing of the request has started.
   */
  void notifyStarted();

  /**
   * Notify listeners that the processing of a task has started.
   * 
   * @param task
   *          Task that started
   */
  void notifyStarted(Task task);

  /**
   * Notify listeners that the processing of the request has timed out.
   */
  void notifyTimeOut();

  /**
   * Notify listeners that the processing of the task has timed out.
   * 
   * @param task
   *          Task that timed out
   */
  void notifyTimeOut(Task task);

  /**
   * Start the flow for this request.
   */
  boolean start();

  /**
   * Pause the flow for this request.
   */
  boolean pause(long timeOut, TimeUnit timeOutUnit);

  /**
   * Restart the flow for this request from the given Task.
   */
  boolean restart(long taskId, long timeOut, TimeUnit timeOutUnit);

  /**
   * Resume the flow for this request.
   */
  boolean resume(long timeOut, TimeUnit timeOutUnit);

  /**
   * Stop the flow for this request.
   */
  boolean stop(long timeOut, TimeUnit timeOutUnit);

  /**
   * Subscribe the given callback to status change notifications of the given task,
   * that should be within the request scope of this process manager.
   * 
   * @param task
   *          Task for which status change notifications are posted
   * @param callback
   *          Callback that will be notified
   */
  void subscribe(Task task, ContextProcessingCallback callback);

  /**
   * Subscribe the given callback to status change notifications for any/all task(s) 
   * within the request scope of this process manager.
   * 
   * @param callback
   */
  void subscribe(ContextProcessingCallback callback);

  /**
   * Unsubscribe the given callback. It can be an "all" subscriber or one subscribed for a specific task.
   * 
   * @param callback
   */
  void unsubscribe(ContextProcessingCallback callback);
  
  /**
   * 
   * @return status of the main request
   */
  public Status getStatus();

  /**
   * @param task
   * @return status of the Task
   */
  public Status getStatus(Task task);
}
