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

import com.isencia.passerelle.util.ptolemy.PortParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * PortFactory
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class PortFactory {
	private final static PortFactory instance = new PortFactory();
	
	public static PortFactory getInstance() {
		return instance;
	}
	
	private PortFactory() {
	}

	/**
	 * @param container
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public Port createOutputPort(Entity container) throws IllegalActionException, NameDuplicationException {
		return createOutputPort(container,"output");
	}
	public Port createOutputPort(Entity container, String name) throws IllegalActionException, NameDuplicationException {
		Port res = new Port(container,name,false,true);
		return res;
	}
	/**
	 * @param container
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public ErrorPort createOutputErrorPort(Entity container) throws IllegalActionException, NameDuplicationException {
		return createOutputErrorPort(container,"error");
	}
	public ControlPort createInputControlPort(Entity container, String name) throws IllegalActionException, NameDuplicationException {
		ControlPort res = new ControlPort(container,name,true,false);
		return res;
	}
	public ControlPort createOutputControlPort(Entity container, String name) throws IllegalActionException, NameDuplicationException {
		ControlPort res = new ControlPort(container,name,false,true);
		return res;
	}
	public ErrorPort createInputErrorPort(Entity container, String name) throws IllegalActionException, NameDuplicationException {
		ErrorPort res = new ErrorPort(container,name,true,false);
		return res;
	}
	public ErrorPort createOutputErrorPort(Entity container, String name) throws IllegalActionException, NameDuplicationException {
		ErrorPort res = new ErrorPort(container,name,false,true);
		return res;
	}

	/**
	 * @param container
	 * @param expectedContentType optionally specify the expected body content type
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public Port createInputPort(Entity container, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		return createInputPort(container,"input", PortMode.PULL, expectedContentType);
	}
	 /**
	  * 
	  * @param container
	  * @param name
	  * @param expectedContentType optionally specify the expected body content type
	  * @return
	  * @throws IllegalActionException
	  * @throws NameDuplicationException
	  */
	public Port createInputPort(Entity container, String name, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		return createInputPort(container, name, PortMode.PULL, expectedContentType);
	}
	/**
	 * @param container
	 * @param mode
	 * @param expectedContentType optionally specify the expected body content type
	 * @return
	 * @throws NameDuplicationException 
	 * @throws IllegalActionException 
	 */
	public Port createInputPort(Entity container, PortMode mode, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		return createInputPort(container, "input", mode, expectedContentType);
	}
	
	public PortParameter createPortParameter(Entity container, String name, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
	  PortParameter res = new PortParameter(container,name);
	  // mark it as an agnostic port
	  res.getPort().setMode(PortMode.AGNOSTIC);
    res.getPort().setExpectedMessageContentType(expectedContentType);
    return res;
	}
	 /**
	  * 
	  * @param container
	  * @param name
	  * @param mode
	  * @param expectedContentType optionally specify the expected body content type
	  * @return
	  * @throws IllegalActionException
	  * @throws NameDuplicationException
	  */
	public Port createInputPort(Entity container, String name, PortMode mode, Class expectedContentType) throws IllegalActionException, NameDuplicationException {
		Port res = new Port(container,name,mode,true,false);
		res.setExpectedMessageContentType(expectedContentType);
		return res;
	}
}
