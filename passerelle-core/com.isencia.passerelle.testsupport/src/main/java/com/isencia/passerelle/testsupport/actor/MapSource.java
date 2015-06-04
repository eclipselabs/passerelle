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
package com.isencia.passerelle.testsupport.actor;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.actor.v5.ActorContext;
import com.isencia.passerelle.actor.v5.ProcessRequest;
import com.isencia.passerelle.actor.v5.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.core.PortFactory;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * A test source actor that creates a message with a Map<String, String>, prefilled with values as configured using a plain Java properties format.
 * 
 * @author erwin
 */
public class MapSource extends Actor {
  private static final long serialVersionUID = 1L;

  public StringParameter mapEntriesParameter; // NOSONAR
  public Port output; // NOSONAR

  public MapSource(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    output = PortFactory.getInstance().createOutputPort(this);

    mapEntriesParameter = new StringParameter(this, "entries");
    new TextStyle(mapEntriesParameter, "paramsTextArea");
  }

  @Override
  public void process(ActorContext ctxt, ProcessRequest request, ProcessResponse response) throws ProcessingException {
    ManagedMessage message = createMessage();
    try {
      Map<String, String> msgMap = new HashMap<String, String>();
      message.setBodyContent(msgMap, ManagedMessage.objectContentType);
      String paramDefs = ((StringToken) mapEntriesParameter.getToken()).stringValue();
      BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
      String paramDef = null;
      while ((paramDef = reader.readLine()) != null) {
        String[] paramKeyValue = paramDef.split("=");
        msgMap.put(paramKeyValue[0], paramKeyValue[1]);
      }
      response.addOutputMessage(output, message);
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.MSG_CONSTRUCTION_ERROR, "Error constructing message from entries parameter", mapEntriesParameter, e);
    } finally {
      requestFinish();
    }
  }
}
