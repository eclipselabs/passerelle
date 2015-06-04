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
package com.isencia.passerelle.executor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ptolemy.actor.Manager;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;

/**
 * <tt>ExecutionContext</tt> is a configurable container for
 * execution context information. The information is stored
 * as name/value entries.
 * 
 * @author erwin
 */
public class ExecutionContext {
	public final static String EXECUTION_CTXT_ATTR="com.isencia.passerelle.execution.ctxt";

	private Map attributes = new HashMap();
	
	private ExecutionContext() {
		super();
	}
	
	public static ExecutionContext getExecutionContext(Manager manager) {
		if(manager==null)
			return null;
		
		ExecutionContext ctxt = null;
		
		try {
			Parameter param = (Parameter) manager.getAttribute(EXECUTION_CTXT_ATTR);
			if(param!=null) {
				ObjectToken ctxtToken = (ObjectToken) param.getToken();
				ctxt = (ExecutionContext) ctxtToken.getValue();
			} else {
				// register a clean context on the manager
				ctxt = new ExecutionContext();
				new Parameter(manager,EXECUTION_CTXT_ATTR,new ObjectToken(ctxt));
			}
		} catch (Exception e) {
			ctxt = null;
		}
		return ctxt;
	}

	public void setAttribute(String attrName, Object attrValue) {
		attributes.put(attrName, attrValue);
	}
	
	public Object getAttribute(String attrName) {
		return attributes.get(attrName);
	}
	
	/**
	 * 
	 * @param visitor
	 */
	public void acceptVisitor(ContextVisitor visitor) {
		if(visitor!=null) {
			for (Iterator attrItr = attributes.entrySet().iterator();attrItr.hasNext();) {
				Entry attrEntry = (Entry) attrItr.next();
				visitor.accept((String)attrEntry.getKey(), attrEntry.getValue());
			}
		}
	}
}
