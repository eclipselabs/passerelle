/* Copyright 2012 - iSencia Belgium NV

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

package com.isencia.passerelle.process.model;

/**
 * Base interface for all things that are identifiable with a unique primary key,
 * with a non-business-key, i.e. a simple generated number or so.
 * 
 * @author erwin
 * 
 */
public interface Identifiable {

  /**
   * @return the PK
   */
  Long getId();
}
