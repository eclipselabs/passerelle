/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.isencia.passerelle.domain.et.impl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.runtime.Event;
import com.isencia.passerelle.domain.et.EventHandler;

/**
 * 
 * @author delerw
 */
public class ThreadPoolEventDispatcher extends SimpleEventDispatcher {

  private final static Logger LOGGER = LoggerFactory.getLogger(ThreadPoolEventDispatcher.class);

  // theadpool for the eventQSinks
  private ExecutorService queueDepletionExecutor;

  public ThreadPoolEventDispatcher(String name, int threadCount, EventHandler... handlers) {
    super(name, handlers);

    queueDepletionExecutor = Executors.newFixedThreadPool(threadCount);
  }

  protected Logger getLogger() {
    return LOGGER;
  }
  
  @Override
  public void shutdown() {
    queueDepletionExecutor.shutdown();
    super.shutdown();
  }
  
  @Override
  public List<Event> shutdownNow() {
    queueDepletionExecutor.shutdownNow();
    return super.shutdownNow();
  }

  @Override
  public boolean dispatch(long timeOut) throws InterruptedException {
    int evtCount = getPendingEventCount();
    boolean hasDispatchedSomething = false;
    if (evtCount > 0) {
      CompletionService<Boolean> execComplSvc = new ExecutorCompletionService<Boolean>(queueDepletionExecutor);
      for (int i = 0; i < evtCount; ++i) {
        execComplSvc.submit(new EventQueueSink(timeOut));
      }
      for (int i = 0; i < evtCount; ++i) {
        try {
          // Watch out! Order of the RHS is important.
          // The ...get() must come first as we want to ensure that we wait for ALL tasks to have completed.
          // If it would be at the end, the Java logical expression evaluation would no longer execute it once
          // the hasDispatchedSomething has become true!
          hasDispatchedSomething = execComplSvc.take().get() || hasDispatchedSomething;
        } catch (ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    } else {
      // block the calling thread until an event has arrived, or timeout is passed
      // if an event arrives, it will also be processed in the calling thread...
      super.dispatch(timeOut);
    }
    getLogger().debug(this + " hasDispatchedSomething " + hasDispatchedSomething);
    return hasDispatchedSomething;
  }

  private class EventQueueSink implements Callable<Boolean> {
    private long timeout;

    public EventQueueSink(long timeout) {
      this.timeout = timeout;
    }

    public Boolean call() throws Exception {
      String name = getName() + " - " + Thread.currentThread().getName();
      getLogger().trace("Starting dispatch {}", name);
      try {
        boolean hasDispatchedSomething = ThreadPoolEventDispatcher.super.dispatch(timeout);
        getLogger().debug(name + " hasDispatchedSomething " + hasDispatchedSomething);
        return hasDispatchedSomething;
      } finally {
        getLogger().trace("Finished dispatch {}", name);
      }
    }
  }
}
