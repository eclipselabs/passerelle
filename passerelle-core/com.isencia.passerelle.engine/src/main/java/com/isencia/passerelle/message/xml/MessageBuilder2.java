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
package com.isencia.passerelle.message.xml;


import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.thoughtworks.xstream.XStream;

/**
 * MessageBuilder2
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public final class MessageBuilder2 {
	
	private final static XStream xmlStreamer = new XStream();

	public static String buildToXML(ManagedMessage message){
		return xmlStreamer.toXML(message);
	}
	
	public static ManagedMessage buildFromXML(String msgXML) throws MessageException {
		ManagedMessage result = (ManagedMessage) xmlStreamer.fromXML(msgXML);
		
		if(result==null) {
			// try old xml builder, might be an xml coming from an old passerelle file
			result = MessageBuilder.buildFromXML(msgXML);
		}
		return result;
	}
}
