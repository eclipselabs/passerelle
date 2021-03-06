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

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * Defines the contract for actors that generate message sequences.
 * <p>
 * Such actors often need to maintain some state information on the progress and/or completion
 * of the processing of "their" generated sequences.<br/>
 * To avoid memory problems in model executions with high throughput rates, a state retention policy should be implemented.
 * This must include following elements :
 * <ul>
 * <li>enable/disable statefulness</li>
 * <li>optionally set eviction criteria (time-based and/or count-based) at construction time</li>
 * <li>optionally set an <code>EvictedMessagesHandler</code> that determines what must be done when messages/sequences are evicted due to retention limitation issues</li>
 * </ul>
 * </p>
 * <p>
 * A stateful <code>MessageSequenceGenerator</code> can be configured with a <code>AggregationStrategy</code> to define any desired
 * aggregation logic that should be performed on a message sequence once each member message has been individually processed.
 * </p>
 * <p>
 * Stateless instances are by definition unable to aggregate previously processed sequenced messages, so don't have a use for a <code>AggregationStrategy</code>.
 * </p>
 * 
 * @author erwin
 */
public interface MessageSequenceGenerator {
  
  // Header name to set the name of the MessageSequenceGenerator actor in each outgoing msg.
  // Join actors must then search for this actor with that name.
  // This assumes that both actors are within the same containing CompositeActor.
  String HEADER_SEQ_SRC = "__PSRL_SEQ_SRC_REF";
  
  /**
   * 
   * @return true if this source retains state on progress of its generated sequenced messages.
   */
  boolean isStateful();

  /**
   * 
   * @param seqMsg
   * @return true if the seqMsg was generated by this one (and it still knows about it ;-) )
   */
  boolean wasGeneratedHere(ManagedMessage seqMsg);
  
  /**
   * A stateful <code>MessageSequenceGenerator</code> with a configured <code>AggregationStrategy</code> will return the aggregated result message,
   * when all member messages of the sequence have been marked as processed.<br/> 
   * If no <code>AggregationStrategy</code> has been set, this method will return this last received <code>seqMsg</code>. <br/>
   * If the sequence has not yet been completely processed, or this seqMsg is not <code>fromThisSource</code>, this method returns null.
   * <br/>
   * A stateless <code>MessageSequenceGenerator</code> always returns the received <code>seqMsg</code>.
   * 
   * @param seqMsg
   * @return the aggregated result of all retained messages for seqMsg's sequence, if all have finished their individual processing; 
   * null otherwise, i.e. when there are still messages from the sequence that have not yet returned from their processing.
   * @throws ProcessingException
   */
  ManagedMessage aggregateProcessedMessage(ProcessManager processManager,ManagedMessage seqMsg) throws ProcessingException;

  /**
   * 
   * @return the currently configured strategy for aggregating message sequences,
   * after their messages have been individually processed.
   */
  AggregationStrategy getAggregationStrategy();
  
  /**
   * Sets the strategy for aggregating message sequences,
   * after their messages have been individually processed.
   * @param aggregationStrategy
   */
  void setAggregationStrategy(AggregationStrategy aggregationStrategy);
  
  /**
   * @return the handler that will be notified of messages that are being evicted from the source's state management cache
   */
  EvictedMessagesHandler getEvictedMessagesHandler();

  /**
   * @param evictedMessagesHandler the handler that must be notified of messages that are being evicted from the source's state management cache
   */
  void setEvictedMessagesHandler(EvictedMessagesHandler evictedMessagesHandler);
}
