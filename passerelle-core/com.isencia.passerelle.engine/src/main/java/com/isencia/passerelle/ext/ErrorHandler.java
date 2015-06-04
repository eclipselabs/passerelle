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
package com.isencia.passerelle.ext;

import ptolemy.kernel.util.Nameable;
import com.isencia.passerelle.core.PasserelleException;

/**
 * ErrorHandlers can be used to implement dedicated handling/continuations
 * i.c.o. errors during loading/execution of a model.
 * <p>
 * Since v8.3 this interface is used as the basis to provide hierarchical error handling,
 * via corresponding actor implementations.
 * </p>
 * <p>
 * Implementations will typically implement some kind of filtering on the presented errors.
 * If a handler is not willing/capable to handle a given error, the engine will try to offer the error to other handlers,
 * in a hierarchical way, i.e. starting from a sub-model-level upwards to parent levels up-to the top level.
 * </p>
 * 
 * @author erwin
 *
 */
public interface ErrorHandler {
  
  /**
   * 
   * @param errorSource the modelElement that generated the error
   * @param error the one to be handled
   * @return true if this handler is taking ownership of the actual error handling.
   * False if the handler does not want to handle it.
   */
  boolean handleError(Nameable errorSource, PasserelleException error);

}
