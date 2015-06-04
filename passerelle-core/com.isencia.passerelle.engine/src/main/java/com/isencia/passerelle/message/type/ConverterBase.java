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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.core.PasserelleType;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.passerelle.message.MessageFactory;

/**
 * @author erwin
 */
public abstract class ConverterBase implements TypeConverter {
    static Logger logger = LoggerFactory.getLogger(ConverterBase.class);

	public Token convertPasserelleTokenToPtolemyToken(PasserelleToken passerelleMsgToken, Type targetType) 
			throws UnsupportedOperationException, PasserelleException {
		
		if(passerelleMsgToken==null)
			return null;
		else {
			try {
				if(PasserelleType.PASSERELLE_MSG_TYPE.equals(targetType)) {
					// no conversion needed
					return passerelleMsgToken;
				} else if(areTypesCompatible(passerelleMsgToken.getMessageContentType(), targetType)) {
					Token result = null;
					try {
						ManagedMessage passerelleMsg = passerelleMsgToken.getMessage();
						result = convertContentToToken(passerelleMsg.getBodyContent(), targetType);
					} catch (MessageException e) {
						throw new PasserelleException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error converting to "+targetType + " from " + passerelleMsgToken, e);
					}
					return result;
				} else {
					throw new UnsupportedOperationException();
				}
			} catch(NullPointerException e) {
				return passerelleMsgToken;
			}
		}
	}

	public PasserelleToken convertPtolemyTokenToPasserelleToken(Token origToken, Class targetContentType) 
			throws UnsupportedOperationException, PasserelleException {
		if(origToken==null)
			return null;
		else if(PasserelleToken.class.isInstance(origToken)) {
			return convertPasserelleMessageContent((PasserelleToken)origToken, targetContentType);
		} else {
			if(areTypesCompatible(origToken.getType(), targetContentType)) {
				PasserelleToken result = null;
				try {
					Object content = convertTokenToContent(origToken, targetContentType);
					ManagedMessage msg = MessageFactory.getInstance().createMessage();
					if(String.class.isInstance(content))
						// TODO need to think about this a bit.
						// Is this acceptable if we would want to use specific String subclasses?
						// Or should we then also put the mime type to ManagedMessage.objectContentType?
						msg.setBodyContentPlainText((String)content);
					else
						msg.setBodyContent(content, ManagedMessage.objectContentType);
					result = new PasserelleToken(msg);
				} catch (MessageException e) {
					logger.error("",e);
				} 
				return result;
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	public PasserelleToken convertPasserelleMessageContent(PasserelleToken origToken, Class targetContentType) 
			throws UnsupportedOperationException, PasserelleException {
		
		if(origToken==null || origToken.getMessage()==null || origToken.getMessage().getBodyContent()==null){
			return origToken;
		} else if(areTypesCompatible(origToken.getMessageContentType(),targetContentType)){
			PasserelleToken result = origToken;
			try {
				Object content = origToken.getMessage().getBodyContent();
				if(content!=null && !content.getClass().equals(targetContentType)) {
					content = convertContentToType(content, targetContentType);
					ManagedMessage msg = MessageFactory.getInstance().copyMessage(origToken.getMessage());
					if(String.class.isInstance(content))
						// TODO need to think about this a bit.
						// Is this acceptable if we would want to use specific String subclasses?
						// Or should we then also put the mime type to ManagedMessage.objectContentType?
						msg.setBodyContentPlainText((String)content);
					else
						msg.setBodyContent(content, ManagedMessage.objectContentType);
					result = new PasserelleToken(msg);
				}
			} catch (MessageException e) {
				logger.error("",e);
			} 
			return result;
		} else {
			throw new UnsupportedOperationException();
		}
	}


	/**
	 * Check if this converter is able to convert between these two types.
	 * 
	 * @param fromType
	 * @param toType
	 * @return
	 */
	abstract protected boolean areTypesCompatible(Class fromType, Type toType);
	
	/**
	 * Check if this converter is able to convert between these two types.
	 * 
	 * @param fromType
	 * @param toType
	 * @return
	 */
	abstract protected boolean areTypesCompatible(Type fromType, Class toType);
	
	/**
	 * Check if this converter is able to convert between these two types.
	 * 
	 * @param fromType
	 * @param toType
	 * @return
	 */
	abstract protected boolean areTypesCompatible(Class fromType, Class toType);
	
	/**
	 * @param content
	 * @param targetType
	 * @return
	 */
	abstract protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException;

	/**
	 * @param typedToken
	 * @param targetType
	 * @return
	 */
	protected abstract Object convertTokenToContent(Token typedToken, Class targetType) throws MessageException;

	/**
	 * @param content
	 * @param targetType
	 * @return
	 */
	abstract protected Object convertContentToType(Object content, Class targetType) throws UnsupportedOperationException, MessageException;

}
