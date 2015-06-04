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
package com.isencia.passerelle.actor.net;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.message.generator.IMessageGenerator;
import com.isencia.message.generator.MessageTextLineGenerator;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.OptionsFactory;

/**
 * Options factory for socket sender actors
 * 
 * @author erwin
 */
public class SocketCltSndOptionsFactory extends OptionsFactory {
  private static final long serialVersionUID = 1L;
  private static final String FS_CHAR = "FS msg delimiter char";
  // private static final String CONNECTION_CLOSED = "Connection closed";
  private static final String LINEFEED = "Linefeed";

  private static final IMessageGenerator FS_CHAR_GENERATOR = new MessageTextLineGenerator(String.valueOf('\u001C'));
  private static final IMessageGenerator LINEFEED_GENERATOR = new MessageTextLineGenerator();

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public SocketCltSndOptionsFactory(Actor container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    initializeOptions();
  }

  protected void initializeOptions() {
    addOption(SocketServerReceiver.MSG_EXTRACTOR_PARAM_NAME, FS_CHAR, FS_CHAR_GENERATOR);
    Option o = addOption(SocketServerReceiver.MSG_EXTRACTOR_PARAM_NAME, LINEFEED, LINEFEED_GENERATOR);
    setDefaultOption(SocketServerReceiver.MSG_EXTRACTOR_PARAM_NAME, o);
  }

}
