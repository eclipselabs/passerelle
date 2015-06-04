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

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * A <code>AggregationStrategy</code> instance can be plugged in <code>MessageSequenceGenerator</code>s and other things handling
 * message sets. The purpose is to be able to configure any desired kind of aggregation/merging/folding logic on message sets, 
 * and to obtain a single result message.
 * 
 * @author erwin
 */
public interface AggregationStrategy {
  
  /**
   * 
   * @param initialMsg
   * @param otherMessages
   * @return the result of aggregating initialMsg with all otherMessages
   * 
   * @throws MessageException
   */
  ManagedMessage aggregateMessages(ProcessManager processManager,ManagedMessage initialMsg, ManagedMessage... otherMessages) throws MessageException;

}
