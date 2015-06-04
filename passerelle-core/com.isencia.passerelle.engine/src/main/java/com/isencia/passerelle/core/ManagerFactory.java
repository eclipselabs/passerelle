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
package com.isencia.passerelle.core;

import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

/**
 * ManagerFactory serves to encapsulate the exact creation rules,
 * and implementation class used, to obtain a Passerelle-enhanced
 * model-execution manager (cfr Ptolemy Manager).
 * 
 * ALL places where models need to be started/monitored/... 
 * via the Ptolemy Manager API should make sure they use
 * this factory to obtain a Manager object.
 * 
 * @author erwin
 */
public class ManagerFactory {
	
	private final static ManagerFactory instance = new ManagerFactory();
	
	private ManagerFactory() {
	}
	
	public final static ManagerFactory getInstance() {
		return instance;
	}
	
	/**
	 * 
	 * @param ws
	 * @param name
	 * @return
	 * @throws PasserelleException
	 */
	public Manager createManager(Workspace ws, String name) throws PasserelleException {
		try {
			return new com.isencia.passerelle.core.Manager(ws, name);
		} catch (IllegalActionException e) {
			throw new PasserelleException(ErrorCode.ERROR, "Could not create a new Manager " + name, e);
		}
	}
}
