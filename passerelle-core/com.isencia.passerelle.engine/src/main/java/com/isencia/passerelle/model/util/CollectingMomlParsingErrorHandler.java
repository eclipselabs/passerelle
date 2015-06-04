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
package com.isencia.passerelle.model.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.ErrorHandler;

/**
 * Utility to collect all parsing errors during the parsing of a moml.
 * 
 * @author erwin
 *
 */
public class CollectingMomlParsingErrorHandler implements ErrorHandler, 
			Iterable<CollectingMomlParsingErrorHandler.ErrorItem> {

	public static class ErrorItem {
		public String element;
		public NamedObj context;
		public Throwable exception;
		public ErrorItem(String element, NamedObj context, Throwable exception) {
			this.element = element;
			this.context = context;
			this.exception = exception;
		}
	}
	
	private List<ErrorItem> errorItems = new ArrayList<ErrorItem>();

	public void enableErrorSkipping(boolean enable) {
	}

	public int handleError(String element, NamedObj context,
			Throwable exception) {
		errorItems.add(new ErrorItem(element,context,exception));
		return CONTINUE;
	}
	
	public boolean hasErrors() {
		return !errorItems.isEmpty();
	}

	public Iterator<ErrorItem> iterator() {
		return errorItems.iterator();
	}
}
