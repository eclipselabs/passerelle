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

import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;

/**
 * @version 1.0
 * @author edeley
 */
public class PasserelleBodyPart extends MimeBodyPart {

	/**
	 * Constructor for PasserelleBodyPart.
	 */
	public PasserelleBodyPart() {
		super();
	}

	/**
	 * Constructor for PasserelleBodyPart.
	 * @param arg0
	 * @throws MessagingException
	 */
	public PasserelleBodyPart(InputStream arg0) throws MessagingException {
		super(arg0);
	}

	/**
	 * Constructor for PasserelleBodyPart.
	 * @param arg0
	 * @param arg1
	 * @throws MessagingException
	 */
	public PasserelleBodyPart(InternetHeaders arg0, byte[] arg1)
		throws MessagingException {
		super(arg0, arg1);
	}

	/** 
	 * Trick to avoid necessity for using javax.mail.Message
	 * to ensure that headers get set correctly after building a bodypart.
	 * 
	 * @throws MessagingException
	 */
	public void saveChanges() throws MessagingException {
		updateHeaders();
	}
}

