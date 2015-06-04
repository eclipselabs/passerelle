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

import com.isencia.passerelle.core.Port;

/**
 * In the new Passerelle Actor API, the MessageOutputContext is a generic container
 * for attributes etc that are related to one specific generated output message for an actor.
 * 
 * @author erwin
 */
public class MessageOutputContext {

	private Port port;
	private ManagedMessage message;
	
	public MessageOutputContext(Port port, ManagedMessage message) {
		this.port = port;
		this.message = message;
	}
	
	public Port getPort() {
		return port;
	}

	public ManagedMessage getMessage() {
		return message;
	}
}
