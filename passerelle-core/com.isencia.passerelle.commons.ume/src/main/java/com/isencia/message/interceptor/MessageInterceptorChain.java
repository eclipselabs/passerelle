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
package com.isencia.message.interceptor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * MessageInterceptorChain Implementation of an interceptor chain
 * 
 * @author dirk
 */
public class MessageInterceptorChain implements IMessageInterceptorChain {

  protected List<IMessageInterceptor> interceptors = new LinkedList<IMessageInterceptor>();

  public void add(IMessageInterceptor interceptor) {
    interceptors.add(interceptor);
  }

  public boolean remove(IMessageInterceptor interceptor) {
    return interceptors.remove(interceptor);
  }

  public void clear() {
    interceptors.clear();
  }

  public Object accept(Object message) throws Exception {
    Iterator<IMessageInterceptor> iterator = interceptors.iterator();
    while (iterator.hasNext()) {
      IMessageInterceptor interceptor = iterator.next();
      message = interceptor.accept(message);
    }
    return message;
  }
}
