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
package com.isencia.message.net.requestreply;

import java.io.IOException;
import java.net.ServerSocket;
import com.isencia.message.ChannelException;
import com.isencia.message.NoMoreMessagesException;
import com.isencia.message.extractor.EndOfMsgCharMsgExtractor;
import com.isencia.message.generator.MessageTextLineGenerator;
import com.isencia.message.net.requestreply.SocketServerRequestReplier;
import com.isencia.message.requestreply.IMessage;

/**
 * @todo Class comment
 * @author erwin
 */
public class EchoSocketServer {

  public static void main(String[] args) {
    try {
      SocketServerRequestReplier testSvr = new SocketServerRequestReplier(new ServerSocket(3333), new EndOfMsgCharMsgExtractor(),
      // new TextLineMessageExtractor(),
          new MessageTextLineGenerator(String.valueOf('\u001C')));
      // new MessageTextLineGenerator());
      testSvr.open();
      while (true) {
        IMessage req = testSvr.receiveRequest();
        testSvr.sendResponse(req.getMessage(), req.getCorrelationID());
        if ("Q".equals(req.getMessage())) {
          break;
        }
      }
      testSvr.close();
    } catch (ChannelException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoMoreMessagesException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
