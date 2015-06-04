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
package com.isencia.passerelle.runtime;

import java.io.Serializable;
import java.net.URI;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

/**
 * A light-weight handle on Passerelle Flows.
 * <p>
 * It offers direct access to some important metadata-properties for Flows,
 * and can be used as a means to identify Flows and to work with them
 * without needing to load/parse/build the actual Flow instance.
 * </p>
 * @author erwin
 *
 */
public interface FlowHandle extends Serializable {
  
  /**
   * 
   * @return the URI where the Flow handle/resource can be retrieved
   */
  URI getResourceLocation();

  /**
   * @return the unique code identifying the Flow(Handle) in its repository
   */
  String getCode();
  
  /**
   * @return the version of this handle's Flow in the repository
   */
  VersionSpecification getVersion();
  
  /**
   * 
   * @return the Flow behind this handle. 
   * This may be a slow/heavy operation as it may involve reading the definition from a remote repository,
   * parsing it and constructing all its model elements.
   */
  Flow getFlow();

  /**
   * @return the flow definition in its raw format, typically a MOML/XML.
   * This may be empty for "compacted" handles, in which case the full contents can
   * be explicitly loaded via FlowRepositoryService.loadFlowHandleWithContent.
   */
  String getRawFlowDefinition();
}
