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
 * BlockingReaderQueue A bounded queue, with blocking reads as long as it is
 * empty.
 * 
 * @author erwin
 * @author dirk
 */
public class BlockingReaderQueue extends BlockingQueue {
  private final static Logger logger = LoggerFactory.getLogger(BlockingReaderQueue.class);

  /**
   * Constructor, wrapping a plain queue.
   * 
   * @param queue a plain queue
   */
  public BlockingReaderQueue(IQueue queue) {
    super(queue);
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#put(java.lang.Object)
   */
  public boolean put(Object o) {
    if (logger.isTraceEnabled()) logger.trace("put() - entry - Putting :" + o);

    boolean res = false;

    synchronized (getQueue()) {
      if (getQueue().size() == 0) {
        // wake up waiting threads
        // indicating that a Q entry has arrived
        setNotified(true);
        getQueue().notifyAll();
      }
      res = getQueue().put(o);
    }

    if (logger.isTraceEnabled()) logger.trace("put() - exit - Returning :" + res);
    return res;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#get()
   */
  public Object get() throws EmptyQueueException {
    if (logger.isTraceEnabled()) logger.trace("get() - entry");

    Object res = null;

    synchronized (getQueue()) {
      setNotified(false);
      while (getQueue().size() == 0 && !isNotified()) {
        // block untill a Q entry arrives or the queue is triggered
        try {
          getQueue().wait();
        } catch (InterruptedException e) {
          throw new EmptyQueueException("Interrupted");
        }
      }
      if (getQueue().size() > 0) {
        res = getQueue().get();
      } else {
        // else it was a trigger,
        throw new EmptyQueueException("Triggered");
      }
    }

    if (logger.isTraceEnabled()) logger.trace("get() - exit - Returning :" + res);
    return res;
  }

  /**
   * Get an element from the queue. If it is empty wait at most timeout
   * milliseconds.
   * 
   * @param timeout
   * @return the queue entry.
   * @throws EmptyQueueException if the timeout has passed
   */
  public Object get(long timeout) throws EmptyQueueException {
    if (logger.isTraceEnabled()) logger.trace("get() - entry - timeout :" + timeout);

    Object res = null;

    synchronized (getQueue()) {
      if (getQueue().size() == 0) {
        // block untill a Q entry arrives
        setNotified(false);
        long endTime = System.currentTimeMillis() + timeout;
        while (!isNotified()) {
          long tmpTimeout = endTime - System.currentTimeMillis();
          if (tmpTimeout > 0)
            try {
              getQueue().wait(tmpTimeout);
            } catch (InterruptedException e) {
              logger.debug("get() - queue interrupted");
              throw new EmptyQueueException("Interrupted");
            }
          else
            break;
        }
      }
      if (getQueue().size() > 0) {
        res = getQueue().get();
      } else {
        // else it was a timeout or trigger,
        logger.debug("get() - queue triggered or interrupted");
        throw new EmptyQueueException("Triggered or Timeout");
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
      if (getQueue().size() == 0) {
        setNotified(true);
        // wake up waiting threads
        // indicating that an event happened
        getQueue().notifyAll();
      }
    }
    if (logger.isTraceEnabled()) {
      logger.trace("trigger() - exit");
    }
  }

}