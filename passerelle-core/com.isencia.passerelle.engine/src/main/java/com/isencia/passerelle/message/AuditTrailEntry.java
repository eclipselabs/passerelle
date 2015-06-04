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
package com.isencia.passerelle.message;

import java.io.Serializable;
import java.util.Date;

/**
 * A simple container for all required info for
 * the message audit trail.
 * 
 * @author erwin
 */
public class AuditTrailEntry implements Serializable {
	private Date timeStamp;
	private String actorRef;
	private String action;
	private String extraInfo;

	
	/**
	 * @param timeStamp
	 * @param actorRef
	 * @param action
	 * @param extraInfo
	 */
	public AuditTrailEntry(Date timeStamp, String actorRef, String action, String extraInfo) {
		super();
		this.timeStamp = timeStamp;
		this.actorRef = actorRef;
		this.action = action;
		this.extraInfo = extraInfo;
	}
	
	/**
	 * @return Returns the action.
	 */
	public String getAction() {
		return action;
	}
	/**
	 * @return Returns the actorRef.
	 */
	public String getActorRef() {
		return actorRef;
	}
	/**
	 * @return Returns the extraInfo.
	 */
	public String getExtraInfo() {
		return extraInfo;
	}
	/**
	 * @return Returns the timeStamp.
	 */
	public Date getTimeStamp() {
		return timeStamp;
	}
	
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[AuditTrailEntry:");
		buffer.append(" timeStamp: ");
		buffer.append(timeStamp);
		buffer.append(" actorRef: ");
		buffer.append(actorRef);
		buffer.append(" action: ");
		buffer.append(action);
		buffer.append(" extraInfo: ");
		buffer.append(extraInfo);
		buffer.append("]");
		return buffer.toString();
	}
}