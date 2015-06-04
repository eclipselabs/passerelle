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

package com.isencia.passerelle.director;

import ptolemy.actor.Actor;
import ptolemy.kernel.util.IllegalActionException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.message.MessageQueue;

/**
 * @author erwin
 *
 */
public interface PasserelleDirector {

  /**
   * Return the configured DirectorAdapter for the given name.
   * If name is null or <code>DirectorAdapter.DEFAULT_ADAPTER_NAME</code>, and no adapter is present yet,
   * the default instance is lazily created and returned.
   * For other names, the specific adapter for that name is searched.
   * If none is found, null is returned.
   * @param adapterName
   * @return
   * @throws IllegalActionException 
   */
  DirectorAdapter getAdapter(String adapterName) throws IllegalActionException;
  
  /**
   * 
   * @param actor
   * @return
   * @throws InitializationException
   */
  MessageQueue newMessageQueue(Actor actor) throws InitializationException;
}
