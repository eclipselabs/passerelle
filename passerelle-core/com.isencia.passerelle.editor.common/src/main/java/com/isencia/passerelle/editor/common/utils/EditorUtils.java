package com.isencia.passerelle.editor.common.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import ptolemy.vergil.kernel.attributes.TextAttribute;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.editor.common.model.LinkImpl;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.CollectingMomlParsingErrorHandler;
import com.isencia.passerelle.model.util.CollectingMomlParsingErrorHandler.ErrorItem;
import com.isencia.passerelle.model.util.MoMLParser;

public class EditorUtils {

  public static final String DEFAULT_OUTPUT_PORT = "OutputPort";
  public static final String DEFAULT_INPUT_PORT = "InputPort";
  public static final String CLIP_BOARD = "clipBoard";
  public static final String CHARSET = "UTF-8";
  public static Logger logger = LoggerFactory.getLogger(EditorUtils.class);;

  public static Class<?> loadClass(String className) {
    try {
      return MoMLParser.getClassLoadingStrategy().loadJavaClass(className, null);
    } catch (ClassNotFoundException e3) {
      logger.info(className + " class not found");
      return null;
    }
  }

  public static Date formatDate(String date, String pattern) {
    if (date == null || pattern == null) {
      return null;
    }
    DateFormat df = createDateFormat(pattern);
    try {
      return df.parse(date);
    } catch (ParseException e) {
    }
    return null;
  }

  public static String format(Date date, String pattern) {
    if (date == null || pattern == null) {
      return "";
    }
    DateFormat df = createDateFormat(pattern);
    return df.format(date);
  }

  public static DateFormat createDateFormat(String pattern) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    // TimeZone gmt = TimeZone.getTimeZone("GMT");
    // sdf.setTimeZone(gmt);
    sdf.setLenient(true);
    return sdf;
  }

  public static String determineAlmostFullName(String fullName) {
    if (fullName != null) {
      int endOfModelName = fullName.indexOf(".", 1) + 1;
      return fullName.substring(endOfModelName);
    }
    return null;
  }

  public static String getStackTrace(Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    String stacktrace = sw.toString();
    return stacktrace;

  }

  public static CompositeEntity parseModel(byte[] data) throws Exception {
    CollectingMomlParsingErrorHandler errorHandler = new CollectingMomlParsingErrorHandler();
    CompositeEntity compositeEntity = (CompositeEntity) readMomlWithErrorHandling(data, errorHandler);

    if (errorHandler.hasErrors()) {
      Iterator itr = errorHandler.iterator();
      StringBuilder errorMsgBldr = new StringBuilder("Error parsing " + data);
      while (itr.hasNext()) {
        ErrorItem errorItem = (ErrorItem) itr.next();
        errorMsgBldr.append(errorItem.context.getName() + ":" + errorItem.exception.getMessage());
      }
    }
    return compositeEntity;
  }

  public static NamedObj readMomlWithErrorHandling(byte[] bytes, CollectingMomlParsingErrorHandler errorHandler) throws Exception {
    MoMLParser moMLParser = new MoMLParser();
    if (errorHandler != null)
      MoMLParser.setErrorHandler(errorHandler);
    return moMLParser.parse(new String(bytes,CHARSET));

  }

  public static byte[] writeMoml(CompositeEntity entity) throws Exception {
    if (entity != null) {
      String exportMoML = entity.exportMoML();
      if (exportMoML != null)
        return exportMoML.getBytes();
    }
    return null;
  }

  public static final String SEPARATOR = "sep";

  public static String generateUniqueID(String... keys) {
    StringBuffer sb = new StringBuffer();
    for (String key : keys) {
      sb.append(key);
    }
    sb.append(SEPARATOR);
    sb.append(System.currentTimeMillis());
    return sb.toString();
  }

  public static String getKey(String code) {
    if (code.contains(SEPARATOR)) {
      return code.split(SEPARATOR)[0];
    }
    return null;
  }

  public static void setContainer(NamedObj child, NamedObj container) {
    try {

      if (child instanceof ComponentEntity) {

        ((ComponentEntity) child).setContainer((CompositeEntity) container);
      } else if (child instanceof Director) {
        ((Director) child).setContainer((CompositeEntity) container);
      } else if (child instanceof Vertex) {
        ((TypedIORelation) ((Vertex) child).getContainer()).setContainer((CompositeEntity) container);
      } else if (child instanceof TextAttribute) {
        ((TextAttribute) child).setContainer((CompositeEntity) container);
      } else if (child instanceof Variable) {
        ((Variable) child).setContainer((CompositeEntity) container);
      } else if (child instanceof TypedIOPort) {
        ((TypedIOPort) child).setContainer((CompositeEntity) container);
      }
    } catch (IllegalActionException e) {
      e.printStackTrace();
    } catch (NameDuplicationException e) {
      e.printStackTrace();
    }
  }

  public static double[] getLocation(NamedObj namedObject) {
    if (namedObject instanceof Locatable) {
      Locatable locationAttribute = (Locatable) namedObject;
      return locationAttribute.getLocation();
    }
    List<Attribute> attributes = namedObject.attributeList(Locatable.class);
    if (attributes == null || attributes.size() == 0) {
      return new double[] { 0.0D, 0.0D };
    }
    Locatable locationAttribute = (Locatable) attributes.get(0);
    return locationAttribute.getLocation();
  }

  public static Locatable getLocatable(NamedObj namedObject) {
    if (namedObject instanceof Locatable) {
      return (Locatable) namedObject;
    }
    List<Attribute> attributes = namedObject.attributeList(Locatable.class);
    if (attributes == null || attributes.size() == 0) {
      return null;
    }
    return (Locatable) attributes.get(0);
  }

  public static String findUniqueName(CompositeEntity parentModel, Class clazz, String startName, String actorName) {
    if (clazz == null) {
      return findUniqueActorName(parentModel, actorName);

    }
    if (clazz.getSimpleName().equals("Vertex")) {
      return generateUniqueVertexName(clazz.getSimpleName(), parentModel, 0, clazz);
    } else if (clazz.getSimpleName().equals("TextAttribute")) {
      return generateUniqueTextAttributeName(clazz.getSimpleName(), parentModel, 0, clazz);
    } else if (clazz.getSimpleName().equals("TypedIOPort")) {
      return generateUniquePortName(startName, parentModel, 0);
    } else {
      return findUniqueActorName(parentModel, actorName != null ? actorName : clazz.getSimpleName());
    }
  }

  private static String generateUniquePortName(String name, CompositeEntity parent, int index) {
    String newName = index != 0 ? (name + "_" + index) : name;
    boolean contains = false;
    Enumeration ports = parent.getPorts();
    while (ports.hasMoreElements()) {
      String portName = ((Port) ports.nextElement()).getName();
      if (newName.equals(portName)) {
        contains = true;
        break;
      }

    }
    if (!contains) {
      return newName;
    }
    index++;
    return generateUniquePortName(name, parent, index);

  }

  public static String findUniqueActorName(CompositeEntity parentModel, String name) {
    String newName = name;
    if (parentModel == null)
      return newName;
    List entityList = parentModel.entityList();
    if (entityList == null || entityList.size() == 0)
      return newName;

    ComponentEntity entity = parentModel.getEntity(newName);
    int i = 1;
    while (entity != null) {
      newName = name + "_" + i++;
      entity = parentModel.getEntity(newName);
    }

    return newName;
  }

  private static String generateUniqueTextAttributeName(String name, NamedObj parent, int index, Class clazz) {
    try {
      String newName = index != 0 ? (name + "_" + index) : name;
      if (parent.getAttribute(newName, clazz) == null) {
        return newName;
      } else {
        index++;
        return generateUniqueTextAttributeName(name, parent, index, clazz);
      }
    } catch (IllegalActionException e) {
      return name;
    }

  }

  private static String generateUniqueVertexName(String name, NamedObj parent, int index, Class clazz) {

    return "Vertex" + System.currentTimeMillis();

  }

  public static String getLegalName(final String name) {
    String newName = name;
    // newName = newName.replace(' ', '_');
    if (name.indexOf('.') > -1) {
      newName = newName.substring(0, newName.lastIndexOf('.'));
    }
    newName = newName.replace('.', '_');
    return newName;
  }

  @SuppressWarnings("unchecked")
  public static void setLocation(NamedObj model, double[] location) {
    if (model instanceof Locatable) {
      try {
        ((Locatable) model).setLocation(location);
        NamedObj cont = model.getContainer();
        cont.attributeChanged((Attribute) model);
      } catch (IllegalActionException e) {
      }

    }
    List<Attribute> attributes = model.attributeList(Locatable.class);
    if (attributes == null)
      return;
    if (attributes.size() > 0) {
      Locatable locationAttribute = (Locatable) attributes.get(0);
      try {
        locationAttribute.setLocation(location);
        model.attributeChanged(attributes.get(0));
      } catch (IllegalActionException e) {
      }
    } else {
      try {
        new Location(model, "_location").setLocation(location);
      } catch (IllegalActionException e) {
      } catch (NameDuplicationException e) {
      }
    }
  }

  public static Flow initFlow(byte[] input, CollectingMomlParsingErrorHandler errorHandler) throws PasserelleException {
    InputStream in = new ByteArrayInputStream(input);
    Reader reader = new InputStreamReader(in);

    try {
      if (errorHandler != null)
        MoMLParser.setErrorHandler(errorHandler);
      Flow flow = FlowManager.readMoml(reader);
      if (errorHandler != null)
        MoMLParser.setErrorHandler(null);
      return flow;
    } catch (Exception e) {
      throw (new PasserelleException(ErrorCode.ERROR, "Error parsing model", e));
    }
  }

  public static Flow initFlow(byte[] input) throws PasserelleException {
    return initFlow(input, null);

  }

  public static Link generateLink(ComponentRelation relation, Object source, Object target) {
    Link link = new LinkImpl();
    link.setHead(source);
    link.setTail(target);
    link.setRelation(relation);
    return link;
  }
}
