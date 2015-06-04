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
/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package com.isencia.util.commandline;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the <code>Commandline</code> class to provide a means to manipulate
 * the OS environment under which the command will run.
 * 
 * @author <a href="mailto:rjmpsmith@hotmail.com">Robert J. Smith</a>
 */
public class EnvCommandline extends Commandline {

  private static final Logger LOG = LoggerFactory.getLogger(EnvCommandline.class);

  /**
   * Provides the OS environment under which the command will run.
   */
  private OSEnvironment env = new OSEnvironment();

  /**
   * Constructor which takes a command line string and attempts to parse it into
   * it's various components.
   * 
   * @param command The command
   */
  public EnvCommandline(String command) {
    super(command);
  }

  /**
   * Default constructor
   */
  public EnvCommandline() {
    super();
  }

  /**
   * Sets a variable within the environment under which the command will be run.
   * 
   * @param var The environment variable to set
   * @param value The value of the variable
   */
  public void setVariable(String var, String value) {
    env.add(var, value);
  }

  /**
   * Gets the value of an environment variable. The variable name is case
   * sensitive.
   * 
   * @param var The variable for which you wish the value
   * @return The value of the variable, or <code>null</code> if not found
   */
  public String getVariable(String var) {
    return env.getVariable(var);
  }

  /**
   * Executes the command.
   */
  public Process execute() throws IOException {
    Process process;

    // Let the user know what's happening
    File workingDir = getWorkingDir();
    if (workingDir == null) {
      LOG.debug("Executing \"" + this + "\"");
      process = Runtime.getRuntime().exec(getCommandline(), env.toArray());
    } else {
      LOG.debug("Executing \"" + this + "\" in directory " + workingDir.getAbsolutePath());
      process = Runtime.getRuntime().exec(getCommandline(), env.toArray(), workingDir);
    }

    return process;
  }
}
