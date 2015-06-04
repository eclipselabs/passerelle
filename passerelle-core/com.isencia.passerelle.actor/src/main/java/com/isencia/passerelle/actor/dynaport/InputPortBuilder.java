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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;

/**
 * This base class provides the basis for "mixin" components
 * that can be registered on actors to add support for dynamically creating
 * an arbitrary number of named input ports.
 * <p>
 * The implementation is based on extracting the required logic from 
 * DynamicNamedInputPortsActor, to allow using it with actors without
 * enforcing any actor class inheritance limitations.
 * </p>
 * @author erwin
 *
 */
public class InputPortBuilder extends Attribute {
  private static Logger LOGGER = LoggerFactory.getLogger(InputPortBuilder.class);

  /**
   * the set of configured/required input port names
   */
  private Set<String> inputPortNames = new HashSet<String>();
  // this will deliver a secured interface on the available input port names
  private Set<String> inputPortNamesForContainerAccess = Collections.unmodifiableSet(inputPortNames);

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public InputPortBuilder(Entity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }
  
  /**
   * @return the container cast to Entity, to allow easy Port retrieval etc
   */
  protected Entity getContainerEntity() {
    return (Entity) super.getContainer();
  }

  public Collection<String> getInputPortNames() {
    return inputPortNamesForContainerAccess; 
  }

  /**
   * @return Returns the inputPorts in a copied list.
   * Modifying the list contents has no impact on the ports that
   * are actually present on the containing entity.
   */
  public List<Port> getInputPorts() {
    // in order to avoid cloning issues
    // when we would maintain the list of dynamically cfg-ed
    // input ports in an instance variable,
    // we build this list dynamically here from
    // Ptolemy's internal port list
    List<Port> ports = new ArrayList<Port>();
    for (String portName : inputPortNames) {
      Port p = (Port) getContainerEntity().getPort(portName);
      if (p != null)
        ports.add(p);
      else {
        LOGGER.error("{} - internal error - configured port not found with name {}", getContainerEntity().getFullName(), portName);
      }
    }
    return ports;
  }

  /**
   * @param portNames comma-separated list of port names
   */
  protected void changeInputPorts(String portNames) {
    LOGGER.trace("{} - changeInputPorts() - entry - portNames : {}", getContainerEntity().getFullName(), portNames);
    String[] newPortNames = portNames.split(",");
    changeInputPorts(newPortNames);
    LOGGER.trace("{} - changeInputPorts() - exit", getContainerEntity().getFullName());
  }

  /**
   * @param portNames array of port names
   */
  protected void changeInputPorts(String... portNames) {
    // add this check as the array manipulation is non-negligible
    if(LOGGER.isTraceEnabled())
      LOGGER.trace("{} - changeInputPorts() - entry - portNames : {}", getContainerEntity().getFullName(), Arrays.toString(portNames));

    Set<String> previousPortNames = new HashSet<String>(inputPortNames);
    inputPortNames.clear();

    // first add new ports
    for (String portName : portNames) {
      Port aPort = (Port) getContainerEntity().getPort(portName);
      if (aPort == null) {
        // create a new one
        try {
          createPort(portName);
        } catch (IllegalActionException e) {
          LOGGER.error("{} - internal error - failed to create port with name {}", getContainerEntity().getFullName(), portName);
        }
      }
      previousPortNames.remove(portName);
      inputPortNames.add(portName);
    }
    // then remove removed ports, based on remaining names in the old port names list
    for (String portName : previousPortNames) {
      try {
        getContainerEntity().getPort(portName).setContainer(null);
      } catch (Exception e) {
        LOGGER.error("{} - internal error - failed to remove port with name {}", getContainerEntity().getFullName(), portName);
      }
    }

    LOGGER.trace("{} - changeInputPorts() - exit", getContainerEntity().getFullName());
  }

  /**
   * @param portName
   * @return
   * @throws IllegalActionException
   */
  protected Port createPort(String portName) throws IllegalActionException {
    LOGGER.trace("{} - createPort() - entry - name : {}", getContainerEntity().getFullName(), portName);
    
    Port aPort = null;
    try {
      aPort = (Port) getContainerEntity().getPort(portName);

      if (aPort == null) {
        LOGGER.debug("{} - createPort() - port {} will be constructed", getContainerEntity().getFullName(), portName);
        aPort = PortFactory.getInstance().createInputPort(getContainerEntity(), portName, PortMode.PUSH, null);
        aPort.setMultiport(true);
      } else {
        throw new IllegalActionException(getContainerEntity(), "port " + portName + " already exists");
      }
    } catch (Exception e) {
      throw new IllegalActionException(this, e, "failed to create port " + portName);
    }
    LOGGER.trace("{} - createPort() - exit - port : {}", getContainerEntity().getFullName(), portName);
    return aPort;
  }
}
