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
package com.isencia.passerelle.util;

import org.slf4j.MDC;
import com.isencia.passerelle.executor.ContextVisitor;
import com.isencia.passerelle.executor.ExecutionContext;


/**
 * Utility to manage extra info in logging, based on execution context
 * and other attributes.
 * 
 * Based on Log4J's NDC and MDC.
 * 
 * @author erwin
 */
public class LoggerManager {
//	private static final String MDC_DELIM = " - ";
	
	private static ContextVisitor contextSetter = new ContextVisitor() {
		public void accept(String name, Object value) {
			MDC.put(name,value.toString());
		}
	};

	private static ContextVisitor contextClearer = new ContextVisitor() {
		public void accept(String name, Object value) {
			MDC.remove(name);
		}
	};

	/**
	 * A wrapper around a nested diagnostic context mechanism.
	 * Currently coupled to the Log4J implementation
	 * @param context to be nested inside the current one
	 */
	public static void pushNDC(String context) {
		//NDC.push(context);		// TODO Chec if still needed
	}

	/**
	 * A wrapper around a nested diagnostic context mechanism.
	 * Currently coupled to the Log4J implementation.
	 * @return the current level of diagnostic context
	 */	
	public static String popNDC() {
		//return NDC.pop();	
		return "";					// TODO Chec if still needed
	}

	/**
	 * A wrapper around a mapped diagnostic context mechanism.
	 * Remark that MDC is not correctly supported on WAS 5!
	 * 
	 * @param key
	 * @param context
	 */	
	public static void pushMDC(String key, String context) {
		// MDC doesn't function correctly on WAS 5
		MDC.put(key,context);
		
//		String currentContext = NDC.pop();
//		if(currentContext!=null && currentContext.length()>0)
//			currentContext = MDC_DELIM + currentContext;
//		else
//			currentContext = "";
//			
//		NDC.push(context+currentContext);
	}

	/**
	 * A wrapper around a mapped diagnostic context mechanism.
	 * Currently coupled to the Log4J implementation of NDC,
	 * as MDC is not correctly supported on WAS 5.
	 * 
	 * @param key
	 * @return the current context with that key
	 */	
	public static String popMDC(String key) {
		// MDC doesn't function correctly on WAS 5
		String value = (String) MDC.get(key);
		MDC.remove(key);
		
//		String context = NDC.pop();
//		String value = context;
//		if(context!=null && context.length()>0) {
//			int delimIndex = context.indexOf(MDC_DELIM);
//			if(delimIndex>0) {
//				value = context.substring(0,delimIndex);
//				context = context.substring(delimIndex+MDC_DELIM.length());
//				NDC.push(context);
//			}
//		}
		return value;
	}
	
	public static void setContext(ExecutionContext context) {
		if(context!=null)
			context.acceptVisitor(contextSetter);
	}
	
	public static void clearContext(ExecutionContext context) {
		if(context!=null)
			context.acceptVisitor(contextClearer);
	}
}