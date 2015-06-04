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
package com.isencia.passerelle.actor.error;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.ErrorCode.Severity;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * An ErrorHandler actor that can be configured with one or more accepted error severities.
 * <p>
 * When an exception is received that has one of the configured severities,
 * its message context is sent out via the corresponding output port.
 * </p>
 * @author erwin
 *
 */
public class ErrorHandlerBySeverity extends AbstractErrorHandlerActor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerBySeverity.class);

  private SortedSet<String> handledSeverities = new TreeSet<String>();
  
  public Set<Parameter> handleSeverityParams = new HashSet<Parameter>();

  public ErrorHandlerBySeverity(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    for(Severity severity : Severity.values()) {
      Parameter p = new Parameter(this, severity.name(), BooleanToken.FALSE);
      p.setTypeEquals(BaseType.BOOLEAN);
      new CheckBoxStyle(p, severity.name()+"box");
      handleSeverityParams.add(p);
    }
  }
  
  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (!handleSeverityParams.contains(attribute)) {
      super.attributeChanged(attribute);
    } else {
      BooleanToken handleSeverityToken = (BooleanToken) ((Parameter)attribute).getToken();
      if(BooleanToken.FALSE.equals(handleSeverityToken)) {
        handledSeverities.remove(attribute.getName());
      } else if(BooleanToken.TRUE.equals(handleSeverityToken)) {
        handledSeverities.add(attribute.getName());
      }
      List<String> portNames = new ArrayList<String>(handledSeverities);
      setOutputPortNames(portNames.toArray(new String[portNames.size()]));
    }
  }
  
  public boolean handleError(Nameable errorSource, PasserelleException error) {
    boolean result = false;
    ManagedMessage msg = error.getMsgContext();
    ErrorCode errCode = error.getErrorCode();
    if ((msg != null) && (errCode != null) && (errCode.getSeverity() != null)) {
      Severity severity = errCode.getSeverity();
      if(handledSeverities.contains(severity.name())) {
        result = sendErrorMsgOnwardsVia(severity.name(), msg, error);
      }
    }
    return result;
  }
}
