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

package com.isencia.passerelle.testsupport.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.TerminationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;

/**
 * @author erwin
 */
@SuppressWarnings("serial")
public class ExceptionGenerator extends Actor {

  private final static Logger LOGGER = LoggerFactory.getLogger(ExceptionGenerator.class);
  
  public Port input;
  public Port output;

  public Parameter runtimeExcParameter;

  public StringParameter errorParameter;
  public ErrorCode errorCode = ErrorCode.ERROR;

  public Parameter preInitExcParameter;
  public Parameter initExcParameter;
  public Parameter preFireExcParameter;
  public Parameter processExcParameter;
  public Parameter postFireExcParameter;
  public Parameter wrapupExcParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ExceptionGenerator(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    runtimeExcParameter = new Parameter(this, "RuntimeException", BooleanToken.FALSE);
    new CheckBoxStyle(runtimeExcParameter, "rte box");

    errorParameter = new StringParameter(this, "error");
    errorParameter.setExpression(getErrorCodeInfo(ErrorCode.ERROR));
    for(ErrorCode errorCode : ErrorCode.values()) {
      errorParameter.addChoice(getErrorCodeInfo(errorCode));
    }

    preInitExcParameter = new Parameter(this, "preInit Exception", BooleanToken.FALSE);
    new CheckBoxStyle(preInitExcParameter, "check box");
    initExcParameter = new Parameter(this, "init Exception", BooleanToken.FALSE);
    new CheckBoxStyle(initExcParameter, "check box");
    preFireExcParameter = new Parameter(this, "preFire Exception", BooleanToken.FALSE);
    new CheckBoxStyle(preFireExcParameter, "check box");
    processExcParameter = new Parameter(this, "process Exception", BooleanToken.FALSE);
    new CheckBoxStyle(processExcParameter, "check box");
    postFireExcParameter = new Parameter(this, "postFire Exception", BooleanToken.FALSE);
    new CheckBoxStyle(postFireExcParameter, "check box");
    wrapupExcParameter = new Parameter(this, "wrapup Exception", BooleanToken.FALSE);
    new CheckBoxStyle(wrapupExcParameter, "check box");
  }
  
  @Override
  public Logger getLogger() {
    return LOGGER;
  }
  
  private static String getErrorCodeInfo(ErrorCode errorCode) {
    return errorCode.name() + ":" + errorCode.getFormattedCode();
  }
  
  private static ErrorCode getErrorCodeFromInfo(String errorCodeInfo) {
    return ErrorCode.valueOf(errorCodeInfo.split(":")[0]);
  }

  @Override
  public void attributeChanged(final Attribute attribute) throws IllegalActionException {
    if (attribute == errorParameter) {
      String errorCodeStr = ((StringToken) errorParameter.getToken()).stringValue();
      try {
        errorCode = getErrorCodeFromInfo(errorCodeStr);
      } catch (Exception e) {
        getLogger().warn("{} Unknown error configured : {} ", getFullName(), errorCodeStr);
        errorCode = ErrorCode.ERROR;
      }
    } else {
      super.attributeChanged(attribute);
    }
  }

  @Override
  protected void doPreInitialize() throws InitializationException {
    boolean mustThrowException=false;
    try {
      mustThrowException = ((BooleanToken) preInitExcParameter.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
    }
    if (mustThrowException) {
      boolean mustThrowRuntimeException = false;
      try {
        mustThrowRuntimeException = ((BooleanToken) runtimeExcParameter.getToken()).booleanValue();
      } catch (IllegalActionException e) {
        throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
      }
      String message = getName()+".doPreInitialize";
      if(mustThrowRuntimeException) {
        throw new RuntimeException(message);
      } else {
        throw new InitializationException(errorCode, message, this, null);
      }
    } else {
      super.doPreInitialize();
    }
  }

  @Override
  protected void doInitialize() throws InitializationException {
    boolean mustThrowException=false;
    try {
      mustThrowException = ((BooleanToken) initExcParameter.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
    }
    if (mustThrowException) {
      boolean mustThrowRuntimeException = false;
      try {
        mustThrowRuntimeException = ((BooleanToken) runtimeExcParameter.getToken()).booleanValue();
      } catch (IllegalActionException e) {
        throw new InitializationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
      }
      String message = getName()+".doInitialize";
      if(mustThrowRuntimeException) {
        throw new RuntimeException(message);
      } else {
        throw new InitializationException(errorCode, message, this, null);
      }
    } else {
      super.doInitialize();
    }
  }

  @Override
  protected boolean doPreFire() throws ProcessingException {
    boolean mustThrowException=false;
    try {
      mustThrowException = ((BooleanToken) preFireExcParameter.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
    }
    if (mustThrowException) {
      boolean mustThrowRuntimeException = false;
      try {
        mustThrowRuntimeException = ((BooleanToken) runtimeExcParameter.getToken()).booleanValue();
      } catch (IllegalActionException e) {
        throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
      }
      String message = getName()+".doPreFire";
      if(mustThrowRuntimeException) {
        throw new RuntimeException(message);
      } else {
        throw new ProcessingException(errorCode, message, this, null);
      }
    } else {
      return super.doPreFire();
    }
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    boolean mustThrowException=false;
    try {
      mustThrowException = ((BooleanToken) processExcParameter.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
    }
    if (mustThrowException) {
      boolean mustThrowRuntimeException = false;
      try {
        mustThrowRuntimeException = ((BooleanToken) runtimeExcParameter.getToken()).booleanValue();
      } catch (IllegalActionException e) {
        throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
      }
      String message = getName()+".process";
      if(mustThrowRuntimeException) {
        throw new RuntimeException(message);
      } else {
        throw new ProcessingException(errorCode, message, this, request.getMessage(input), null);
      }
    } else {
      response.addOutputMessage(output, request.getMessage(input));
    }
  }

  @Override
  protected boolean doPostFire() throws ProcessingException {
    boolean res = super.doPostFire();
    boolean mustThrowException=false;
    try {
      mustThrowException = ((BooleanToken) postFireExcParameter.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
    }
    if (mustThrowException) {
      boolean mustThrowRuntimeException = false;
      try {
        mustThrowRuntimeException = ((BooleanToken) runtimeExcParameter.getToken()).booleanValue();
      } catch (IllegalActionException e) {
        throw new ProcessingException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
      }
      String message = getName()+".doPostFire";
      if(mustThrowRuntimeException) {
        throw new RuntimeException(message);
      } else {
        throw new ProcessingException(errorCode, message, this, null);
      }
    } else {
      return res;
    }
  }

  @Override
  protected void doWrapUp() throws TerminationException {
    boolean mustThrowException=false;
    try {
      mustThrowException = ((BooleanToken) wrapupExcParameter.getToken()).booleanValue();
    } catch (IllegalActionException e) {
      throw new TerminationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
    }
    if (mustThrowException) {
      boolean mustThrowRuntimeException = false;
      try {
        mustThrowRuntimeException = ((BooleanToken) runtimeExcParameter.getToken()).booleanValue();
      } catch (IllegalActionException e) {
        throw new TerminationException(ErrorCode.FLOW_EXECUTION_FATAL, "Error reading parameter", this, e);
      }
      String message = getName()+".doWrapUp";
      if(mustThrowRuntimeException) {
        throw new RuntimeException(message);
      } else {
        throw new TerminationException(errorCode, message, this, null);
      }
    } else {
      super.doWrapUp();
    }
  }
}
