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
package com.isencia.passerelle.runtime.jmx.server;

import java.net.URI;
import java.net.URISyntaxException;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.jmx.FlowHandleBean;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

/**
 * @author erwin
 */
public class FlowHandleImpl implements FlowHandle {
  
  public static FlowHandleImpl buildFlowHandle(FlowHandleBean fhb) throws URISyntaxException {
    return new FlowHandleImpl(fhb.getCode(), new URI(fhb.getResourceLocation()), VersionSpecification.parse(fhb.getVersion()));
  }

  private String code;
  private URI resourceLocation;
  private VersionSpecification version;

  public FlowHandleImpl(String code, URI resourceLocation, VersionSpecification version) {
    this.code = code;
    this.resourceLocation = resourceLocation;
    this.version = version;
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
    return version;
  }

  @Override
  public Flow getFlow() {
    throw new UnsupportedOperationException("getFlow()");
  }

  @Override
  public String getRawFlowDefinition() {
    throw new UnsupportedOperationException("getRawFlowDefinition()");
  }

  @Override
  public String toString() {
    return "FlowHandleImpl [code=" + code + ", resourceLocation=" + resourceLocation + ", version=" + version + "]";
  }
}
