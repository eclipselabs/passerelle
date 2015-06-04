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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.util.MapOfLists;


/**
 * ErrorMessageListContainer
 * 
 * a message container dedicated for transporting a collection of error infos
 * 
 * @author erwin
 */
public class ErrorMessageListContainer extends MessageContainer {
	private static Logger logger = LoggerFactory.getLogger(ErrorMessageListContainer.class);
	private List errorMessages = new ArrayList();

	/**
	 * 
	 */
	public ErrorMessageListContainer() {
		try {
			setBodyContent(errorMessages,ManagedMessage.objectContentType);
		} catch (MessageException ex) {
			logger.error("",ex);
		}
	}

	public void addErrorMessage(ErrorMessageContainer errMsg) {
		if(errMsg!=null) {
			errorMessages.add(errMsg);
		}
	}
	
	public boolean removeErrorMessage(ErrorMessageContainer errMsg) {
		if(errMsg!=null) {
			return errorMessages.remove(errMsg);
		} else {
			return false;
		}
	}
	
	public int size() {
		return errorMessages.size();
	}

    public MessageContainer copy() throws MessageException {
        ErrorMessageListContainer res = new ErrorMessageListContainer();
        for (Iterator iter = errorMessages.iterator(); iter.hasNext();) {
            ErrorMessageContainer msg = (ErrorMessageContainer) iter.next();
            res.addErrorMessage((ErrorMessageContainer)msg.copy());
        }
        res.headers = ((MapOfLists)this.headers).copy();
        return res;
    }


    public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[ErrorMessageListContainer:");
            buffer.append(" errorMessages: ");
            buffer.append(errorMessages);
            buffer.append("]");
            return buffer.toString();
        }
    
    
}
