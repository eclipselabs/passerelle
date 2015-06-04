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
package com.isencia.passerelle.message.internal;

import javax.mail.Multipart;

import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;


/**
 * SettableMessage
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public interface SettableMessage extends ManagedMessage {

	PasserelleBodyPart getBody();
	void setBody(PasserelleBodyPart body);
	void setBodyContent(Multipart body) throws MessageException;

  void addHeader(String name, String value);
  void setHeader(String name, String value);
  void removeHeader(String name);
  void saveChanges() throws MessageException;

}
