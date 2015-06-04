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

package com.isencia.passerelle.actor.dynaport;

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A trivial implementation of an InputPortBuilder, to allow an actor developer
 * to easily add support for dynamically configurable multiple input ports,
 * by setting the names of the desired input ports.
 * 
 * @author delerw
 *
 */
public class InputPortSetterBuilder extends InputPortBuilder {
  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public InputPortSetterBuilder(Entity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }
  
  public void setInputPortNames(String... portNames) {
    changeInputPorts(portNames);
  }
}
