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

import com.isencia.message.interceptor.MessageInterceptorChain;
import junit.framework.TestCase;

/**
 * MessageInterceptorChainTest
 * 
 * @author erwin
 */
public class MessageInterceptorChainTest extends TestCase {
  MessageInterceptorChain chain = null;
  MockInterceptor interceptor = new MockInterceptor();
  Object message = "message";

  protected void setUp() throws Exception {
    chain = new MessageInterceptorChain();
    chain.add(interceptor);
  }

  protected void tearDown() throws Exception {
    chain = null;
  }

  public void testAdd() {
    assertTrue("Added interceptor not found", chain.interceptors.contains(interceptor));
  }

  public void testAddRemove() {
    assertTrue("remove() should return true", chain.remove(interceptor));
    assertFalse("Removed interceptor still in there", chain.interceptors.contains(interceptor));
  }

  public void testAccept() {
    try {
      chain.accept(message);
      assertEquals("Interceptor didn't receive correct message", message, interceptor.receivedMessage);
    } catch (Exception e) {
      fail("Got an Exception " + e);
    }
  }

  public void testClear() {
    chain.clear();
    assertTrue(chain.interceptors.isEmpty());
  }

}
