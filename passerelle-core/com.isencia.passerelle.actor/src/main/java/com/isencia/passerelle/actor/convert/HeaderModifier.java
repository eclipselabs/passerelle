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
package com.isencia.passerelle.actor.convert;

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
import com.isencia.passerelle.actor.Transformer;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;

/**
 * Simple actor that reads std passerelle msgs and allows to add/modify a property name/value pair.
 * 
 * @author erwin
 */
public class HeaderModifier extends Transformer {

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
  private final static String MODE_REPLICATE = "Replicate";

  private static Logger LOGGER = LoggerFactory.getLogger(HeaderModifier.class);

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
  public HeaderModifier(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
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
    propModeParam.addChoice(MODE_REPLICATE);
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

  public void doFire(ManagedMessage message) throws ProcessingException {
    if (message != null) {
      if (propName != null && propName.length() > 0) {
        try {
          if (propMode.equalsIgnoreCase(MODE_ADD)) {
            message.addBodyHeader(propName, propValue);
          } else if (propMode.equalsIgnoreCase(MODE_MODIFY)) {
            message.setBodyHeader(propName, propValue);
          } else if (propMode.equalsIgnoreCase(MODE_REMOVE)) {
            message.removeBodyHeader(propName);
          } else if (propMode.equalsIgnoreCase(MODE_REPLICATE)) {
            String[] originalHeaders = message.getBodyHeader(propValue);
            if (originalHeaders != null) {
              for (String originalHeader : originalHeaders) {
                message.addBodyHeader(propName, originalHeader);
              }
            }
          }
        } catch (MessageException e) {
          throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "", this, message, e);
        }
      }
    }
    sendOutputMsg(output, message);
  }
}