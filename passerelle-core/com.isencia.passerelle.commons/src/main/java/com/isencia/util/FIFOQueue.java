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

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * FIFOQueue TODO: class comment
 * 
 * @author wim
 */
public class FIFOQueue implements IQueue {

  public static final int INFINITE_CAPACITY = 0;
  LinkedList queue = new LinkedList();
  private int capacity = INFINITE_CAPACITY;

  /** Creates a new instance of FIFOQueue */
  public FIFOQueue() {
  }

  public FIFOQueue(int capacity) {
    if (capacity > 0) this.capacity = capacity;
  }

  public boolean put(Object o) {
    if (capacity == INFINITE_CAPACITY || capacity > queue.size()) {
      queue.add(o);
      return true;
    } else
      return false;
  }

  public Object get() throws EmptyQueueException {
    try {
      return queue.removeFirst();
    } catch (NoSuchElementException e) {
      throw new EmptyQueueException(e.getMessage());
    }
  }

  public int size() {
    return queue.size();
  }

  public Object look() throws EmptyQueueException {
    try {
      return queue.getFirst();
    } catch (NoSuchElementException e) {
      throw new EmptyQueueException(e.getMessage());
    }
  }

  public boolean isEmpty() {
    return queue.isEmpty();
  }

  public int getCapacity() {
    return capacity;
  }

  public boolean setCapacity(int newCapacity) {
    if (newCapacity > size()) {
      capacity = newCapacity;
      return true;
    }
    return false;
  }

  public void clear() {
    queue.clear();
  }
}