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
 * BlockingQueue A base class for a bounded queue, with blocking reads and/or
 * writes.
 * 
 * @author erwin
 * @author dirk
 */
public abstract class BlockingQueue implements IQueue {
  private final static Logger logger = LoggerFactory.getLogger(BlockingQueue.class);

  private IQueue queue = null;
  // flag to indicate that a wait() state is unlocked by a notify()
  private boolean isNotified = false;

  /**
   * Constructor, wrapping a plain queue.
   * 
   * @param queue a plain queue
   */
  public BlockingQueue(IQueue queue) {
    this.queue = queue;
  }

  /**
   * @return Returns the isNotified.
   */
  protected boolean isNotified() {
    return isNotified;
  }

  /**
   * @param isNotified The isNotified to set.
   */
  protected void setNotified(boolean isNotified) {
    this.isNotified = isNotified;
  }

  /**
   * @return Returns the queue.
   */
  protected IQueue getQueue() {
    return queue;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#isEmpty()
   */
  public boolean isEmpty() {
    return queue.isEmpty();
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#clear()
   */
  public void clear() {
    if (logger.isTraceEnabled()) {
      logger.trace("clear() - entry");
    }
    queue.clear();
    if (logger.isTraceEnabled()) {
      logger.trace("clear() - exit");
    }
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#getCapacity()
   */
  public int getCapacity() {
    return queue.getCapacity();
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#look()
   */
  public Object look() throws EmptyQueueException {
    if (logger.isTraceEnabled()) {
      logger.trace("look() - entry");
    }
    Object res = queue.look();
    if (logger.isTraceEnabled()) {
      logger.trace("look() - exit - Result :" + res);
    }
    return res;
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#setCapacity(int)
   */
  public boolean setCapacity(int newCapacity) {
    if (logger.isTraceEnabled()) {
      logger.trace("setCapacity() - value :" + newCapacity);
    }
    return queue.setCapacity(newCapacity);
  }

  /*
   * (non-Javadoc)
   * @see be.isencia.util.IQueue#size()
   */
  public int size() {
    return queue.size();
  }

  /**
   * Triggering a queue will notify all blocked threads.
   */
  public abstract void trigger();

}