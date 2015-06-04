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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.util.charops.CharOperation;

/**
 * An ErrorHandler actor that can be configured with one or more actor name patterns.
 * <p>
 * When an exception is received, caused/from an actor whose (full) name matches one/some of the configured patterns, 
 * the exception's message context is sent out via the corresponding output port(s).
 * </p>
 * <p>
 * The actor allows 3 matching modes for actor names (or full names) :
 * <ul>
 * <li>regexp strict : applies a strict pattern matching of the configured regular expression on the actor's (full) name</li>
 * <li>regexp contains : checks if the configured regular expression occurs somewhere in the actor's (full) name</li>
 * <li>CamelCase match : applies a CamelCase pattern match on the actor's (full) name, similar as in eclipse's Open Type dialog etc.
 * E.g. you can match <code>ErrorHandlerByCausingActor</code> with a pattern "ErHaBCA" or "EH" etc.
 * </li>
 * </ul>
 * </p>
 * @author erwin
 */
public class ErrorHandlerByCausingActor extends AbstractErrorHandlerActor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(ErrorHandlerByCausingActor.class);

  private SortedSet<NamePatternPair> causingActorNamePatterns = new TreeSet<NamePatternPair>();

  public StringParameter handleCausingActorNamesParam;
  /**
   * Flag to indicate if the error source actor's short name or its full name should be matched
   */
  public Parameter useFullNameParam;//NOSONAR
  private boolean useFullName = false;

  /**
   * Flag to indicate if a strict pattern match is needed, or a "contains" match, i.e. whether the given pattern should match a part of the actor name or
   * completely. Practically speaking, non-strict matching implies pre-&post-fixing the pattern with a ".*".
   */
  public StringParameter matchModeParam;//NOSONAR
  private MatchMode matchMode;

  public ErrorHandlerByCausingActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    handleCausingActorNamesParam = new StringParameter(this, "actor name patterns");
    new TextStyle(handleCausingActorNamesParam, "textbox");

    useFullNameParam = new Parameter(this, "match full name", BooleanToken.FALSE);
    useFullNameParam.setTypeEquals(BaseType.BOOLEAN);
    new CheckBoxStyle(useFullNameParam, "checkbox");

    matchModeParam = new StringParameter(this, "match mode");
    matchModeParam.setExpression(MatchMode.MODE_REGEXP_CONTAINS.getModeDescr());
    matchModeParam.addChoice(MatchMode.MODE_REGEXP_CONTAINS.getModeDescr());
    matchModeParam.addChoice(MatchMode.MODE_REGEXP_STRICT.getModeDescr());
    matchModeParam.addChoice(MatchMode.MODE_CAMEL_CASE_MATCH.getModeDescr());
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void attributeChanged(Attribute attribute) throws IllegalActionException {
    if (handleCausingActorNamesParam == attribute) {
      try {
        List<String> patternNames = new ArrayList<String>();
        causingActorNamePatterns.clear();
        String mappingDefs = handleCausingActorNamesParam.getExpression();
        BufferedReader reader = new BufferedReader(new StringReader(mappingDefs));
        String mappingDef = null;
        while ((mappingDef = reader.readLine()) != null) {
          String[] mappingParts = mappingDef.split("=");
          if (mappingParts.length == 2) {
            String patternName = mappingParts[0];
            patternNames.add(patternName);
            String patternDef = mappingParts[1];
            NamePatternPair cr = NamePatternPair.buildFrom(patternName, patternDef, matchMode);
            if (cr != null) {
              causingActorNamePatterns.add(cr);
            }
          } else {
            getLogger().warn("{} - Invalid mapping definition: {}", getFullName(), mappingDef);
          }
        }
        setOutputPortNames(patternNames.toArray(new String[patternNames.size()]));
      } catch (Exception e) {
        throw new IllegalActionException(this, e, "Error processing actor name pattern mapping");
      }
    } else if (useFullNameParam == attribute) {
      useFullName = ((BooleanToken) useFullNameParam.getToken()).booleanValue();
    } else if (matchModeParam == attribute) {
      MatchMode sm = MatchMode.fromModeDescr(matchModeParam.getExpression());
      if(sm==null) {
        matchModeParam.setExpression(matchMode.getModeDescr());
        throw new IllegalActionException(this, matchModeParam,"Invalid value");
      } else if (!sm.equals(matchMode)) {
        matchMode = sm;
        for (NamePatternPair npp : causingActorNamePatterns) {
          npp.setMatchMode(sm);
        }
      }
    } else {
      super.attributeChanged(attribute);
    }
  }

  /**
   * This handler requires that either the errorSource argument is not-null, or the error argument returns a not-null <code>getModelElement()</code>
   */
  public boolean handleError(Nameable errorSource, PasserelleException error) {
    boolean result = false;
    ManagedMessage msg = error.getMsgContext();
    Nameable errorSourceFromException = error.getModelElement();
    if ((msg != null) && ((errorSource != null) || (errorSourceFromException != null)) && (!causingActorNamePatterns.isEmpty())) {
      String sourceName = useFullName ? (errorSource != null ? errorSource.getFullName() : errorSourceFromException.getFullName())
          : (errorSource != null ? errorSource.getName() : errorSourceFromException.getName());
      for (NamePatternPair nameMatcherPair : causingActorNamePatterns) {
        if (nameMatcherPair.matches(sourceName)) {
          result = sendErrorMsgOnwardsVia(nameMatcherPair.getName(), msg, error);
        }
      }
    }
    return result;
  }

  static class NamePatternPair implements Comparable<NamePatternPair> {
    String name;
    MatchMode matchMode = MatchMode.MODE_REGEXP_CONTAINS;
    // for regexp matching
    boolean strictMatch = false;
    Pattern pattern;
    // for camelcase matching
    boolean camelCaseMatch = false;
    String patternStr;

    NamePatternPair(String name, String pattern, MatchMode matchMode) {
      super();
      this.name = name;
      this.patternStr = pattern;
      this.matchMode = matchMode;
    }

    static NamePatternPair buildFrom(String name, String pattern, MatchMode matchMode) {
      return new NamePatternPair(name, pattern, matchMode);
    }

    public String getName() {
      return name;
    }

    public void setMatchMode(MatchMode matchMode) {
      if (this.matchMode==null || !this.matchMode.equals(matchMode)) {
        this.matchMode = matchMode;
        switch(this.matchMode) {
        case MODE_REGEXP_CONTAINS :
          pattern = Pattern.compile(patternStr);
          strictMatch = false;
          camelCaseMatch = false;
          break;
        case MODE_REGEXP_STRICT :
          pattern = Pattern.compile(patternStr);
          strictMatch = true;
          camelCaseMatch = false;
          break;
        case MODE_CAMEL_CASE_MATCH :
          pattern = null;
          strictMatch = false;
          camelCaseMatch = true;
        }
      }
    }

    public boolean matches(String text) {
      if (matchMode.equals(MatchMode.MODE_CAMEL_CASE_MATCH)) {
        return CharOperation.camelCaseMatch(patternStr.toCharArray(), text.toCharArray());
      } else {
        Matcher m = pattern.matcher(text);
        if (strictMatch) {
          return m.matches();
        } else {
          return m.find();
        }
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
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
      NamePatternPair other = (NamePatternPair) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      return true;
    }

    public int compareTo(NamePatternPair o) {
      if (this == o) {
        return 0;
      }
      if (o == null) {
        return 1;
      }
      return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
      return "NamePatternPair [name=" + name + ", pattern=" + pattern + "]";
    }
  }
  
  static Map<String, MatchMode> byModeDescr = new HashMap<String, MatchMode>();
  
  enum MatchMode {
    MODE_CAMEL_CASE_MATCH("CamelCase match"),
    MODE_REGEXP_STRICT("regexp strict"),
    MODE_REGEXP_CONTAINS("regexp contains");

    String modeDescr;
    
    MatchMode(String modeDescr) {
      this.modeDescr = modeDescr;
      byModeDescr.put(modeDescr, this);
    }
    
    public String getModeDescr() {
      return modeDescr;
    }
    
    public static MatchMode fromModeDescr(String modeDescr) {
      return byModeDescr.get(modeDescr);
    }
  }
}
