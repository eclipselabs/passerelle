package com.isencia.passerelle.process.service;

import java.util.Collection;
import java.util.Set;

import com.isencia.passerelle.process.model.ContextProcessingCallback;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.runtime.ProcessHandle;

public interface ProcessManagerService {
	ProcessFactory getFactory();
	ProcessPersister getPersister();
	ProcessManager addProcessManager(ProcessManager processManager);
	Set<ProcessHandle> getProcessHandles(String userId, boolean master);
	Set<ProcessHandle> getProcessHandles(String userId, boolean master,String type);
  ProcessManager getProcessManager(Request request);
  ProcessManager getProcessManager(ProcessHandle handle);
  ProcessManager getProcessManager(String id);
	ProcessManager removeProcessManager(String id);
	
	 /**
   * Subscribe the given listener for status change notifications for all requests & tasks.
   * 
   * @param callback
	 * @return 
   */
  boolean subscribeToAll(ContextProcessingCallback callback);
  
  /**
   * Unsubscribe the given overall listener.
   * 
   * @param callback
   */
  boolean unsubscribe(ContextProcessingCallback callback);

  /**
   * FIXME review the structure of events and notifications to find a way to add a plain sendEvent(ContextEvent) i.o. needing to check for context state, exception ets
   * to be able to invoke the right notify method. Then we can add a notifyEvent method here, i.o. this getter being invoked from inside ProcessManager impls.
   * 
   * @return
   */
  Collection<ContextProcessingCallback> getOverallCallbacks();  

}
