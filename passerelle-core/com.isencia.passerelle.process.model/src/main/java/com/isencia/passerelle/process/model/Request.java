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
import java.util.Date;

/**
 * A <code>Request</code> instance is an (almost) immutable container for the original data received from an initiator, 
 * for which work is performed in a Passerelle process.
 * <p>
 * All status changes, task results etc are maintained in an associated processing <code>Context</code>.
 * For specific needs, Passerelle processes are allowed to add extra <code>Attribute</code>s.
 * <br/>
 * But the originally received data can not be tampered with...
 * </p>
 * @author erwin
 */
public interface Request extends Serializable, Identifiable, AttributeHolder {

  /**
   * @return the <code>Case</code> to which this <code>Request</code> is related.
   */
  Case getCase();

	/**
	 * @return a unique identifier of the request initiator, i.e. the party or system component
	 * responsible for the initiation of this request. 
	 * 
	 */
	String getInitiator();
	
  /**
   * @return a unique identifier of the request executor, i.e. the party or system component
   * responsible for the handling of this request. 
   * 
   */
	String getExecutor();
	
	/**
	 * @return a unique identifier of the request category
	 * 
	 */
	String getDataTypes();
	
	/**
	 * 
	 * @param executor
	 */
	void setExecutor(String executor);
	
  /**
   * A correlation ID can be specified by the request initiator. 
   * Passerelle will then ensure that in any notifications, acknowledgements or other kinds of feedback, the correlation ID will be available.
   * This can be used by external systems to facilitate correlating their original requests to a Passerelle runtime, with later asynchronous responses.
   * 
   * @return the correlation Id that was received from the request initiator.
   */
  String getCorrelationId();

  /**
   * @return the type of request, which typically determines the flow that must be executed to handle it.
   */
  String getType();

  /**
   * 
   * @return the context containing all current status info about the lifecycle of the request processing, tasks that were executed, results obtained etc.
   */
  Context getProcessingContext();
  
  Date getCreationTS();
}
