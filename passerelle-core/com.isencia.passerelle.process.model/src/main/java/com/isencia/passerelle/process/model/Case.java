/* Copyright 2012 - iSencia Belgium NV

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

package com.isencia.passerelle.process.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * A <code>Case</code> is the parent object of any <code>Request</code> being processed in a Passerelle flow.
 * <p>
 * The Passerelle process domain model describes the execution of workflow(s) in the context of a certain "situation" or "driver",
 * where the situation is represented by a <code>Case</code>-instance.
 * <br/>
 * As several activities may be triggered in Passerelle, related to the same driver or situation, a <code>Case</code> can also be seen
 * as a grouping of one-or-more <code>Request</code>s and their processing results.
 * </p>
 * <p> 
 * Through the <code>Case.getExternalReference()</code>, the processing context can (optionally) be related to an external entity that could
 * be the driver of the work.
 * </p>
 * <p>
 * E.g. for a customer-support system, the <code>Case</code> could be related to a customer complaint or an order. 
 * The resolution of the complaint, or the execution and delivery of the ordered goods could be controlled by the execution of one-or-more
 * flows, each triggered by specific <code>Request</code>s.
 * </p>
 * <p>
 * <p>
 * Or for a research environment, the Case could be related to an experiment. 
 * And consecutive flow executions (with their Request instances) could represent consecutive experiment phases, 
 * from experiment preparation via automated execution (device control and data acquisition) to data analysis flows.
 * </p>
 * 
 * @author erwin
 *
 */
public interface Case extends Serializable, Identifiable {
  
  /**
   * A case can be linked to an external/business entity, e.g. a trouble ticket or a business order etc.
   * The (optional) <code>referenceKey</code> can refer to this external entity.
   * <p>
   * Alternatively, it can just be used to maintain a more-or-less readable key that can be used to refer to this <code>Case</code>.
   * </p>
   * @return an optional key that can be used to refer to an associated (business) entity, or can serve as a simple readable key,
   * to refer to this <code>Case</code>.
   */
  String getExternalReference();
  
  /**
   * 
   * @return the <code>Request</code>s that are related to processing this case.
   */
  Collection<Request> getRequests();

}
