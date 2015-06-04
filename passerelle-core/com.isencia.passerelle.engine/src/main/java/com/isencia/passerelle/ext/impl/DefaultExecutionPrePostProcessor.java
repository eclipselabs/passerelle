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

package com.isencia.passerelle.ext.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.ext.ExecutionPrePostProcessor;


/**
 * Default implementation, providing no pre-/post-processing whatsoever.
 * 
 * If a "real" implementation is needed, it has to be registered with the
 * Passerelle Director.setExecutionPrePostProcessor(ExecutionPrePostProcessor) method.
 * 
 * @author erwin
 *
 */
public class DefaultExecutionPrePostProcessor implements
		ExecutionPrePostProcessor {

	private Logger logger = LoggerFactory.getLogger(DefaultExecutionPrePostProcessor.class);

	public void postProcess() {
		if(logger.isTraceEnabled())
			logger.trace("postProcess() - entry");
		
		if(logger.isTraceEnabled())
			logger.trace("postProcess() - exit");
	}

	public void preProcess() {
		if(logger.isTraceEnabled())
			logger.trace("preProcess() - entry");
		
		if(logger.isTraceEnabled())
			logger.trace("preProcess() - exit");
	}

}
