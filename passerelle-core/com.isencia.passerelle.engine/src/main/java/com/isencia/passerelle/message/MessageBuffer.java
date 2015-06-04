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

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.Port;


/**
 * Contract for things maintaining a queue of Messages,
 * in MessageInputContexts maintaining the link between a
 * received message, the port on which it was received and
 * the channel index in that port on which it was received.
 * 
 * @author delerw
 *
 */
public interface MessageBuffer {

	/**
	 * 
	 * @return the queue on which the msgs are buffered
	 */
	MessageQueue getMessageQueue();
	
	/**
	 * 
	 * @param ctxt
	 * @throws PasserelleException 
	 */
	void offer(MessageInputContext ctxt) throws PasserelleException;
	
	/**
	 * 
	 * @param p
	 * @return true if the port can use this buffer for its
	 * incoming messages.
	 */
	boolean acceptInputPort(Port p);
	
	/**
	 * 
	 * @param provider should be not-null
	 * @return true if the given provider was not yet registered
	 */
	boolean registerMessageProvider(MessageProvider provider);
	
	/**
	 * When all providers have been unregistered, the MessageBuffer
	 * can safely shut down, after all its buffered messages have been processed...
	 * 
	 * @param provider
	 * @return true if the given provider was registered, false if not
	 */
	boolean unregisterMessageProvider(MessageProvider provider);
}
