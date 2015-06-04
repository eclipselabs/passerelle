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
package com.isencia.passerelle.runtime.repos.impl.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.io.FileUtils;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

/**
 * @author erwin
 */
public class FlowHandleImpl implements FlowHandle {

  private String code;
  private File resourceLocation;
  private VersionSpecification version;
  private Flow flow;
  private String moml;

  public FlowHandleImpl(String code, File resourceLocation, VersionSpecification version) {
    this.code = code;
    this.resourceLocation = resourceLocation;
    this.version = version;
  }

  @Override
  public URI getResourceLocation() {
    return resourceLocation.toURI();
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
    if (flow == null) {
      try {
        flow = FlowManager.readMoml(getResourceLocation().toURL());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return flow;
  }

  @Override
  public String getRawFlowDefinition() {
    if (moml == null) {
      try {
        moml = FileUtils.readFileToString(resourceLocation);
      } catch (IOException e) {
      }
    }
    return moml;
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
    FlowHandleImpl other = (FlowHandleImpl) obj;
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
    return "FlowHandleImpl [code=" + code + ", resourceLocation=" + resourceLocation + ", version=" + version + "]";
  }
}
