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
package com.isencia.passerelle.core;

/**
 * PortMode represents whether a port should be polled
 * to see if a message is available, or whether the message should be pushed 
 * to an actor.
 * <p>
 * Remark that polling a port with <code>Port.get()</code> blocks until
 * a message is available or until the Port can be certain that no more
 * messages will ever arrive in the life of the current model.
 * </p>
 * <p>
 * Since Passerelle v8.3, the AGNOSTIC mode has been added. 
 * This means that the engine should not assume anything about the Port semantics, 
 * and that the container component (typically an actor)
 * is responsible for any ad-hoc approach to obtain the messages from the input port.
 * </p>
 * @author erwin
 */
public enum PortMode {
  
	PULL(true),PUSH(false),AGNOSTIC(false);
	
  private boolean blocking;

  private PortMode(boolean blocking) {
    this.blocking = blocking;
  }
  
  public boolean isBlocking() {
    return blocking;
  }
}
