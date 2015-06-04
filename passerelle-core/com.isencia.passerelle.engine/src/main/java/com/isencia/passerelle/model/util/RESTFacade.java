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
package com.isencia.passerelle.model.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.model.ExecutionTraceRecord;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowHandle;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.FlowNotExecutingException;

/**
 * A utility class to support the FlowManager in its interactions with a REST-based flow server a.k.a. Passerelle Manager instance.
 * 
 * @author erwin
 */
public class RESTFacade {

  private static final ContentType MOML_CONTENT_TYPE = ContentType.create("text/xml", "UTF-8");

  private final static Logger LOGGER = LoggerFactory.getLogger(RESTFacade.class);

  private CloseableHttpClient httpClient;
  private RequestConfig requestConfig;

  public RESTFacade(int connectionTimeout, int socketTimeout) {
    RequestConfig.Builder requestConfigBldr = RequestConfig.custom();
    final String proxyHost = System.getProperty("http.proxyHost");
    final String proxyPort = System.getProperty("http.proxyPort");
    if (proxyHost != null && proxyPort != null && !"".equals(proxyHost) && !"".equals(proxyPort)) {
      LOGGER.debug("configure proxy with " + proxyHost + ":" + proxyPort);
      HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort), "http");
      requestConfigBldr.setProxy(proxy); 
    }
    RequestConfig requestConfig = requestConfigBldr.setSocketTimeout(socketTimeout).
      setConnectTimeout(connectionTimeout).setConnectionRequestTimeout(connectionTimeout).build();
    
    httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
  }

  @SuppressWarnings("unchecked")
  private Collection<FlowHandle> buildExecutingFlowHandles(URL baseURL, String jobHeadersResponse) throws PasserelleException {
    Collection<FlowHandle> flowHandles = new ArrayList<FlowHandle>();
    SAXBuilder parser = new SAXBuilder();
    Document doc = null;
    try {
      doc = parser.build(new StringReader(jobHeadersResponse));
    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Unable to parse response data " + jobHeadersResponse, e);
    }
    if (doc != null) {
      List<Element> scheduledJobElements = doc.getRootElement().getChildren("scheduledJob");
      for (Element scheduledJobElement : scheduledJobElements) {
        Element execInfo = scheduledJobElement.getChild("execInfo");
        String execId = execInfo.getAttributeValue("id");
        // String execName = execInfo.getAttributeValue("name");
        String execHREF = execInfo.getAttributeValue("href");
        Element jobElement = scheduledJobElement.getChild("job");
        String jobId = jobElement.getAttributeValue("id");
        String jobHREF = jobElement.getAttributeValue("href");
        String jobName = jobElement.getAttributeValue("name");
        if (jobId != null && jobName != null && jobHREF != null) {
          try {
            FlowHandle flowHandle = new FlowHandle(new Long(jobId), jobName, new URL(jobHREF));
            flowHandle.setExecId(execId);
            flowHandle.setExecResourceLocation(new URL(execHREF));
            flowHandles.add(flowHandle);
          } catch (Exception e) {
            throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Invalid URL " + jobHREF + " in response " + jobHeadersResponse, e);
          }
        }
      }
    }
    return flowHandles;
  }

  protected FlowHandle buildFlowHandle(String seqDetail) throws PasserelleException {
    FlowHandle flowHandle = null;
    SAXBuilder parser = new SAXBuilder();
    Document doc = null;
    try {
      doc = parser.build(new StringReader(seqDetail));
      if (doc != null) {
        String id = doc.getRootElement().getAttributeValue("id");
        if (id == null)
          id = "0";
        String href = doc.getRootElement().getAttributeValue("href");
        String name = doc.getRootElement().getAttributeValue("name");
        String code = doc.getRootElement().getAttributeValue("code");
        String moml = doc.getRootElement().getChildText("moml");
        flowHandle = new FlowHandle(new Long(id), code, name, new URL(href));
        flowHandle.setMoml(moml);
      }
    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Unable to parse response data " + seqDetail, e);
    }

    return flowHandle;
  }

  @SuppressWarnings("unchecked")
  private Collection<FlowHandle> buildFlowHandles(String seqHeadersResponse) throws PasserelleException {
    Collection<FlowHandle> flowHandles = new ArrayList<FlowHandle>();
    SAXBuilder parser = new SAXBuilder();
    Document doc = null;
    try {
      doc = parser.build(new StringReader(seqHeadersResponse));
    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Unable to parse response data " + seqHeadersResponse, e);
    }
    if (doc != null) {
      List<Element> seqElements = doc.getRootElement().getChildren("sequence");
      for (Element seqElement : seqElements) {
        String id = seqElement.getAttributeValue("id");
        if (id == null)
          id = "0";
        String href = seqElement.getAttributeValue("href");
        String name = seqElement.getAttributeValue("name");
        String code = seqElement.getAttributeValue("code");
        try {
          FlowHandle flowHandle = new FlowHandle(new Long(id), name, new URL(href));
          flowHandles.add(flowHandle);
        } catch (Exception e) {
          throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Invalid URL " + href + " in response " + seqHeadersResponse, e);
        }
      }
    }
    return flowHandles;
  }

  /**
   * Get all flow handles for all currently executing jobs from a Passerelle Manager server instance. The baseURL should identify the REST service, i.e. be of
   * the form : <code>http://localhost:8080/PasserelleManagerService/V1.0</code>.
   * 
   * @param baseURL
   * @return
   * @throws PasserelleException
   */
  public Collection<FlowHandle> getAllRemoteExecutingFlowHandles(URL baseURL) throws PasserelleException {
    String baseURLStr = baseURL.toString();
    baseURLStr += "/scheduledjobs";
    String scheduledJobsResponse = invokeMethodForURL(new HttpGet(baseURLStr));
    if (scheduledJobsResponse != null) {
      Collection<FlowHandle> flowHandles = buildExecutingFlowHandles(baseURL, scheduledJobsResponse);
      return flowHandles;
    } else {
      return new ArrayList<FlowHandle>(0);
    }
  }

  /**
   * Get all flow handles from a Passerelle Manager server instance. The baseURL should identify the REST service, i.e. be of the form :
   * <code>http://localhost:8080/PasserelleManagerService/V1.0</code>.
   * 
   * @param baseURL
   *          should be not-null or an NPE will be thrown!
   * @return
   * @throws PasserelleException
   */
  public Collection<FlowHandle> getAllRemoteFlowHandles(URL baseURL) throws PasserelleException {
    String baseURLStr = baseURL.toString();

    if (!baseURLStr.endsWith("sequences")) {
      baseURLStr += "/sequences";
    }
    String sequenceHeadersResponse = invokeMethodForURL(new HttpGet(baseURLStr));
    if (sequenceHeadersResponse != null) {
      Collection<FlowHandle> flowHandles = buildFlowHandles(sequenceHeadersResponse);
      return flowHandles;
    } else {
      return new ArrayList<FlowHandle>(0);
    }
  }

  /**
   * As a side effect, based on the response status info for the remote execution, the fHandle may be updated when it is noticed that the remote execution is
   * done.
   * 
   * @param fHandle
   * @return
   * @throws PasserelleException
   */
  public List<ExecutionTraceRecord> getRemoteExecutionTraces(FlowHandle fHandle) throws PasserelleException {
    if (fHandle.getExecResourceLocation() != null) {
      String tracesURL = fHandle.getExecResourceLocation().toString() + "/traces";
      String tracesResponse = invokeMethodForURL(new HttpGet(tracesURL.toString()));

      List<ExecutionTraceRecord> traces = new ArrayList<ExecutionTraceRecord>();

      SAXBuilder parser = new SAXBuilder();
      Document doc = null;
      try {
        doc = parser.build(new StringReader(tracesResponse));
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Unable to parse response data " + tracesResponse, e);
      }
      if (doc != null) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        String status = doc.getRootElement().getChild("scheduledJob").getAttributeValue("status");
        if ("inactive".equalsIgnoreCase(status)) {
          fHandle.setExecResourceLocation(null);
          fHandle.setExecId(null);
        }

        List<Element> traceElements = doc.getRootElement().getChildren("trace");
        for (Element traceElem : traceElements) {
          String id = traceElem.getAttributeValue("id");
          String session = traceElem.getAttributeValue("session");
          String name = traceElem.getAttributeValue("name");
          String timeStampStr = traceElem.getAttributeValue("time");
          Date timeStamp = null;
          try {
            timeStamp = dateFormat.parse(timeStampStr);
          } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          String actor = traceElem.getAttributeValue("actor");
          String message = traceElem.getAttributeValue("message");
          ExecutionTraceRecord trace = new ExecutionTraceRecord(new Long(id), session, name, actor, message, timeStamp);
          traces.add(trace);
        }
      }

      return traces;
    } else {
      throw new FlowNotExecutingException(fHandle.getName());
    }
  }

  /**
   * @param seqURL
   * @return
   * @throws PasserelleException
   */
  public FlowHandle getRemoteFlowHandle(URL seqURL) throws PasserelleException {
    String seqDetailResponse = invokeMethodForURL(new HttpGet(seqURL.toString()));
    FlowHandle flowHandle = buildFlowHandle(seqDetailResponse);
    return flowHandle;
  }

  private synchronized String invokeMethodForURL(HttpRequestBase method) {
    CloseableHttpResponse response = null;
    try {
      // Execute the method.
      response = httpClient.execute(method);

      if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        LOGGER.warn("Response status error : " + response.getStatusLine());
      }

      String responseText = EntityUtils.toString(response.getEntity());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Received response\n" + responseText);
      }

      return responseText;
    } catch (ClientProtocolException e) {
      LOGGER.error("Fatal protocol violation: ", e);
      return null;
    } catch (IOException e) {
      LOGGER.error("Fatal transport error: ", e);
      return null;
    } finally {
      // Release the connection.
      if (response != null) {
        try {
          response.close();
        } catch (IOException e) {
          LOGGER.error("Error closing Http response", e);
        }
      }
    }
  }

  public Flow startFlowRemotely(Flow flow) throws PasserelleException, IllegalStateException, IllegalArgumentException {
    FlowHandle handle = startFlowRemotely(flow.getHandle());
    return flow;
  }

  /**
   * @param fHandle
   * @return the flow handle with its newly obtained execId
   * @throws PasserelleException
   * @throws IllegalStateException
   *           e.g. when the flow is already executing
   * @throws IllegalArgumentException
   *           e.g. when the handle does not correspond to an existing flow
   */
  public FlowHandle startFlowRemotely(FlowHandle fHandle) throws PasserelleException, IllegalStateException, IllegalArgumentException {
    String startURL = fHandle.getAuthorativeResourceLocation().toString() + "/launch";
    String startInfo = invokeMethodForURL(new HttpPost(startURL));
    SAXBuilder parser = new SAXBuilder();
    Document doc = null;
    try {
      doc = parser.build(new StringReader(startInfo));
    } catch (Exception e) {
      throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Unable to parse response data " + startInfo, e);
    }
    if (doc != null) {
      Element scheduledJobElement = doc.getRootElement().getChild("scheduledJob");
      Element execInfo = scheduledJobElement.getChild("execInfo");
      String execId = execInfo.getAttributeValue("id");
      String execHREF = execInfo.getAttributeValue("href");
      fHandle.setExecId(execId);
      try {
        fHandle.setExecResourceLocation(new URL(execHREF));
      } catch (Exception e) {
        throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Invalid URL " + execHREF + " in response " + startInfo, e);
      }
    }
    return fHandle;
  }

  /**
   * @param fHandle
   *          should have a valid execId, corresponding to an actual executing flow
   * @return the flow handle without its execId
   * @throws PasserelleException
   * @throws IllegalStateException
   *           e.g. when the flow is not executing
   * @throws IllegalArgumentException
   *           e.g. when the handle does not correspond to an existing flow
   */
  public FlowHandle stopFlowRemotely(FlowHandle fHandle) throws PasserelleException, IllegalStateException, IllegalArgumentException {
    String stopURL = fHandle.getExecResourceLocation().toString() + "/stop";
    String stopInfo = invokeMethodForURL(new HttpPost(stopURL));
    fHandle.setExecId(null);
    fHandle.setExecResourceLocation(null);
    return fHandle;
  }

  /**
   * Update method that updates the complete flow's moml. For the moment, we assume that only parameter overrides have been done, but no way to guarantee this
   * for the moment...
   * 
   * @param flow
   * @return
   * @throws PasserelleException
   */
  public FlowHandle updateRemoteFlow(Flow flow) throws PasserelleException {
    if (!flow.getHandle().isRemote()) {
      throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Trying to remotely update a local flow", flow, null);
    } else {
      String updateURL = flow.getHandle().getAuthorativeResourceLocation().toString() + "/update";
      HttpPost updateMethod = new HttpPost(updateURL);
      try {
        StringWriter momlWriter = new StringWriter();
        FlowManager.writeMoml(flow, momlWriter);
        updateMethod.setEntity(new StringEntity(momlWriter.toString(),  MOML_CONTENT_TYPE));
        String seqDetailResponse = invokeMethodForURL(updateMethod);
        FlowHandle updatedFlowHandle = buildFlowHandle(seqDetailResponse);
        return updatedFlowHandle;
      } catch (UnsupportedEncodingException e) {
        throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Transport error related to UTF-8 encoding", flow, e);
      } catch (IOException e) {
        throw new PasserelleException(ErrorCode.RUNTIME_COMMUNICATION_ERROR, "Error writing flow moml", flow, e);
      }
    }
  }

  /**
   * Update method that explicitly limits the updates to parameter value changes
   * 
   * @param fHandle
   * @param parameterUpdates
   * @return
   * @throws PasserelleException
   */
  public FlowHandle updateRemoteFlow(FlowHandle fHandle, Map<String, String> parameterUpdates) throws PasserelleException {
    String updateURL = null;
    try {
      updateURL = fHandle.getAuthorativeResourceLocation().toString() + "/update";
      URIBuilder uriBldr = new URIBuilder(updateURL);
      for (Entry<String, String> paramUpdateEntry : parameterUpdates.entrySet()) {
        uriBldr.setParameter(paramUpdateEntry.getKey(), paramUpdateEntry.getValue());
      }
      HttpPost updateMethod = new HttpPost(updateURL);
      String seqDetailResponse = invokeMethodForURL(updateMethod);
      FlowHandle updatedFlowHandle = buildFlowHandle(seqDetailResponse);
      return updatedFlowHandle;
    } catch (URISyntaxException e) {
      throw new PasserelleException(ErrorCode.FLOW_SAVING_ERROR_TECH, "Invalid resource location " + updateURL + " for flowhandle "+fHandle.getName(), e);
    }
  }
}
