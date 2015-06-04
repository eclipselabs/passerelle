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

/**
 * SyncLock TODO: class comment
 * 
 * @author dirk
 */
public class SyncLock {
  // ~ Instance/static variables
  // ..............................................................................................................................

  // flag to indicate that a wait() state is unlocked by a notify()
  private boolean isNotified = false;
  private Lock lock = new Lock(false);

  // ~ Constructors
  // ...........................................................................................................................................

  /**
   * Creates a new SyncLock object.
   */
  public SyncLock() {
  }

  // ~ Methods
  // ................................................................................................................................................

  /**
   * DOCUMENT ME !
   * 
   * @param timeout
   * @return
   */
  public boolean get(long timeout) {
    synchronized (lock) {
      if (!lock.getState()) {
        // block untill a lock is set
        isNotified = false;

        long endTime = System.currentTimeMillis() + timeout;
        while (!isNotified) {
          long tmpTimeout = endTime - System.currentTimeMillis();
          if (tmpTimeout > 0) {
            try {
              lock.wait(tmpTimeout);
            } catch (InterruptedException e) {
              // Just check the state and return
            }
          } else
            break;
        }
      }

      if (lock.getState()) {
        lock.setState(false);
        return true;
      } else
        return false;
    }
  }

  /**
   * DOCUMENT ME !
   * 
   * @return
   */
  public boolean get() {
    synchronized (lock) {
      isNotified = false;
      while (!lock.getState() && !isNotified) {
        // block untill a lock is set or the queue is triggered
        try {
          lock.wait();
        } catch (InterruptedException e) {
          // Just check the state and return
        }
      }

      if (lock.getState()) {
        lock.setState(false);
        return true;
      } else
        return false;
    }
  }

  /**
   * DOCUMENT ME !
   */
  public void set() {
    synchronized (lock) {
      isNotified = true;
      lock.setState(true);
      lock.notifyAll();
    }
  }

  /**
   * DOCUMENT ME !
   */
  public void trigger() {
    synchronized (lock) {
      isNotified = true;

      // wake up waiting threads
      // indicating that an event happend
      lock.notifyAll();
    }
  }

  // ~ Inner classes
  // ..........................................................................................................................................

  private class Lock {
    // ~ Instance/static variables
    // ..........................................................................................................................

    boolean state = false;

    // ~ Constructors
    // .......................................................................................................................................

    public Lock(boolean state) {
      this.state = state;
    }

    // ~ Methods
    // ............................................................................................................................................

    /**
     * Sets the state.
     * 
     * @param state The state to set
     */
    public void setState(boolean state) {
      this.state = state;
    }

    /**
     * Returns the state.
     * 
     * @return boolean
     */
    public boolean getState() {
      return state;
    }
  }
}