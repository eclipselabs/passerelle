/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.runtime.ws.rest;

import java.io.StringReader;
import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

@XmlRootElement(name="FlowHandle")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlowHandleResource implements FlowHandle {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(FlowHandleResource.class);
  
  private URI resourceLocation;
  private String code;
  private String rawFlowDefinition;
  private String version;
  @XmlTransient
  private VersionSpecification versionSpec;
  @XmlTransient
  private Flow flow;
  
  /**
   * Builds a REST resource wrapper around a given local handle,
   * copying all contents except the Flow.
   * @param handle
   * @return
   */
  public static FlowHandleResource buildFlowHandleResource(FlowHandle handle) {
    return new FlowHandleResource(handle.getResourceLocation(), handle.getCode(), handle.getRawFlowDefinition(), handle.getVersion());
  }
  
  /**
   * Builds a compacted REST resource wrapper around a given local handle,
   * copying all contents except the raw definition and the Flow.
   * @param handle
   * @return
   */
  public static FlowHandleResource buildCompactFlowHandleResource(FlowHandle handle) {
    return new FlowHandleResource(handle.getResourceLocation(), handle.getCode(), null, handle.getVersion());
  }
  
  public FlowHandleResource() {
  }
  
  public FlowHandleResource(URI resourceLocation, String code, String rawFlowDefinition, VersionSpecification versionSpec) {
    super();
    this.code = code;
    this.rawFlowDefinition = rawFlowDefinition;
    this.resourceLocation = resourceLocation;
    this.version = versionSpec.toString();
    this.versionSpec = versionSpec;
  }

  @Override
  public URI getResourceLocation() {
    return resourceLocation;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public VersionSpecification getVersion() {
    if(versionSpec==null && version!=null) {
      versionSpec = VersionSpecification.parse(version);
    }
    return versionSpec;
  }

  @Override
  public Flow getFlow() {
    if(flow==null && rawFlowDefinition!=null) {
      try {
        flow = FlowManager.readMoml(new StringReader(rawFlowDefinition));
      } catch (Exception e) {
        LOGGER.error(ErrorCode.FLOW_LOADING_ERROR.getFormattedCode()+" - Error parsing flow from raw definition for "+getCode(), e);
      }
    }
    return flow;
  }

  @Override
  public String getRawFlowDefinition() {
    return rawFlowDefinition;
  }

  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((resourceLocation == null) ? 0 : resourceLocation.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    FlowHandleResource other = (FlowHandleResource) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (resourceLocation == null) {
      if (other.resourceLocation != null)
        return false;
    } else if (!resourceLocation.equals(other.resourceLocation))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FlowHandleResource [resourceLocation=" + resourceLocation + ", code=" + code + ", version=" + version + ", rawFlowDefinition=" + rawFlowDefinition + "]";
  }
}
