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
package com.isencia.passerelle.testsupport.actor;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;

/**
 * Simple actor that assumes messages contain a Map<String, String> and allows to modify the Map's contents. (cfr HeaderModifier).
 * 
 * @author erwin
 */
public class MapModifier extends Forwarder {

  private static final long serialVersionUID = 1L;
  public Parameter propNameParam = null;
  public Parameter propValueParam = null;
  public Parameter propModeParam = null;

  private String propName = "";
  private String propValue = "";
  private String propMode = null;

  private final static String MODE_ADD = "Add";
  private final static String MODE_MODIFY = "Modify";
  private final static String MODE_REMOVE = "Remove";

  private static Logger LOGGER = LoggerFactory.getLogger(MapModifier.class);

  /**
   * Construct an actor with the given container and name.
   * 
   * @param container
   *          The container.
   * @param name
   *          The name of this actor.
   * @exception IllegalActionException
   *              If the actor cannot be contained by the proposed container.
   * @exception NameDuplicationException
   *              If the container already has an actor with this name.
   */
  public MapModifier(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);

    output.setMultiport(true);

    propNameParam = new StringParameter(this, "header name");
    propNameParam.setExpression("");

    propValueParam = new StringParameter(this, "header value");
    propValueParam.setExpression("");

    propModeParam = new StringParameter(this, "mode");

    propModeParam.addChoice(MODE_ADD);
    propModeParam.addChoice(MODE_MODIFY);
    propModeParam.addChoice(MODE_REMOVE);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    getLogger().trace("{} attributeChanged() - entry : {}", getFullName(), attribute);
    if (attribute == propNameParam) {
      propName = ((StringToken) propNameParam.getToken()).stringValue();
    } else if (attribute == propValueParam) {
      propValue = ((StringToken) propValueParam.getToken()).stringValue();
    } else if (attribute == propModeParam) {
      propMode = propModeParam.getExpression();
    } else {
      super.attributeChanged(attribute);
    }
    getLogger().trace("{} attributeChanged() - exit", getFullName());
  }

  protected void doProcess(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage receivedMsg = request.getMessage(input);
    try {
      ManagedMessage outputMsg = MessageFactory.getInstance().createCausedCopyMessage(receivedMsg);
      if (outputMsg.getBodyContent() instanceof Map<?, ?>) {
        Map<String, String> map = (Map<String, String>) outputMsg.getBodyContent();
        // Create a new outgoing msg, "caused by" the received input msg
        // and for the rest a complete copy of the received msg
        if (propName != null && propName.length() > 0) {
          if (propMode.equalsIgnoreCase(MODE_ADD)) {
            map.put(propName, propValue);
          } else if (propMode.equalsIgnoreCase(MODE_MODIFY)) {
            if (map.containsKey(propName)) {
              map.put(propName, propValue);
            }
          } else if (propMode.equalsIgnoreCase(MODE_REMOVE)) {
            map.remove(propName);
          }
        }
      }
      response.addOutputMessage(output, outputMsg);
    } catch (MessageException e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error constructing copy from received message from value parameter", 
          this, receivedMsg, e);
    }
  }
}