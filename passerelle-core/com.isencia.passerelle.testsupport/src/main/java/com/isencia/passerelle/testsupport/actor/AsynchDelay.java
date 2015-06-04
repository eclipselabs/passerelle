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
package com.isencia.passerelle.testsupport.actor;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;

@SuppressWarnings("serial")
public class AsynchDelay extends Delay {

  public AsynchDelay(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
  }

  @Override
  protected void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    new Thread(new DelayedSender(response)).start();
  }
  
  @Override
  protected ProcessingMode getProcessingMode(ActorContext ctxt, ProcessRequest request) {
    return ProcessingMode.ASYNCHRONOUS;
  }

  private class DelayedSender implements Runnable {
    private ProcessResponse response;

    public DelayedSender(ProcessResponse response) {
      this.response = response;
    }

    public void run() {
      try {
        AsynchDelay.super.process(response.getContext(), response.getRequest(), response);
      } catch (ProcessingException e) {
        response.setException(e);
      } finally {
        AsynchDelay.this.processFinished(response.getContext(), response.getRequest(), response);
      }
    }
  }
}
