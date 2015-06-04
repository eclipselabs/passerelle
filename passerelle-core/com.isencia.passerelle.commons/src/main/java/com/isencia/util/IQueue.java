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
 * IQueue Interface contracts for queues
 * 
 * @author dirk
 */
public interface IQueue {

  /**
   * @param o
   * @return
   */
  public boolean put(Object o);

  /**
   * @return
   * @throws EmptyQueueException
   */
  public Object get() throws EmptyQueueException;

  /**
   * @return
   */
  public int size();

  /**
   * @return
   * @throws EmptyQueueException
   */
  public Object look() throws EmptyQueueException;

  /**
   * @return
   */
  public boolean isEmpty();

  /**
   * @return
   */
  public int getCapacity();

  /**
   * @param newCapacity
   * @return
   */
  public boolean setCapacity(int newCapacity);

  /**
     * 
     *
     */
  public void clear();
}
