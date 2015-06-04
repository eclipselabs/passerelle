/**
 * 
 */
package com.isencia.passerelle.process.actor.trial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.factory.ProcessFactoryTracker;
import com.isencia.passerelle.util.ExecutionTracerService;

/**
 * 
 * @author heukoe
 * 
 */
public class EventFromFileActor extends Actor {

  private static final long serialVersionUID = 1L;

  public Port input; // NOSONAR
  public Port output; // NOSONAR

  private static final Logger LOGGER = LoggerFactory.getLogger(EventFromFileActor.class);
  private StringParameter rootFolder;
  private StringParameter number;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public EventFromFileActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    rootFolder = new StringParameter(this, "Root Folder");
    number = new StringParameter(this, "Doubles");
    input = PortFactory.getInstance().createInputPort(this, null);
    output = PortFactory.getInstance().createOutputPort(this);

  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();

  }

  private static final class PropertiesFileFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return name != null && name.endsWith(".properties");
    }
  }

  private ResultItem createEvent(ProcessFactory factory, String date, String name, String value) throws Exception {
    Date d = null;
    if (date == null) {
      d = new Date();

    } else {
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/ddHHmmss");
      d = fmt.parse(date);
    }

    return factory.createResultItem(null, name, value, (String) null, d);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    try {
      String root = rootFolder.getExpression();
      List<ResultItem> allEvents = new ArrayList<ResultItem>();
      if (root != null) {
        String testDataRootFolder = "C:/temp/monitoring/" + root;
        File testDataFolder = new File(testDataRootFolder);
        if (!testDataFolder.exists()) {
          throw new Exception("Test data folder not found for :" + testDataRootFolder);
        }
        File[] listFiles = testDataFolder.listFiles(new PropertiesFileFilter());
        for (File dataFile : listFiles) {
          List<ResultItem> events = buildEventsFromProperties(new FileInputStream(dataFile));
          allEvents.addAll(events);
          if (number.getExpression() != null) {
            int numb = Integer.parseInt(number.getExpression());
            while (numb > 0) {
              allEvents.addAll(events);
              numb = numb - 1;
            }
          }
        }
      }
      sendEventBatch(allEvents, response);

    } catch (Exception e) {
      throw new ProcessingException("Error generating dummy events from " + getFullName(), null, e);
    }
  }

  private void sendEventBatch(List<ResultItem> batch, ProcessResponse response) throws MessageException {
    for (ResultItem event : batch) {
      ManagedMessage message = createMessage(event, ManagedMessage.objectContentType);
      response.addOutputMessage(output, message);
    }

  }

  protected List<ResultItem> buildEventsFromProperties(InputStream propsStream) throws Exception {
    Properties resultItemProps = new Properties();
    try {
      resultItemProps.load(propsStream);
    } finally {
      if (propsStream != null) {
        try {
          propsStream.close();
        } catch (IOException e) {/* ignore */
          LOGGER.error("IO exception reading properties", e);

        }
      }
    }
    List<ResultItem> eventList = new ArrayList<ResultItem>();
    Enumeration<String> propnames = (Enumeration<String>) resultItemProps.propertyNames();
    while (propnames.hasMoreElements()) {
      String propName = (String) propnames.nextElement();
      ResultItem event = createEvent(resultItemProps.getProperty(propName), propName);
      if (event != null) {
        eventList.add(event);
      }
    }

    return eventList;
  }

  private ResultItem createEvent(String value, String name) {
    String[] eventFields = name.split("\\|");
    ProcessFactory factory = ProcessFactoryTracker.getService();
    
    if (eventFields.length == 1) {
      try {
        return createEvent(factory, null, name, value);
      } catch (Exception e) {
        LOGGER.error("Invalid event definition: " + value, e);
        ExecutionTracerService.trace(this, "Invalid event definition: " + value);
      }

    } else if (eventFields.length == 2) {
      try {

        return createEvent(factory, eventFields[1], eventFields[0], value);
      } catch (Exception e) {
        LOGGER.error("Invalid event definition: " + value, e);
        ExecutionTracerService.trace(this, "Invalid event definition: " + value);
      }
    } else {
      LOGGER.error("Invalid event definition: wrong nr of fields (should be 3) : " + value);
      ExecutionTracerService.trace(this, "Invalid event definition: wrong nr of fields (should be 3) : " + value);
    }
    return null;
  }

}
