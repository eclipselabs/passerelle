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
package com.isencia.passerelle.actor.general;

import java.io.PrintStream;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * @author Wim
 */
public class ErrorConsole extends Console {
	private static final long serialVersionUID = -6530337774933942033L;

	/**
   * @param container
   * @param name
   * @throws ptolemy.kernel.util.NameDuplicationException
   * @throws ptolemy.kernel.util.IllegalActionException
   */
  public ErrorConsole(CompositeEntity container, String name) throws NameDuplicationException, IllegalActionException {
    super(container, name);
  }

  protected PrintStream getConsole() {
    return System.err;
  }
}
