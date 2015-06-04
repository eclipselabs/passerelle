/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.process.actor.flow;

import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * This is an implementation that can be set on an <code>Actor</code> (or other <code>NamedObj</code>) 
 * that is a <code>MessageSequenceGenerator</code>, and generates an exception for the <code>initialMsg</code> that is being evicted.
 * 
 * @author erwin
 *
 */
public class ErrorThrowingEvictedMessageHandler implements EvictedMessagesHandler {
  
  private NamedObj container;

  /**
   * 
   * @param container the <code>MessageSequenceGenerator</code> using this handler;
   * must be a model element, typically an <code>Actor</code>, for this type of handler.
   */
  public ErrorThrowingEvictedMessageHandler(NamedObj container) {
    this.container = container;
  }
  public void handleEvictedMessages(ManagedMessage initialMsg, ManagedMessage... otherMessages) throws PasserelleException {
    throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, "Message evicted from msg sequence src", container, initialMsg, null);
  }
}
