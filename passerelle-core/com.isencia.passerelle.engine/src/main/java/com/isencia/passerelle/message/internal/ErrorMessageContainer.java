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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageException;
import com.isencia.util.MapOfLists;


/**
 * ErrorMessageContainer
 * 
 * a message container dedicated for transporting errors
 * 
 * @author erwin
 */
public class ErrorMessageContainer extends MessageContainer {
	private static Logger logger = LoggerFactory.getLogger(ErrorMessageContainer.class);

	/**
	 * 
	 * @param message
	 * @param context
	 * @param rootException
	 */
	public ErrorMessageContainer(PasserelleException e) {
		try {
			setBodyContent(e,ManagedMessage.objectContentType);
		} catch (MessageException ex) {
			logger.error("",ex);
		}
	}
	
	/**
	 * 
	 * @return the exception contained in this message
	 */
  public PasserelleException getException() {
    try {
      return ((PasserelleException)getBodyContent());
    } catch (Throwable e) {
      return null;
    }
  }

  /**
   * @return the context object associated with the contained
   * passerelle exception
   */
  public Object getContext() {
    try {
      return ((PasserelleException)getBodyContent()).getContext();
    } catch (Throwable e) {
      return null;
    }
  }

	/**
	 * @return the (optional) root exception that
	 * has caused the contained Passerelle exception 
	 */
	public Throwable getRootException() {
		try {
			return ((PasserelleException)getBodyContent()).getCause();
		} catch (Throwable e) {
			return null;
		}
	}

    
    public MessageContainer copy() throws MessageException {
        ErrorMessageContainer res = new ErrorMessageContainer((PasserelleException)getBodyContent());
        res.headers = ((MapOfLists)this.headers).copy();
        return res;
    }

    public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[ErrorMessageContainer:");
            try {
				buffer.append(" error : "+getBodyContent());
			} catch (MessageException e) {
				buffer.append(" error : error info not available");
			}
            buffer.append("]");
            return buffer.toString();
        }
    
    

}
