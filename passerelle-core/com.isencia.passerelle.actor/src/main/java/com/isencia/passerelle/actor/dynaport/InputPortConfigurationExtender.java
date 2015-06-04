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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import com.isencia.passerelle.ext.ConfigurationExtender;

/**
 * @author delerw
 *
 */
public class InputPortConfigurationExtender extends InputPortBuilder implements ConfigurationExtender, ValueListener {
  private static Logger LOGGER = LoggerFactory.getLogger(InputPortConfigurationExtender.class);

  private static final String INPUT_PORTNAMES = "Input port names (comma-separated)";
  public StringParameter inputPortNamesParameter = null;
  
  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public InputPortConfigurationExtender(Entity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    inputPortNamesParameter = new StringParameter(container, INPUT_PORTNAMES);
    inputPortNamesParameter.addValueListener(this);
  }
  
  // TODO : problem is that there's no way to pass error info to the source of the change
  // so no possibility to warn user that the configured ports could not be created correctly
  // Maybe need to do this change via the attributeChanged() of the containing actor after all?
  // But then this config extender can only work on some specific Passerelle actors that 
  // have adapted attributeChanged()...
  public void valueChanged(Settable settable) {
    // should always be our output port parameter, but still...
    if (settable == inputPortNamesParameter) {
      String inputPortNames = inputPortNamesParameter.getExpression();
      changeInputPorts(inputPortNames);
    }
  }
}
