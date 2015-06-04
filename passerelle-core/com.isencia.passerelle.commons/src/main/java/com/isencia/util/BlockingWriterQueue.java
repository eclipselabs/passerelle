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
package com.isencia.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BlockingWriterQueue A bounded queue, with blocking writes as long as it is
 * full.
 * 
 * @author erwin
 * @author dirk
 */
public class BlockingWriterQueue extends BlockingQueue {
  private final static Logger logger = LoggerFactory.getLogger(BlockingWriterQueue.class);

  /**
   * Constructor, wrapping a plain queue.
   * 
   * @param queue a plain queue
   */
  public BlockingWriterQueue(IQueue queue) {
    super(queue);
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#put(java.lang.Object)
   */
  public boolean put(Object o) {
    if (logger.isTraceEnabled()) logger.trace("put() - entry - Putting :" + o);

    boolean isAdded = false;

    synchronized (getQueue()) {
      setNotified(false);
      while (!isAdded && !isNotified()) {
        isAdded = getQueue().put(o);
        try {
          getQueue().wait();
        } catch (InterruptedException e) {
          return false;
        }
      }
    }
    if (logger.isTraceEnabled()) logger.trace("put() - exit - Returning :" + isAdded);
    return isAdded;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#get()
   */
  public Object get() throws EmptyQueueException {
    if (logger.isTraceEnabled()) logger.trace("get() - entry");

    Object res = null;

    synchronized (getQueue()) {
      if (getQueue().size() > 0) {
        res = getQueue().get();
        setNotified(true);
      } else {
        throw new EmptyQueueException("Queue is empty");
      }
    }

    if (logger.isTraceEnabled()) logger.trace("get() - exit - Returning :" + res);
    return res;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.BlockingQueue#trigger()
   */
  public void trigger() {
    if (logger.isTraceEnabled()) {
      logger.trace("trigger() - entry");
    }
    synchronized (getQueue()) {
      if (!getQueue().isEmpty()) {
        setNotified(true);
        // wake up waiting threads
        // indicating that an event happend
        getQueue().notifyAll();
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("trigger() - exit");
    }
  }

}