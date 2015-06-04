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
import java.util.List;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.core.PortMode;
import com.isencia.passerelle.message.MessageInputContext;

/**
 * Remark : for these kinds of actors, it is not allowed to modify the names of the dynamically generated ports.
 * Otherwise the lookup of the ports can fail...
 * 
 * @author Erwin
 */
public abstract class DynamicPortsActor extends Actor {

  private static final long serialVersionUID = 1L;

  static protected enum PortType {
    INPUT, OUTPUT;
  }

  public static final String NUMBER_OF_INPUTS = "Number of inputs";
  public static final String NUMBER_OF_OUTPUTS = "Number of outputs";
  public static final String INPUTPORTPREFIX = "input";
  public static final String OUTPUTPORTPREFIX = "output";

  public Parameter numberOfInputs = null;
  protected int nrInputPorts = 0;

  public Parameter numberOfOutputs = null;
  protected int nrOutputPorts = 0;

  /**
   * Construct an actor in the specified container with the specified name.
   * 
   * @param container
   *          The container.
   * @param name
   *          The name of this actor within the container.
   * @exception IllegalActionException
   *              If the actor cannot be contained by the proposed container.
   * @exception NameDuplicationException
   *              If the name coincides with an actor already in the container.
   */
  public DynamicPortsActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    numberOfInputs = new Parameter(this, NUMBER_OF_INPUTS, new IntToken(0));
    numberOfInputs.setTypeEquals(BaseType.INT);
    numberOfOutputs = new Parameter(this, NUMBER_OF_OUTPUTS, new IntToken(0));
    numberOfOutputs.setTypeEquals(BaseType.INT);
  }

  /**
   * @param attribute
   *          The attribute that changed.
   * @exception IllegalActionException
   */
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == numberOfOutputs) {
      int newPortCount = ((IntToken) numberOfOutputs.getToken()).intValue();
      if (newPortCount != nrOutputPorts) {
        changeNumberOfPorts(newPortCount, nrOutputPorts, PortType.OUTPUT);
        nrOutputPorts = newPortCount;
      }
    } else if (attribute == numberOfInputs) {
      int newPortCount = ((IntToken) numberOfInputs.getToken()).intValue();
      if (newPortCount != nrInputPorts) {
        changeNumberOfPorts(newPortCount, nrInputPorts, PortType.INPUT);
        nrInputPorts = newPortCount;
      }
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  /**
   * @return Returns the inputPorts.
   */
  @SuppressWarnings("unchecked")
  public List<Port> getInputPorts() {
    // in order to avoid cloning issues
    // when we would maintain the list of dynamically cfg-ed
    // input ports in an instance variable,
    // we build this list dynamically here from
    // Ptolemy's internal port list
    List<Port> inpPorts = new ArrayList<Port>();
    List<Port> inputPortList = inputPortList();
    for (Port inP : inputPortList) {
      if (inP.getName().startsWith(INPUTPORTPREFIX, 0)) {
        inpPorts.add(inP);
      }
    }
    return inpPorts;
  }

  /**
   * @return Returns the nrInputPorts.
   */
  public int getNrInputPorts() {
    return nrInputPorts;
  }

  /**
   * @return Returns the nrOutputPorts.
   */
  public int getNrOutputPorts() {
    return nrOutputPorts;
  }

  /**
   * @return Returns the outputPorts.
   */
  @SuppressWarnings("unchecked")
  public List<Port> getOutputPorts() {
    // in order to avoid cloning issues
    // when we would maintain the list of dynamically cfg-ed
    // output ports in an instance variable,
    // we build this list dynamically here from
    // Ptolemy's internal port list
    List<Port> outPorts = new ArrayList<Port>();
    List<Port> outputPortList = outputPortList();
    for (Port outP : outputPortList) {
      if (outP.getName().startsWith(OUTPUTPORTPREFIX, 0)) {
        outPorts.add(outP);
      }
    }
    return outPorts;
  }

  /**
   * @param newPortCount
   *          The amount of ports needed.
   * @param currPortCount
   *          The current nr of ports of the requested type
   * @param portType
   *          PortType.INPUT or PortType.OUTPUT, this parameter is used to set default values for a port and to choose a
   *          default name.
   * @throws IllegalActionException
   * @throws IllegalArgumentException
   */
  protected void changeNumberOfPorts(int newPortCount, int currPortCount, PortType portType) throws IllegalActionException, IllegalArgumentException {
    if (getLogger().isTraceEnabled()) {
      getLogger().trace("{} changeNumberOfPorts() - entry - portType : {} / new nrOfPorts : {}", new Object[] { getFullName(), portType, newPortCount });
    }
    // Set port to input or output
    // Remark: input is never multiport, output is always multiport
    boolean isInput = false, isOutput = true;
    String namePrefix;
    if (portType == PortType.INPUT) {
      isInput = true;
      isOutput = false;
      namePrefix = INPUTPORTPREFIX;
    } else if (portType == PortType.OUTPUT) {
      isInput = false;
      isOutput = true;
      namePrefix = OUTPUTPORTPREFIX;
    } else {
      throw new IllegalArgumentException("Unknown PortType: " + portType);
    }

    // if we want lesser ports, remove some
    if (newPortCount < currPortCount) {
      for (int i = currPortCount - 1; (i >= 0) && (i >= newPortCount); i--) {
        String portName = namePrefix + i;
        // remove the port
        try {
          this.getPort(portName).setContainer(null);
        } catch (Exception e) {
          throw new IllegalActionException(this, e, "failed to remove port " + portName);
        }
      }
    }
    // if we want more ports, reuse old ones + add new ones
    else if (newPortCount > currPortCount) {
      for (int i = currPortCount; i < newPortCount; i++) {
        createPort(namePrefix, i, isInput, isOutput);
      }
    }
    getLogger().trace("{} changeNumberOfPorts() - exit", getFullName());
  }

  /**
   * @param name
   * @param isInput
   * @param isOutput
   * @param typeOfData
   * @return
   * @throws IllegalActionException
   */
  protected Port createPort(String namePrefix, int index, boolean isInput, boolean isOutput) throws IllegalActionException {
    String portName = namePrefix + index;
    getLogger().trace("{} createPort() - entry - portName : {} ", getFullName(), portName);
    
    Port aPort = null;
    try {
      aPort = (Port) getPort(portName);

      if (aPort == null) {
        if (isInput) {
          aPort = PortFactory.getInstance().createInputPort(this, portName, getPortModeForNewInputPort(portName), null);
        } else {
          aPort = PortFactory.getInstance().createOutputPort(this, portName);
        }
        aPort.setMultiport(!isInput);
      } else {
        getLogger().debug("{} createPort() - port {} already exists", getFullName(), portName);
        // ensure it has the right characteristics
        aPort.setInput(isInput);
        aPort.setOutput(isOutput);
        aPort.setMode(getPortModeForNewInputPort(portName));
      }
    } catch (Exception e) {
      throw new IllegalActionException(this, e, "failed to create port " + portName);
    }
    getLogger().trace("{} createPort() - exit - port : {}", getFullName(), aPort);
    return aPort;
  }

  /**
   * Overridable method to allow subclasses to differentiate the PortMode for new input ports.
   * 
   * @param portName
   * @return
   */
  protected PortMode getPortModeForNewInputPort(String portName) {
    return PortMode.PUSH;
  }

  protected int getPortIndex(final MessageInputContext messageInputContext) {
    String portName = messageInputContext.getPortName();
    String portIndexStr = portName.substring("input".length());
    int portIndex = Integer.parseInt(portIndexStr);
    return portIndex;
  }
}