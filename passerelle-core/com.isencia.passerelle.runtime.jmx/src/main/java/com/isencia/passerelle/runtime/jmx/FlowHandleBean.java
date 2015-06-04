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
package com.isencia.passerelle.runtime.jmx;

import java.beans.ConstructorProperties;
import com.isencia.passerelle.runtime.FlowHandle;

public class FlowHandleBean {
  
  private String resourceLocation;
  private String code;
  private String rawFlowDefinition;
  private String version;
  
  /**
   * Builds a simplified bean around a given local handle, for use by a JMX facade,
   * copying all contents except the Flow.
   * @param handle
   * @return
   */
  public static FlowHandleBean buildFlowHandleBean(FlowHandle handle) {
    return new FlowHandleBean(handle.getResourceLocation().toString(), handle.getCode(), handle.getRawFlowDefinition(), handle.getVersion().toString());
  }
  
  /**
   * Builds a simplified bean around a given local handle, for use by a JMX facade,
   * copying all contents except the raw definition and the Flow.
   * @param handle
   * @return
   */
  public static FlowHandleBean buildCompactFlowHandleBean(FlowHandle handle) {
    return new FlowHandleBean(handle.getResourceLocation().toString(), handle.getCode(), null, handle.getVersion().toString());
  }
  
  public FlowHandleBean() {
  }
  
  @ConstructorProperties({"resourceLocation","code","rawFlowDefinition","version"})
  public FlowHandleBean(String resourceLocation, String code, String rawFlowDefinition, String versionSpec) {
    super();
    this.code = code;
    this.rawFlowDefinition = rawFlowDefinition;
    this.resourceLocation = resourceLocation;
    this.version = versionSpec;
  }

  public String getResourceLocation() {
    return resourceLocation;
  }

  public String getCode() {
    return code;
  }
  
  public String getVersion() {
    return version;
  }

  public String getRawFlowDefinition() {
    return rawFlowDefinition;
  }

  @Override
  public String toString() {
    return "FlowHandleBean [resourceLocation=" + resourceLocation + ", code=" + code + ", version=" + version + ", rawFlowDefinition=" + rawFlowDefinition + "]";
  }
}
