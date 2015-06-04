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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * An ErrorHandler actor that can be configured with one or more numerical error code ranges.
 * <p>
 * When an exception is received that has an error code in one/some of the configured ranges, 
 * the exception's message context is sent out via the corresponding output port(s).
 * </p>
 * 
 * @author erwin
 */
public class ErrorHandlerByCodeRange extends AbstractErrorHandlerActor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerByCodeRange.class);

  /**
   * multi-line definition of error ranges for which this handler will take ownership. <br/>
   * Each line is of format <code>name=range</code>, where for each line :
   * <ul>
   * <li>name will be used to create a corresponding output port</li>
   * <li>range is of format <code>nnnn[-mmmm]</code>, i.e. either one 4-digit number or two of them separated by a mid-score</li>
   * </ul>
   */
  public StringParameter errorRangesParameter;
  private Set<CodeRange> handledErrorRanges = new TreeSet<CodeRange>();

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public ErrorHandlerByCodeRange(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    errorRangesParameter = new StringParameter(this, "error ranges");
    new TextStyle(errorRangesParameter, "textbox");
  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (attribute != errorRangesParameter) {
      super.attributeChanged(attribute);
    } else {
      try {
        List<String> rangeNames = new ArrayList<String>();
        handledErrorRanges.clear();
        String mappingDefs = errorRangesParameter.getExpression();
        BufferedReader reader = new BufferedReader(new StringReader(mappingDefs));
        String mappingDef = null;
        while ((mappingDef = reader.readLine()) != null) {
          String[] mappingParts = mappingDef.split("=");
          if (mappingParts.length == 2) {
            String rangeName = mappingParts[0];
            rangeNames.add(rangeName);
            String rangeDef = mappingParts[1];
            CodeRange cr = CodeRange.buildFrom(rangeName, rangeDef);
            if (cr != null) {
              handledErrorRanges.add(cr);
            }
          } else {
            getLogger().warn("{} - Invalid mapping definition: {}", getFullName(), mappingDef);
          }
        }
        setOutputPortNames(rangeNames.toArray(new String[rangeNames.size()]));
      } catch (Exception e) {
        throw new IllegalActionException(this, e, "Error processing error range mapping");
      }
    }
  }

  /**
   * Checks if the given error contains a msg and an error code. If the code matches one/some of the configured ranges, the msg will be sent out via the
   * corresponding port(s).
   */
  public synchronized boolean handleError(Nameable errorSource, PasserelleException error) {
    boolean result = false;
    ManagedMessage msg = error.getMsgContext();
    ErrorCode errCode = error.getErrorCode();
    if ((msg != null) && (errCode != null)) {
      for (CodeRange codeRange : handledErrorRanges) {
        if (codeRange.isInRange(errCode.getCodeAsInteger())) {
          result = sendErrorMsgOnwardsVia(codeRange.getName(), msg, error);
        }
      }
    }
    return result;
  }

  public Logger getLogger() {
    return LOGGER;
  }

  private static class CodeRange implements Comparable<CodeRange> {
    int minValue = 0;
    int maxValue = Integer.MAX_VALUE;
    String name;

    public CodeRange(String name, int minValue) {
      this.name = name;
      this.minValue = minValue;
      this.maxValue = minValue;
    }

    public CodeRange(String name, int minValue, int maxValue) {
      this.name = name;
      this.minValue = minValue;
      this.maxValue = maxValue;
    }

    public static CodeRange buildFrom(String name, String rangeDef) {
      CodeRange result = null;
      String[] rangeLimits = rangeDef.split("-");
      if (rangeLimits.length == 2) {
        result = new CodeRange(name, Integer.parseInt(rangeLimits[0]), Integer.parseInt(rangeLimits[1]));
      } else if (rangeLimits.length == 1) {
        result = new CodeRange(name, Integer.parseInt(rangeLimits[0]));
      }
      return result;
    }

    public String getName() {
      return name;
    }

    public boolean isInRange(int i) {
      return minValue <= i && maxValue >= i;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + maxValue;
      result = prime * result + minValue;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      CodeRange other = (CodeRange) obj;
      if (maxValue != other.maxValue)
        return false;
      if (minValue != other.minValue)
        return false;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }

    public int compareTo(CodeRange o) {
      if (this == o) {
        return 0;
      }
      if (o == null) {
        return 1;
      }
      return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
      return "CodeRange [minValue=" + minValue + ", maxValue=" + maxValue + ", name=" + name + "]";
    }
  }
}
