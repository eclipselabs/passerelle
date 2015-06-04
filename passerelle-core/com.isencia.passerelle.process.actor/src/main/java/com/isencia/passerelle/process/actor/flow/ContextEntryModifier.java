/* Copyright 2012 - iSencia Belgium N

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

package com.isencia.passerelle.process.actor.flow;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.ValidationException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * A transformer actor that allows to manipulate Context entries.
 * <p>
 * An entry is identified by a <code>scope</code>, a <code>name</code> and a <code>value</code>. The scope acts as a
 * parent "namespace", within which each specific entry has a unique name. <br/>
 * Technically this is implemented in a basic way by maintaining a separate <code>Map<String,String></code> for each
 * scope and storing each entry as a simple entry in such a map. <br/>
 * An empty scope identifies the "main" level, i.e. directly into the received <code>Context</code>'s entries map.
 * </p>
 * <p>
 * <ul>
 * <li>Set : set a new entry or overwrite an existing with same scope&name</li>
 * <li>Remove : remove an existing entry; do nothing if it is not found. <br/>
 * A remove with only a scope and no entry name will remove the complete scope in one shot.</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("serial")
@Deprecated
public class ContextEntryModifier extends Actor {

  private enum Mode {
    Set, Remove;
  }

  public Port input;
  public Port output;

  public StringParameter entryScopeParameter;
  public StringParameter entryNameParameter;
  public StringParameter entryValueParameter;
  public StringParameter modeParameter;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ContextEntryModifier(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

    entryScopeParameter = new StringParameter(this, "Entry scope");
    entryNameParameter = new StringParameter(this, "Entry name");
    entryValueParameter = new StringParameter(this, "Entry value");
    modeParameter = new StringParameter(this, "Mode");
    modeParameter.addChoice(Mode.Set.name());
    modeParameter.addChoice(Mode.Remove.name());
    modeParameter.setExpression(Mode.Set.name());
  }

  /**
   * An illustration of validating the parameter settings. E.g. for the mode parameter, we expect either Add, Modify or
   * Remove, but this constraint can not be enforced with 100% certainty in model files. So we can check it again here.
   */
  @Override
  protected void validateInitialization() throws ValidationException {
    super.validateInitialization();

    String mode = modeParameter.getExpression();
    try {
      Mode.valueOf(mode);
    } catch (IllegalArgumentException e) {
      throw new ValidationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Invalid mode " + mode, this, null);
    }
  }

  @Override
  public void process(ProcessManager processManager, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = request.getMessage(input);
    if (message != null) {
      try {
        Context diagnosisContext = ProcessRequest.getContextForMessage(processManager, message);
        String entryName = entryNameParameter.getExpression();
        String entryValue = entryValueParameter.getExpression();
        String scopeStr = entryScopeParameter.getExpression();
        String modeStr = modeParameter.getExpression();

        if (scopeStr == null || scopeStr.trim().length() == 0) {
          doItOnMainContext(diagnosisContext, modeStr, entryName, entryValue);
        } else {
          doItOnScope(diagnosisContext, modeStr, scopeStr, entryName, entryValue);
        }
        response.addOutputMessage(output, message);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Failed to modify Context entries", this, message, e);
      }
    }
  }

  /**
   * 
   * @param diagnosisContext
   * @param modeStr
   * @param scopeStr
   * @param entryName
   * @param entryValue
   * @throws IllegalStateException
   *           e.g. when the Context has a conflicting entry for e.g. scope
   * @throws IllegalArgumentException
   *           e.g. when an invalid mode is passed
   */
  @SuppressWarnings("unchecked")
  private void doItOnScope(Context diagnosisContext, String modeStr, String scopeStr, String entryName, String entryValue) throws IllegalStateException, IllegalArgumentException {
    Object scopeObj = diagnosisContext.getEntryValue(scopeStr);
    HashMap<String, String> scopeMap = null;
    if (scopeObj != null) {
      try {
        scopeMap = (HashMap<String, String>) scopeObj;
      } catch (ClassCastException e) {
        throw new IllegalStateException("Context entry scope not a Map for " + scopeStr);
      }
    }
    Mode mode = Mode.valueOf(modeStr);
    switch (mode) {
    case Set:
      if (scopeMap == null) {
        scopeMap = new HashMap<String, String>();
        diagnosisContext.putEntry(scopeStr, scopeMap);
      }
      scopeMap.put(entryName, entryValue);
      break;
    case Remove:
      if (scopeMap != null) {
        if (StringUtils.isEmpty(entryName)) {
          // it's a remove for the complete scope
          diagnosisContext.removeEntry(scopeStr);
        } else {
          // it's a remove for one specific entry
          scopeMap.remove(entryName);
        }
      }
      break;
    }
  }

  private void doItOnMainContext(Context diagnosisContext, String modeStr, String entryName, String entryValue) {
    Mode mode = Mode.valueOf(modeStr);
    switch (mode) {
    case Set:
      diagnosisContext.putEntry(entryName, entryValue);
      break;
    case Remove:
      diagnosisContext.removeEntry(entryName);
      break;
    }
  }

}
