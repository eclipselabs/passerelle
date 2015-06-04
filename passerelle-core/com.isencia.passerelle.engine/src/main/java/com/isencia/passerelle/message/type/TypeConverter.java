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
package com.isencia.passerelle.message.type;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;

/**
 * 
 * The contract for specific convertors
 * between a certain ptolemy token type
 * and passerelle message tokens.
 * 
 * @author erwin
 */
public interface TypeConverter {
	/**
	 * Convert a token containing a passerelle message into
	 * a token with the given type.
	 * Checks if the conversion is supported by the given converter.
	 * 
	 * @param passerelleMsgToken
	 * @param targetType the expected Ptolemy token type
	 * @return a token with type and content matching the converter
	 * @throws UnsupportedOperationException thrown when the convertor can not handle 
	 * the conversion to the requested type
	 * @throws PasserelleException
	 */
	Token convertPasserelleTokenToPtolemyToken(PasserelleToken passerelleMsgToken, Type targetType) throws UnsupportedOperationException, PasserelleException;
	
	/**
	 * Convert a Ptolemy token into a Passerelle token with a corresponding 
	 * passerelle message with body content with the expected content type.
	 * Checks if the conversion is supported by the given converter.
	 * 
	 * @param origToken
	 * @param targetContentType the expected type of the Passerelle msg body content
	 * @return a Passerelle token containing a Passerelle msg
	 * @throws UnsupportedOperationException thrown when the convertor can not handle 
	 * the conversion to the requested type
	 * @throws PasserelleException
	 */
	PasserelleToken convertPtolemyTokenToPasserelleToken (Token origToken, Class targetContentType)  throws UnsupportedOperationException, PasserelleException;
	
	/**
	 * Convert the contents of a Passerelle msg to a requested type, and return a new Passerelle token
	 * with the converted message in it.
	 * Checks if the conversion is supported by the given converter.
	 * 
	 * @param origToken
	 * @param targetContentType the expected type of the resulting Passerelle msg body content
	 * @return a Passerelle token containing a Passerelle msg with converted content type
	 * @throws UnsupportedOperationException thrown when the convertor can not handle 
	 * the conversion to the requested type
	 * @throws PasserelleException
	 */
	PasserelleToken convertPasserelleMessageContent (PasserelleToken origToken, Class targetContentType)  throws UnsupportedOperationException, PasserelleException;
}
