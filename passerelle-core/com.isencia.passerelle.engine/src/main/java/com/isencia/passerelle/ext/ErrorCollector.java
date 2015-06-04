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
package com.isencia.passerelle.ext;

import com.isencia.passerelle.core.PasserelleException;


/**
 * One or more ErrorCollectors may be registered with a model's director.
 * Any NON_FATAL exception thrown from inside an actor who's error port is not connected,
 * is reported to the director, who will forward it to all registered collectors.
 * <b>
 * This can be used e.g. to implement an actor that receives all NON_FATAL exceptions,
 * without needing any connections in the model.
 * 
 * @author erwin
 */
public interface ErrorCollector {

	/**
	 * @param e
	 */
	void acceptError(PasserelleException e);

}
