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

import java.util.Collection;

/**
 * A Task is a kind of internal request, to execute one step in a process.
 * <p>
 * Contrary to a plain Request, which is just a plain container for storing the info
 * which triggered the process, Tasks (may) have associated results represented in blocks of key/value items.
 * </p>
 * @author delerw
 *
 */
public interface Task extends Request {
  /**
   * Header name where an individual scope name is stored in messages travelling between actors that are inside a
   * context scope, e.g. inside a Fork/Splitter-...->Join construct, on a specific branch.
   */
  String HEADER_CTXT_SCOPE = "__PSRL_CTXT_SCOPE";
  /**
   * Header name where a scope group is stored in messages travelling between actors that are inside a context scope,
   * e.g. inside a Fork/Splitter-...->Join construct.
   */
  String HEADER_CTXT_SCOPE_GRP = "__PSRL_CTXT_SCOPE_GRP";

  /**
   * 
   * @return the <code>Context</code> of the parent request of this task
   */
	Context getParentContext();
	
	/**
	 * @return the results that have been gathered by this task
	 */
	Collection<ResultBlock> getResultBlocks();
	
	ResultBlock getResultBlock(String type);
}
