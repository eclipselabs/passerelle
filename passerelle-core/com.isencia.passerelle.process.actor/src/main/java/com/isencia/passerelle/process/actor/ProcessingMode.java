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
package com.isencia.passerelle.process.actor;

/**
 * Identifies whether the actor will process a given ProcessRequest synchronously (all work done when the <code>process()</code> method returns) 
 * or asynchronously (work done in background and will probably take longer than the return of the <code>process()</code> method invocation).
 */
public enum ProcessingMode {
  SYNCHRONOUS, ASYNCHRONOUS;
}