/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.testsupport.utils;

import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import com.isencia.passerelle.actor.ProcessingException;

/**
 * An ExecutionListener that can be used to listen for an error of a specific class,
 * and to optionally stop the execution then.
 * 
 * @author erwin
 *
 */
public class ExecutionErrorCatcher implements ExecutionListener {
  public final static boolean STOP_ON_ERROR = true;
  
  private int errorCounter;
  private Class<?> errorClass;
  private boolean stopOnError;

  public ExecutionErrorCatcher(Class<?> errorClass) {
    this.errorClass = errorClass;
  }

  public ExecutionErrorCatcher(Class<?> errorClass, boolean stopOnError) {
    this.errorClass = errorClass;
    this.stopOnError = stopOnError;
  }

  public void managerStateChanged(Manager manager) {
  }

  public void executionFinished(Manager manager) {
  }

  public void executionError(Manager manager, Throwable throwable) {
    if (errorClass == null || errorClass.isInstance(throwable) || 
        ((throwable instanceof ProcessingException) && errorClass.isInstance(((ProcessingException) throwable).getCause()))) {
      errorCounter++;
      if(stopOnError) {
        manager.stop();
      }
    }
  }

  public int getErrorCount() {
    return errorCounter;
  }
}
