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

package com.isencia.passerelle.actor;

import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;

/**
 * @author erwin
 *
 */
@SuppressWarnings("serial")
public class InitializationValidator extends Actor {
  
  public Parameter mustValidateWithErrorParameter;
  public StringParameter validationErrorMessageParameter;
  
  public Port input;
  public Port output;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public InitializationValidator(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);
    
    mustValidateWithErrorParameter = new Parameter(this,"Must generate validation error", new BooleanToken(false));
    new CheckBoxStyle(mustValidateWithErrorParameter, "valerrstyle");
    registerConfigurableParameter(mustValidateWithErrorParameter);
    
    validationErrorMessageParameter = new StringParameter(this, "Validation error message");
    validationErrorMessageParameter.setExpression("some validation error");
    registerConfigurableParameter(validationErrorMessageParameter);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    // just fwd received msgs to the output port
    response.addOutputMessage(output, request.getMessage(input));
  }
  
  @Override
  protected void validateInitialization() throws ValidationException {
    super.validateInitialization();
    
    try {
      boolean mustValidateWithError = ((BooleanToken)mustValidateWithErrorParameter.getToken()).booleanValue();
      if(mustValidateWithError) {
        String validationErrorMsg = ((StringToken)validationErrorMessageParameter.getToken()).stringValue();
        throw new ValidationException(ErrorCode.FLOW_CONFIGURATION_ERROR, validationErrorMsg, this, null);
      }
    } catch (IllegalActionException e) {
      throw new ValidationException(ErrorCode.ERROR, "Error reading parameter", this, e);
    }
  }

}
