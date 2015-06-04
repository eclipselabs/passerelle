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

package com.isencia.passerelle.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager.State;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Manager;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.engine.activator.Activator;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.ext.ErrorCollector;
import com.isencia.passerelle.ext.ExecutionControlStrategy;
import com.isencia.passerelle.ext.impl.SuspendResumeExecutionControlStrategy;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.model.util.RESTFacade;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * A FlowManager offers services to work with flows:
 * <ul>
 * <li>read flows from a std Java Reader or an URL (in moml format)
 * <li>write flows to a std Java Writer (in moml format)
 * <li>execute flows in a blocking mode or in non-blocking mode
 * </ul>
 * <p>
 * The <code>readMoml(URL)</code> can interact with the REST web services of a Passerelle Manager instance. The URL
 * should then identify a flow resource on the Passerelle Manager. To bootstrap this,
 * <code>getFlowsFromResourceLocation(URL)</code> allows to retrieve the complete list of all known flows on the
 * Passerelle Manager instance identified by the URL.
 * </p>
 * <p>
 * 
 * </p>
 * 
 * @author erwin
 * 
 */
public class FlowManager {

  private static FlowManager defaultFlowManager;

  public static FlowManager getDefault() {
    if (defaultFlowManager == null) {
      defaultFlowManager = new FlowManager();
    }
    return defaultFlowManager;
  }

  protected class ModelExecutionListener implements ExecutionListener, ErrorCollector {
    private Throwable throwable;

    private final Flow flow;
    private ExecutionListener[] delegateListeners;

    ModelExecutionListener(final Flow flow, ExecutionListener... delegateListeners) {
      this.flow = flow;
      this.delegateListeners = delegateListeners;
    }

    public void acceptError(final PasserelleException e) {
      executionError(null, e);
    }

    // IMPORTANT : inside the executionError impl we can not modify anything in the flow's composition
    // or this can lead to deadlock on ptolemy's Workspace internal locking!
    // e.g. flow.setManager(null) is absolutely forbidden!!
    public void executionError(ptolemy.actor.Manager manager, final Throwable throwable) {
      try {
        this.throwable = throwable;
        logger.error("Error during model execution", throwable);
        if (delegateListeners != null) {
          for (ExecutionListener delegateListener : delegateListeners) {
            if (delegateListener != null) {
              delegateListener.executionError(manager, throwable);
            }
          }
        }
      } finally {
        FlowManager.this.executionError(flow);
      }
    }

    public void executionFinished(final ptolemy.actor.Manager manager) {
      try {
        if (delegateListeners != null) {
          for (ExecutionListener delegateListener : delegateListeners) {
            if (delegateListener != null) {
              delegateListener.executionFinished(manager);
            }
          }
        }
      } finally {
        FlowManager.this.executionFinished(flow);
      }
    }

    public Throwable getThrowable() {
      return throwable;
    }

    public void illegalActionExceptionOccured() throws IllegalActionException {
      if (throwable != null) {
        Throwable t = throwable;
        if (t instanceof IllegalActionException) {
          throwable = null;
          throw (IllegalActionException) t;
        }
      }
    }

    public void managerStateChanged(ptolemy.actor.Manager manager) {
      if (delegateListeners != null) {
        for (ExecutionListener delegateListener : delegateListeners) {
          if (delegateListener != null) {
            delegateListener.managerStateChanged(manager);
          }
        }
      }
    }

    public void otherExceptionOccured() throws Throwable {
      if (throwable != null) {
        Throwable t = throwable;
        if (!(t instanceof IllegalActionException) && !(t instanceof PasserelleException)) {
          throwable = null;
          throw t;
        }
      }
    }

    public void passerelleExceptionOccured() throws PasserelleException {
      if (throwable != null) {
        Throwable t = throwable;
        if (t instanceof PasserelleException) {
          throwable = null;
          throw (PasserelleException) t;
        }
      }
    }
  }

  private final Logger logger = LoggerFactory.getLogger(FlowManager.class);

  // Maintains a mapping between locally executing flows and their managers
  private final Map<FlowHandle, Manager> flowExecutions = new HashMap<FlowHandle, Manager>();
  private final Map<FlowHandle, ExecutionListener> flowExecutionListeners = new HashMap<FlowHandle, ExecutionListener>();

  // Maintains a list of remotly executing flows
  private final List<Flow> remoteFlowExecutionsList = new ArrayList<Flow>();

  private static RESTFacade restFacade;

  public static void applyParameterSettings(Flow flow, Map<String, String> props) throws PasserelleException {
    applyParameterSettings(flow, props, null);
  }

  public static void applyParameterSettings(Flow flow, Map<String, String> props, Map<String, Object> contexts) throws PasserelleException {
    Iterator<Entry<String, String>> propsItr = props.entrySet().iterator();
    while (propsItr.hasNext()) {
      Entry<String, String> element = propsItr.next();
      String propName = element.getKey();
      String propValue = element.getValue();
      String[] nameParts = propName.split("[\\.]");

      Entity e = flow;
      // set model parameters
      if (e instanceof CompositeActor) {
        if (!e.attributeList().isEmpty()) {
          try {
            final Parameter p = (Parameter) e.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
            if (p != null) {
              p.setExpression(propValue);
              p.setPersistent(true);
            }
          } catch (final IllegalActionException e1) {
            e1.printStackTrace();
            // ignore
          }
        }
      }
      // parts[parts.length-1] is the parameter name
      // all the parts[] before that are part of the nested Parameter name
      for (int j = 0; j < nameParts.length - 1; j++) {
        if (e instanceof CompositeActor) {
          Entity test = ((CompositeActor) e).getEntity(nameParts[j]);
          if (test == null) {
            try {
              // maybe it is a director
              ptolemy.actor.Director d = ((CompositeActor) e).getDirector();
              if (d != null) {
                Parameter p = (Parameter) d.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                if (p != null) {
                  p.setExpression(propValue);
                  p.setPersistent(true);
                } else {
                  throw new PasserelleException(ErrorCode.FLOW_CONFIGURATION_ERROR, "Inconsistent parameter definition " + propName, flow, null);
                }
              }
            } catch (IllegalActionException e1) {
              // ignore
            }
          } else {
            e = ((CompositeActor) e).getEntity(nameParts[j]);
            if (e != null) {
              try {
                Parameter p = (Parameter) e.getAttribute(nameParts[nameParts.length - 1], Parameter.class);
                if (p != null) {
                  p.setExpression(propValue);
                  p.setPersistent(true);
                }
              } catch (IllegalActionException e1) {
                e1.printStackTrace();
                // ignore
              }
            }
          }
        } else {
          break;
        }
      }
    }
  }

  private static Flow buildFlowFromHandle(FlowHandle flowHandle) throws Exception {
    Flow toplevel = readMoml(new StringReader(flowHandle.getMoml()));
    toplevel.setHandle(flowHandle);
    return toplevel;
  }

  /**
   * Finds flow resources on the given base location, and constructs FlowHandle instances for them. <br/>
   * From these FlowHandles, the full flow can be constructed. <br/>
   * In this way, we can implement a lazy-loading approach for Flows.
   * <p>
   * If the URL points to a local location, it should be a folder on a local disk. Alternatively it can be a REST URL
   * pointing to a Passerelle Manager. A sample REST URL is in the form of :<br/>
   * http://localhost:8080/passerelle-manager/bridge/PasserelleManagerService/ V1.0/jobs/
   * </p>
   * 
   * @param baseResourceLocation
   * @return
   * @throws PasserelleException
   */
  public static Collection<FlowHandle> getFlowsFromResourceLocation(URL baseResourceLocation) throws PasserelleException {
    if (baseResourceLocation == null) {
      return null;
    }

    Collection<FlowHandle> results = null;

    String protocol = baseResourceLocation.getProtocol();
    if ("http".equals(protocol) || "https".equals(protocol)) {
      // it's probably/hopefully a REST url pointing towards a Passerelle
      // Manager
      if (restFacade == null) {
        initRESTFacade();
      }

      results = restFacade.getAllRemoteFlowHandles(baseResourceLocation);
    } else if ("file".equals(protocol)) {
      // it's a local directory
      results = new ArrayList<FlowHandle>();
      try {
        File dir = new File(baseResourceLocation.toURI());
        if (dir != null && dir.exists()) {
          // find all files that end with ".moml"
          FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File file, String name) {
              return name.endsWith(".moml");
            }
          };
          File[] fileList = dir.listFiles(filter);
          for (File file : fileList) {
            FlowHandle flowHandle = new FlowHandle(null, file.getName(), file.toURI().toURL());
            results.add(flowHandle);
          }
        }
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.FLOW_LOADING_ERROR, "Error reading flows from " + baseResourceLocation, null, e);
      }
    }
    return results != null ? results : new ArrayList<FlowHandle>();
  }

  /**
   * Get remote execution traces of the given flow. Remark that this may also update the flow execution status info in
   * the handle if the remote server responds with status "inactive". From that moment on, no more execution traces can
   * be requested for this handle!
   * 
   * @param fHandle
   * @throws PasserelleException
   * @throws IllegalArgumentException
   * @throws IllegalStateException
   */
  public static List<ExecutionTraceRecord> getRemoteExecutionTraces(FlowHandle fHandle) throws IllegalStateException, IllegalArgumentException,
      PasserelleException, Exception {
    if (fHandle.isRemote()) {
      if (restFacade == null) {
        initRESTFacade();
      }

      return restFacade.getRemoteExecutionTraces(fHandle);
    } else {
      throw new IllegalArgumentException("Flow is not managed remotely");
    }
  }

  /**
   * TODO make timeout values configurable
   */
  private static void initRESTFacade() {
    restFacade = new RESTFacade(10000, 10000);
  }

  /**
   * Read the Flow in MOML format from the given Reader.
   * 
   * @param in
   * @return the resulting flow
   * @throws Exception
   */
  public static Flow readMoml(Reader in) throws Exception {
    ClassLoader classLoader = null;
    try {
      classLoader = Activator.class.getClassLoader();
    } catch (final NoClassDefFoundError e) {
      // Activator class not found, so not inside an OSGi container
      classLoader = FlowManager.class.getClassLoader();
    }
    return readMoml(in, classLoader);
  }

  /**
   * Read the Flow in MOML format from the given Reader, using the given ClassLoader to instantiate actors etc.
   * 
   * @param in
   * @param classLoader
   * @return the resulting flow
   * @throws Exception
   */
  public static Flow readMoml(Reader in, ClassLoader classLoader) throws Exception {
    return readMoml(in, null, classLoader);
  }

  /**
   * Read the Flow in MOML format from the given Reader, with given default version specification and using the given
   * ClassLoader to instantiate actors etc.
   * <p>
   * The version specification will be used as default for all version-aware model elements, when the model itself does
   * not contain an explicit version specification for an element.
   * </p>
   * <p>
   * This is typically useful for code/tag version specs to allow an easy version-aware model parsing where all elements
   * should consistently be loaded with a same tag.
   * </p>
   * 
   * @param in
   * @param versionSpec
   * @param classLoader
   * @return
   * @throws Exception
   */
  public static Flow readMoml(Reader in, VersionSpecification versionSpec, ClassLoader classLoader) throws Exception {
    return readMoml(in, null, versionSpec, classLoader);
  }

  /**
   * Read the Flow in MOML format from the given Reader
   * <p>
   * The version specification will be used as default for all version-aware model elements, when the model itself does
   * not contain an explicit version specification for an element.
   * </p>
   * <p>
   * This is typically useful for code/tag version specs to allow an easy version-aware model parsing where all elements
   * should consistently be loaded with a same tag.
   * </p>
   * 
   * @param in
   * @param versionSpec
   * @return the resulting flow
   * @throws Exception
   */
  public static Flow readMoml(Reader in, VersionSpecification versionSpec) throws Exception {
    ClassLoader classLoader = null;
    try {
      classLoader = Activator.class.getClassLoader();
    } catch (final NoClassDefFoundError e) {
      // Activator class not found, so not inside an OSGi container
      classLoader = FlowManager.class.getClassLoader();
    }
    return readMoml(in, versionSpec, classLoader);
  }

  /**
   * 
   * Read the Flow in MOML format from the given Reader, with given default version specification and using the given
   * ClassLoader to instantiate actors etc.
   * <p>
   * The version specification will be used as default for all version-aware model elements, when the model itself does
   * not contain an explicit version specification for an element.
   * </p>
   * <p>
   * This is typically useful for code/tag version specs to allow an easy version-aware model parsing where all elements
   * should consistently be loaded with a same tag.
   * </p>
   * 
   * @param in
   * @param workspace
   * @param versionSpec
   * @param classLoader
   * @return
   * @throws Exception
   */
  public static Flow readMoml(Reader in, Workspace workspace, VersionSpecification versionSpec, ClassLoader classLoader) throws Exception {
    final MoMLParser parser = new MoMLParser(workspace, versionSpec, classLoader);
    final Flow toplevel = (Flow) parser.parse(null, in);
    return toplevel;
  }

  /**
   * Read the Flow in MOML format from the given URL.
   * 
   * @param in
   * @return the resulting flow
   * @throws Exception
   */
  public static Flow readMoml(URL xmlFile) throws Exception {
    ClassLoader classLoader = null;
    try {
      classLoader = Activator.class.getClassLoader();
    } catch (NoClassDefFoundError e) {
      // Activator class not found, so not inside an OSGi container
      classLoader = FlowManager.class.getClassLoader();
    }
    return readMoml(xmlFile, classLoader);
  }

  /**
   * Read the Flow in MOML format from the given URL.
   * 
   * @param xmlFile
   * @param classLoader
   * @return
   * @throws Exception
   */
  public static Flow readMoml(URL xmlFile, ClassLoader classLoader) throws Exception {
    return readMoml(xmlFile, null, classLoader);
  }

  /**
   * Read the Flow in MOML format from the given URL, with given default version specification and using the given
   * ClassLoader to instantiate actors etc.
   * <p>
   * The version specification will be used as default for all version-aware model elements, when the model itself does
   * not contain an explicit version specification for an element.
   * </p>
   * <p>
   * This is typically useful for code/tag version specs to allow an easy version-aware model parsing where all elements
   * should consistently be loaded with a same tag.
   * </p>
   * 
   * @param xmlFile
   * @param versionSpec
   * @param classLoader
   * @return
   * @throws Exception
   */
  public static Flow readMoml(URL xmlFile, VersionSpecification versionSpec, ClassLoader classLoader) throws Exception {
    return readMoml(xmlFile, null, versionSpec, classLoader);
  }

  /**
   * Read the Flow in MOML format from the given URL, with given default version specification and using the given
   * ClassLoader to instantiate actors etc.
   * <p>
   * The version specification will be used as default for all version-aware model elements, when the model itself does
   * not contain an explicit version specification for an element.
   * </p>
   * <p>
   * This is typically useful for code/tag version specs to allow an easy version-aware model parsing where all elements
   * should consistently be loaded with a same tag.
   * </p>
   * 
   * @param xmlFile
   * @param versionSpec
   * @param workspace
   * @param classLoader
   * @return
   * @throws Exception
   */
  public static Flow readMoml(URL xmlFile, Workspace workspace, VersionSpecification versionSpec, ClassLoader classLoader) throws Exception {
    if (xmlFile == null) {
      return null;
    }

    String protocol = xmlFile.getProtocol();
    if ("file".equals(protocol) || "jar".equals(protocol) || "bundleresource".equals(protocol)) {
      // it's a local moml
      MoMLParser parser = new MoMLParser(workspace, versionSpec, classLoader);
      MoMLParser.purgeModelRecord(xmlFile);
      Flow toplevel = (Flow) parser.parse(null, xmlFile);
      final FlowHandle handle = new FlowHandle(0L, toplevel.getFullName(), xmlFile);
      toplevel.setHandle(handle);
      return toplevel;
    } else if ("http".equals(protocol) || "https".equals(protocol)) {
      // it's probably/hopefully a REST url pointing towards a Passerelle Manager
      if (restFacade == null) {
        initRESTFacade();
      }
      FlowHandle flowHandle = restFacade.getRemoteFlowHandle(xmlFile);
      return buildFlowFromHandle(flowHandle);
    } else {
      throw new IllegalArgumentException("Unsupported URL protocol " + protocol);
    }
  }

  /**
   * For locally managed flows, saves the flow's moml on the given URL (which should point to a local file). For
   * remotely managed flows, updates the flow on the remote Passerelle Manager. Then the url parameter is ignored.
   * 
   * @param flow
   * @param url
   * @return the updated/saved flow (if it's a remote one, then with updated FlowHandle)
   * @throws IOException
   * @throws PasserelleException
   */
  public static Flow save(final Flow flow, final URL url) throws IOException, PasserelleException, Exception {
    if (!flow.getHandle().isRemote()) {
      FlowManager.writeMoml(flow, new BufferedWriter(new FileWriter(url.getFile())));
      return flow;
    } else {
      if (restFacade == null) {
        initRESTFacade();
      }
      FlowHandle updatedhandle = restFacade.updateRemoteFlow(flow);
      return buildFlowFromHandle(updatedhandle);
    }
  }

  public static Flow saveMomlParameterUpdates(Flow flow, Map<String, String> updatedParams) throws Exception {
    applyParameterSettings(flow, updatedParams);
    if (flow.getHandle().isRemote()) {
      if (restFacade == null) {
        initRESTFacade();
      }

      FlowHandle updatedFlowhandle = restFacade.updateRemoteFlow(flow.getHandle(), updatedParams);
      return buildFlowFromHandle(updatedFlowhandle);
    } else {
      if (flow.getHandle().getAuthorativeResourceLocation() != null) {
        FileWriter out = null;
        try {
          File outputFile = new File(flow.getHandle().getAuthorativeResourceLocation().toURI());
          out = new FileWriter(outputFile);
          writeMoml(flow, out);
        } finally {
          if (out != null) {
            try {
              out.close();
            } catch (IOException e) {
            }
          }
        }
      }
      return flow;
    }
  }

  /**
   * Write the Flow in MOML format to the given Writer.
   * 
   * @param flow
   * @param out
   * @throws IOException
   *           if the writer raises an IOException during the moml writing.
   */
  public static void writeMoml(Flow flow, Writer out) throws IOException {
    String moml = flow.exportMoML();
    out.append(moml);
    out.flush();
  }

  public void execute(final Flow flow, final Map<String, String> props) throws Exception {
    if (flow.getHandle().isRemote()) {
      executeOnServer(flow, props);
    } else {
      executeLocally(flow, props);
    }
  }

  public void execute(final Flow flow, final Map<String, String> props, final ExecutionListener executionListener) throws Exception {
    if (flow.getHandle().isRemote()) {
      executeOnServer(flow, props);
      flow.setExecutionListener(executionListener);
    } else {
      executeLocally(flow, props, executionListener);
    }
  }

  /**
   * Executes the given flow and blocks till the execution finishes. If the execution fails, an exception is thrown
   */
  public void executeBlockingErrorLocally(Flow flow, Map<String, String> props) throws FlowAlreadyExecutingException, PasserelleException {
    FlowHandle handle = flow.getHandle();
    if (handle == null) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Invalid flow : missing FlowHandle", flow, null);
    }
    checkFlowAlreadyExecuting(flow);
    if (props != null) {
      applyParameterSettings(flow, props);
    }

    ModelExecutionListener executionListener = new ModelExecutionListener(flow);
    try {
      Manager manager = new Manager(flow.workspace(), flow.getName());
      flow.setManager(manager);
      flowExecutions.put(handle, manager);
      DirectorAdapter dirAdapter = flow.getDirectorAdapter();
      dirAdapter.removeAllErrorCollectors();
      dirAdapter.addErrorCollector(executionListener);
      manager.addExecutionListener(executionListener);
      manager.execute();
    } catch (Exception e) {
      if (e.getCause() instanceof PasserelleException) {
        throw ((PasserelleException) e.getCause());
      } else {
        throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flow, e);
      }
    } finally {
      executionFinished(flow);
    }

    try {
      executionListener.illegalActionExceptionOccured();
    } catch (IllegalActionException e) {
      throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flow, e);
    }
    executionListener.passerelleExceptionOccured();

    try {
      executionListener.otherExceptionOccured();
    } catch (Throwable e) { // NOSONAR - need to make sure any problem is wrapped in a PasserelleException
      throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flow, e);
    }
  }

  /**
   * Executes the given flow and blocks till the execution finishes. <br>
   * REMARK: Not all flows will stop automatically. For such flows, this execution method will block forever, unless the
   * flow execution is stopped explicitly, by invoking FlowManager.stop() for the given flow. <br>
   * The flow's actors' parameter values can be configured with the given properties. Parameter/property names are of
   * the format "actor_name.param_name", where the actor name itself can be nested. E.g. in the case of using composite
   * actors in a model... <br>
   * Parameter values should be passed as String values. For numerical parameter types, the conversion to the right
   * numerical type will be done internally. <br/>
   * Any exceptions raised during the model execution will not be delivered to an ExecutionListener, but will be thrown
   * from here, wrapped in a PasserelleException
   * 
   * @param flow
   * @param props
   * @throws FlowAlreadyExecutingException
   *           if the flow is already executing.
   * @throws PasserelleException
   *           any possible exceptions during the flow execution.
   */
  public void executeBlockingLocally(Flow flow, Map<String, String> props) throws FlowAlreadyExecutingException, PasserelleException {
    FlowHandle handle = flow.getHandle();
    if (handle == null) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Invalid flow : missing FlowHandle", flow, null);
    }
    // should not be necessary, a flow's handle already knows about it's flow
    // handle.setLocalFlow(flow);
    checkFlowAlreadyExecuting(flow);
    if (props != null) {
      applyParameterSettings(flow, props);
    }
    try {
      Manager manager = new Manager(flow.workspace(), flow.getName());
      flow.setManager(manager);
      flowExecutions.put(handle, manager);
      manager.execute();
    } catch (Exception e) {
      if (e.getCause() instanceof PasserelleException) {
        throw ((PasserelleException) e.getCause());
      } else {
        throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flow, e);
      }
    } finally {
      executionFinished(flow);
    }
  }

  /**
   * Executes the given flow and blocks till the execution finishes. <br>
   * REMARK: Not all flows will stop automatically. For such flows, this execution method will block forever, unless the
   * flow execution is stopped explicitly, by invoking FlowManager.stop() for the given flow. <br>
   * The flow's actors' parameter values can be configured with the given properties. Parameter/property names are of
   * the format "actor_name.param_name", where the actor name itself can be nested. E.g. in the case of using composite
   * actors in a model... <br>
   * Parameter values should be passed as String values. For numerical parameter types, the conversion to the right
   * numerical type will be done internally. <br/>
   * A listener can be passed, which in this blocking execution mainly serves to be notified of any errors during the
   * execution.
   * 
   * @param flow
   * @param props
   * @param listener
   * @throws FlowAlreadyExecutingException
   *           if the flow is already executing.
   * @throws PasserelleException
   *           any possible exceptions during the flow execution.
   */
  public void executeBlockingLocally(Flow flow, Map<String, String> props, ExecutionListener listener) throws FlowAlreadyExecutingException,
      PasserelleException {
    FlowHandle handle = flow.getHandle();
    if (handle == null) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Invalid flow : missing FlowHandle", flow, null);
    }
    checkFlowAlreadyExecuting(flow);
    if (props != null) {
      applyParameterSettings(flow, props);
    }
    try {
      Manager manager = new Manager(flow.workspace(), flow.getName());
      flow.setManager(manager);
      flowExecutions.put(handle, manager);
      manager.addExecutionListener(listener);
      manager.run();
    } catch (Exception e) {
      if (e.getCause() instanceof PasserelleException) {
        throw ((PasserelleException) e.getCause());
      } else {
        throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flow, e);
      }
    } finally {
      executionFinished(flow);
    }
  }

  /**
   * Executes the given flow and returns immediately, without waiting for the flow to finish its execution. <br>
   * The flow's actors' parameter values can be configured with the given properties. Parameter/property names are of
   * the format "actor_name.param_name", where the actor name itself can be nested. E.g. in the case of using composite
   * actors in a model... <br>
   * Parameter values should be passed as String values. For numerical parameter types, the conversion to the right
   * numerical type will be done internally.
   * 
   * @param flow
   * @param props
   * @param executionListeners
   * @throws PasserelleException
   */
  private void executeLocally(final Flow flow, final Map<String, String> props, final ExecutionListener... executionListeners)
      throws FlowAlreadyExecutingException, PasserelleException {
    FlowHandle handle = flow.getHandle();
    if (handle == null) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Invalid flow : missing FlowHandle", flow, null);
    }
    checkFlowAlreadyExecuting(flow);
    if (props != null) {
      applyParameterSettings(flow, props);
    }
    try {
      ModelExecutionListener listener = this.new ModelExecutionListener(flow, executionListeners);
      flowExecutionListeners.put(handle, listener);
      final Manager manager = new Manager(flow.workspace(), flow.getName());
      manager.addExecutionListener(listener);

      flow.setManager(manager);
      flowExecutions.put(handle, manager);
      manager.startRun();
    } catch (Exception e) {
      if (e.getCause() instanceof PasserelleException) {
        throw ((PasserelleException) e.getCause());
      } else {
        throw new PasserelleException(ErrorCode.FLOW_EXECUTION_ERROR, flow, e);
      }
    }
  }

  /**
   * @param flow
   * @param handle
   * @throws FlowAlreadyExecutingException
   */
  protected void checkFlowAlreadyExecuting(final Flow flow) throws FlowAlreadyExecutingException {
    Manager mgr = flowExecutions.get(flow.getHandle());
    if (mgr != null) {
      if (ptolemy.actor.Manager.IDLE.equals(mgr.getState())) {
        // probably means the execution-finished event did not arrive,
        // so do the corresponding cleanup here
        executionFinished(flow);
      } else {
        throw new FlowAlreadyExecutingException(flow);
      }
    }
  }

  /**
   * Execute the given flow on the Passerelle Manager server instance, from which it was previously obtained. <br/>
   * Locally constructed Flows can not be launched on a server!
   * <p>
   * If the <code>updatedParams</code> is non-empty, a new model revision is created and activated on the server first,
   * with the updated parameters. Then this new revision is launched.
   * </p>
   * <p>
   * Parameters should be identified by their full names, but optionally the flow's name can be chopped off. E.g.
   * .helloWorldModel.Constant.value or Constant.value
   * </p>
   * <p>
   * For parameters inside submodels, the submodel-hierarchy must still be present. E.g.
   * .helloWorldModel.submodel.subSubmodel.Constant.value or submodel.subSubmodel.Constant.value
   * </p>
   * <p>
   * Remark that when the model name is present, there's an extra spurious '.' at the beginning, which is not assumed to
   * be present when the model name is absent!
   * </p>
   * 
   * @param flow
   *          a flow with a authorative resource URL on a Passerelle Manager server
   * @param updatedParams
   *          a map with updated parameters.
   * @throws Exception
   * @return the flow with added execution UID in its FLlwHandle
   */
  private Flow executeOnServer(Flow flow, Map<String, String> updatedParams) throws Exception {
    if (restFacade == null) {
      initRESTFacade();
    }
    if (remoteFlowExecutionsList.contains(flow)) {
      throw new FlowAlreadyExecutingException(flow);
    }

    FlowHandle flowHandle = flow.getHandle();
    if (updatedParams != null && !updatedParams.isEmpty()) {
      flowHandle = restFacade.updateRemoteFlow(flow.getHandle(), updatedParams);
    }
    FlowHandle startedFlowhandle = restFacade.startFlowRemotely(flowHandle);
    remoteFlowExecutionsList.add(flow);
    flow.setHandle(startedFlowhandle);

    return flow;
  }

  /**
   * Set the flow in standby state
   * 
   * @param flow
   */
  private synchronized void executionFinished(final Flow flow) {
    flowExecutionListeners.remove(flow.getHandle());
    if (flow.getHandle().isRemote()) {
      remoteFlowExecutionsList.remove(flow);
    } else {
      final Manager mgr = flowExecutions.get(flow.getHandle());
      if (mgr != null) {
        flowExecutions.remove(flow.getHandle());
        try {
          flow.setManager(null);
        } catch (final IllegalActionException e) {
          // ignore
        }
      }
    }
  }

  /**
   * Set the flow in standby state after an error
   * 
   * @param flow
   */
  private synchronized void executionError(final Flow flow) {
    flowExecutionListeners.remove(flow.getHandle());
    if (flow.getHandle().isRemote()) {
      remoteFlowExecutionsList.remove(flow);
    } else {
      final Manager mgr = flowExecutions.get(flow.getHandle());
      if (mgr != null) {
        flowExecutions.remove(flow.getHandle());
        // this seems to lead to deadlocks inside Ptolemy!
//        try {
//          flow.setManager(null);
//        } catch (final IllegalActionException e) {
//          // ignore
//        }
      }
    }
  }

  /**
   * 
   * @param executionLocation
   *          : if null : return locally executing flows, otherwise should have http(s) as protocol and should identify
   *          a Passerelle Manager web service entry point
   * 
   * @return
   * @throws PasserelleException
   */
  public Collection<FlowHandle> getExecutingFlows(URL executionLocation) throws PasserelleException {
    if (executionLocation != null && executionLocation.getProtocol().startsWith("http")) {
      if (restFacade == null) {
        initRESTFacade();
      }

      return restFacade.getAllRemoteExecutingFlowHandles(executionLocation);
    } else {
      Set<FlowHandle> localExecutingFlows = flowExecutions.keySet();
      return Collections.unmodifiableCollection(localExecutingFlows);
    }
  }

  public State getLocalExecutionState(Flow flow) throws FlowNotExecutingException {
    if (flow.getHandle().isRemote()) {
      throw new IllegalArgumentException("Flow is not managed locally");
    }
    Manager mgr = flowExecutions.get(flow.getHandle());
    if (mgr == null) {
      throw new FlowNotExecutingException(flow);
    }

    return mgr.getState();
  }

  public List<Flow> getRemoteFlowExecutionsList() {
    return remoteFlowExecutionsList;
  }

  /**
   * Stop the execution of the given flow.
   * 
   * @param flow
   * @throws PasserelleException
   * @throws IllegalArgumentException
   * @throws IllegalStateException
   * 
   * @deprecated this version may block forever for models that do not stop correctly. Use stopExecution(Flow flow, long
   *             timeout) instead.
   */
  public synchronized Flow stopExecution(Flow flow) throws IllegalStateException, IllegalArgumentException, PasserelleException, Exception {
    return stopExecution(flow, -1);
  }

  /**
   * Stop the execution of the given flow and wait at most the given time in ms for the full stop.
   * 
   * If the model has not stopped completely within the given time, a PasserelleException is thrown.
   * 
   * @param flow
   * @param timeOut_ms
   * @return
   * @throws IllegalStateException
   * @throws IllegalArgumentException
   * @throws PasserelleException
   * @throws Exception
   */
  public synchronized Flow stopExecution(Flow flow, long timeOut_ms) throws IllegalStateException, IllegalArgumentException, PasserelleException, Exception {
    logger.info("Stopping {}", flow.getName());

    FlowHandle handle = flow.getHandle();
    if (handle == null) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Invalid flow : missing FlowHandle", flow, null);
    }
    if (handle.isRemote()) {
      if (!remoteFlowExecutionsList.contains(flow)) {
        throw new FlowNotExecutingException(flow);
      }
      if (restFacade == null) {
        initRESTFacade();
      }
      FlowHandle stoppedRemoteFlowHandle = restFacade.stopFlowRemotely(handle);
      // rely on indirect removal via trace polling in HMI
      // remoteFlowExecutionsList.remove(flow);
      return buildFlowFromHandle(stoppedRemoteFlowHandle);
    } else {
      Manager mgr = flowExecutions.get(handle);
      if (mgr == null) {
        throw new FlowNotExecutingException(flow);
      }
      mgr.stop();
      // wait for stop completion
      long elapsedTime = 0;
      boolean timeOut = false;
      while (!mgr.getState().equals(ptolemy.actor.Manager.IDLE) && !timeOut) {
        try {
          // System.out.println(getLocalExecutionState(flow));
          Thread.sleep(100);
          elapsedTime += 100;
          timeOut = timeOut_ms > 0 && (timeOut_ms < elapsedTime);
        } catch (final InterruptedException e) {
        }
      }
      logger.info("{} stopped to state {}", flow.getName(), mgr.getState());
      try {
        Collection<Thread> remainingThreads = ((Director) flow.getDirector()).getThreads();
        if (!remainingThreads.isEmpty()) {
          logger.warn("Failed to do a clean stop of {}", flow.getName());
          for (Thread thread : remainingThreads) {
            logger.warn("{} - pending thread {}", flow.getName(), thread.toString());
          }
        }
      } catch (Exception e) {
        // ignore
      }
      executionFinished(flow);
      return flow;
    }
  }

  public synchronized Flow pauseExecution(Flow flow) throws IllegalStateException, IllegalArgumentException, PasserelleException, Exception {
    FlowHandle handle = flow.getHandle();
    if (handle == null) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Invalid flow : missing FlowHandle", flow, null);
    }
    if (handle.isRemote()) {
      throw new IllegalArgumentException("Suspend not yet supported for remote execution");
    } else {
      Manager mgr = flowExecutions.get(handle);
      if (mgr == null) {
        throw new FlowNotExecutingException(flow);
      } else {
        mgr.pause();

        // try {
        // ExecutionControlStrategy execCtrlStrategy = ((Director)flow.getDirector()).getExecutionControlStrategy();
        // if(execCtrlStrategy instanceof SuspendResumeExecutionControlStrategy) {
        // ((SuspendResumeExecutionControlStrategy) execCtrlStrategy).suspend();
        // }
        // } catch (ClassCastException e) {
        // logger.error("Received suspend event, but model not configured correctly", e);
        // }
      }
      return flow;
    }
  }

  /**
   * Resuming a model execution may be a slow operation, as each paused actor is sequentially resumed, before all actor
   * threads are allowed to start iterating again. <br/>
   * Some of the actors may take some time for their "resume", e.g. when waiting for external events. In the meantime,
   * the whole FlowManager instance is locked!
   * 
   * TODO : check if it is not better after all to do the actor resume within their actor threads!?
   * 
   * @param flow
   * @return
   * @throws IllegalStateException
   * @throws IllegalArgumentException
   * @throws PasserelleException
   * @throws Exception
   */
  public synchronized Flow resumeExecution(Flow flow) throws IllegalStateException, IllegalArgumentException, PasserelleException, Exception {
    FlowHandle handle = flow.getHandle();
    if (handle == null) {
      throw new PasserelleException(ErrorCode.FLOW_STATE_ERROR, "Invalid flow : missing FlowHandle", flow, null);
    }
    if (handle.isRemote()) {
      throw new IllegalArgumentException("Flow is not managed locally");
    } else {
      Manager mgr = flowExecutions.get(handle);
      if (mgr == null) {
        throw new FlowNotExecutingException(flow);
      }
      mgr.resume();

      try {
        ExecutionControlStrategy execCtrlStrategy = flow.getDirectorAdapter().getExecutionControlStrategy();
        if (execCtrlStrategy instanceof SuspendResumeExecutionControlStrategy) {
          ((SuspendResumeExecutionControlStrategy) execCtrlStrategy).resume();
        }
      } catch (ClassCastException e) {
        logger.error("Received resume event, but model not configured correctly", e);
      }
      return flow;
    }
  }
}
