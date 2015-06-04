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


/**
 * Enumerator of all possible states of a <code>Request</code>'s <code>Context</code>.
 * 
 * @author erwin
 *
 */
public enum Status {
	// these are transient states
	CREATED,STARTED,PROCESSING,PAUSED,DELAYED,RESUMED,PENDING,RESTARTED,
	// these are final states
	FINISHED,CANCELLED,TIMEOUT,ERROR,INTERRUPTED;
	
	public boolean isFinalStatus() {
		return this.compareTo(FINISHED)>=0;
	}
}
