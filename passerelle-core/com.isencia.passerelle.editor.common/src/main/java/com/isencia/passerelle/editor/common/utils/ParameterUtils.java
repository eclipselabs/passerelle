package com.isencia.passerelle.editor.common.utils;

import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

import com.isencia.passerelle.actor.dynaport.DynamicNamedOutputPortsActor;
import com.isencia.passerelle.util.ptolemy.DateTimeParameter;
import com.isencia.passerelle.util.ptolemy.ParameterGroup;

public class ParameterUtils {

  public static final String MULTI_CHECKBOX = "multiCheckbox";
  public static final String COMBO = "combo";
  public static final String CHECKBOX = "checkbox";
  public static final String TEXT_AREA = "TextArea";
  public static final String GROUP = "Group";
  public static final String TABLE = "table";
  public static final String RENDER_CANVAS = "renderCanvas";
  public static final String RELOAD_PARAMETERS = "reloadParameters";
  public static final String CHANGE_ATTRIBUTES = "changeAttributes";

  private static boolean isValidCharacter(String substring) {
    String trim = substring.trim();
    if (StringUtils.isEmpty(trim)) {
      return true;
    }
    return false;
  }

  public static Class getType(Parameter parameter) {
    // Boolean
    if (parameter instanceof Variable && (((Variable) parameter).getDeclaredType() == BaseType.BOOLEAN || isCheckbox(parameter))) {
      return Boolean.class;
    } else if (parameter instanceof FileParameter) {
      return File.class;
    } else if (parameter instanceof DateTimeParameter) {
      return Date.class;
    } else
      return String.class;
  }

  public static ParameterGroup getConfigParameter(NamedObj namedObj) {
    for (Object attr : namedObj.attributeList()) {
      if (attr instanceof ParameterGroup) {
        ParameterGroup group = (ParameterGroup) attr;
        if (isTable(group)) {
          return group;
        }
      }

    }
    return null;
  }

  public static Parameter getParameter(NamedObj no, String name) {
    Parameter parameter = null;
    try {
      return (Parameter) no.getAttribute(name, Parameter.class);
    } catch (IllegalActionException e) {

    }
    return null;

  }

  public static String getParameterValue(NamedObj no, String name) {
    Parameter parameter = getParameter(no, name);
    if (parameter != null)
      return parameter.getExpression();
    return null;
  }

  public static Object getValue(Parameter parameter) {
    // Boolean
    if (parameter instanceof Variable && ((Variable) parameter).getDeclaredType() == BaseType.BOOLEAN) {
      try {
        Token current = ((Variable) parameter).getToken();
        if (parameter.getExpression().equals("true") || parameter.getExpression().equals("false")) {
          return ((BooleanToken) current).booleanValue();
        }
      } catch (IllegalActionException e) {
      }
      return false;
    } else if (parameter instanceof DateTimeParameter) {
      return ((DateTimeParameter) parameter).getDateValue();
    } else
      return parameter.getExpression();
  }

  public static void setValue(Parameter parameter, Object value) {
    // Boolean
    if (parameter == null) {
      return;
    }
    if (value == null) {
      parameter.setExpression("");
      return;
    }
    if (parameter instanceof Variable && ((Variable) parameter).getDeclaredType() == BaseType.BOOLEAN) {

      parameter.setExpression(value.toString());
      // Date
    } else if (parameter instanceof DateTimeParameter) {
      ((DateTimeParameter) parameter).setDateValue((Date) value);
      // Other
    } else
      parameter.setExpression(value.toString());

    try {
      parameter.propagateValue();
      parameter.propagateValues();
    } catch (IllegalActionException e) {
    }
  }

  public static String generateActorName(String name, NamedObj entity) {
    if (entity.getContainer() == null) {
      return name;
    }
    if (entity.getContainer().getContainer() != null)
      return generateActorName(entity.getContainer().getName() + "." + name, entity.getContainer());
    else
      return generateActorName(name, entity.getContainer());
  }

  public static boolean shouldRender(ptolemy.data.expr.Parameter parameter) {
    if (parameter.getContainer() instanceof DynamicNamedOutputPortsActor) {
      return true;
    }
    return parameter.getAttribute(RENDER_CANVAS) != null;
  }

  public static boolean shouldLoadParameters(ptolemy.data.expr.Parameter parameter) {
    return parameter.getAttribute(RELOAD_PARAMETERS) != null;
  }

  public static boolean shouldchangeAttributes(ptolemy.data.expr.Parameter parameter) {
    return parameter.getAttribute(CHANGE_ATTRIBUTES) != null;
  }

  public static boolean isCombo(ptolemy.data.expr.Parameter parameter) {
    return parameter.getAttribute(COMBO) != null || (parameter.getContainer() instanceof ParameterGroup && ParameterUtils.isTable((ParameterGroup) parameter.getContainer()));
  }

  public static boolean isCheckbox(ptolemy.data.expr.Parameter parameter) {
    return parameter.getAttribute(CHECKBOX) != null;
  }

  public static boolean isTextArea(ptolemy.data.expr.Parameter parameter) {
    return parameter.getAttribute(TEXT_AREA) != null || parameter.getAttribute("paramsTextArea") != null;
  }

  public static boolean isGroup(ptolemy.data.expr.Parameter parameter) {
    return parameter instanceof ParameterGroup;
  }

  public static boolean isTable(ptolemy.data.expr.Parameter parameter) {
    return parameter.getAttribute(TABLE) != null;
  }

  public static boolean isMultiCheckbox(ptolemy.data.expr.Parameter parameter) {
    return parameter.getAttribute(MULTI_CHECKBOX) != null;
  }

  public static String[] getChoises(ptolemy.data.expr.Parameter parameter) {
    String target = "target=" + parameter.getName();
    NamedObj no = parameter.getContainer();
    if (no instanceof com.isencia.passerelle.actor.Actor) {
      com.isencia.passerelle.actor.Actor actor = (com.isencia.passerelle.actor.Actor) no;
      Enumeration attributes = actor.getAttributes();
      for (Object o : actor.attributeList()) {
        if (o instanceof ptolemy.data.expr.Parameter && ((ptolemy.data.expr.Parameter) o).getAttribute(target) != null) {
          String expression = ((ptolemy.data.expr.Parameter) o).getExpression();
          if (StringUtils.isNotBlank(expression))
            return expression.split(",");
        }
      }
    }
    if (no instanceof ParameterGroup) {
      ParameterGroup pg = (ParameterGroup) no;
      if (ParameterUtils.isTable(pg)) {
        return pg.getChoices();
      }
    }
    return null;
  }

  public static boolean isVisible(NamedObj target, Settable settable, boolean expert) {

    if (settable.getVisibility() == Settable.FULL || settable.getVisibility() == Settable.NOT_EDITABLE) {
      return true;
    }
    if ((settable.getVisibility() == Settable.EXPERT) && (target instanceof com.isencia.passerelle.actor.Actor)) {
      return expert;
    }
    return false;
  }

  private static void initializeOptions(NamedObj entity) {
    if (entity instanceof com.isencia.passerelle.actor.Actor) {
      configureParameters((com.isencia.passerelle.actor.Actor) entity);
    }
  }

  private static void configureParameters(com.isencia.passerelle.actor.Actor actor) {
    if (actor.getOptionsFactory() != null) {
      List parameters = actor.attributeList(Parameter.class);
      for (Iterator iter = parameters.iterator(); iter.hasNext();) {
        Parameter p = (Parameter) iter.next();
        actor.getOptionsFactory().setOptionsForParameter(p);
      }
    }
  }

  public static boolean hasOptions(Attribute parameter) {
    boolean b = parameter instanceof Parameter && ((Parameter) parameter).getChoices() != null && ((Parameter) parameter).getChoices().length > 0;
    if (b)
      return true;

    return false;
  }

}
