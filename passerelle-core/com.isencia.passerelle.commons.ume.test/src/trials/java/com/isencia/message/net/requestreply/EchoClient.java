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

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import com.isencia.message.extractor.EndOfMsgCharMsgExtractor;
import com.isencia.message.extractor.IMessageExtractor;

/**
 * @todo Class comment
 * @author erwin
 */
public class EchoClient {
  final static char RECORD_SEPARATOR = '\n';
  final static char FIELD_SEPARATOR = '\t';
  final static char EOF = '\u001C';

  public static void main(String[] args) {

    Socket s = null;
    Writer msgWriter = null;
    Reader msgReader = null;
    try {
      s = new Socket("localhost", 3333);
      msgWriter = new OutputStreamWriter(s.getOutputStream(), "ISO-8859-1");
      msgReader = new InputStreamReader(s.getInputStream());

      // send msg with linefeed
      for (int i = 0; i < 5; ++i) {
        msgWriter.write("Hello" + i);
        msgWriter.write(RECORD_SEPARATOR);
        msgWriter.write(EOF);
        msgWriter.flush();
      }

      // read echoes
      for (int i = 0; i < 5; ++i) {
        // IMessageExtractor extractor = new TextLineMessageExtractor();
        IMessageExtractor extractor = new EndOfMsgCharMsgExtractor();
        extractor.open(msgReader);
        System.out.println("Received echo msg : " + extractor.getMessage());
      }

      // terminate
      msgWriter.write("Q");
      msgWriter.write(RECORD_SEPARATOR);
      msgWriter.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (s != null) try {
        s.close();
      } catch (Exception e) {
      }
    }

  }
}
