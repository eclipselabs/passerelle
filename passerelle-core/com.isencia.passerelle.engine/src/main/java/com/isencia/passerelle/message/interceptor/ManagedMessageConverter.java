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
package com.isencia.passerelle.message.interceptor;

import com.isencia.message.interceptor.IMessageInterceptor;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.MessageFactory;

/**
 * @todo Class Comment
 *
 * @author erwin
 */
public abstract class ManagedMessageConverter implements IMessageInterceptor {
    
    private IMessageCreator msgCreator = null;
    
    public ManagedMessageConverter(IMessageCreator msgCreator) {
        this.msgCreator = msgCreator;
    }
    
    /**
     * Utility method to ensure messages get created in a uniform way in all converters
     * @return
     */
    protected ManagedMessage createMessage() {
    	if(msgCreator!=null)
    		return msgCreator.createMessage();
    	else 
    		return MessageFactory.getInstance().createMessage();
    }
    
    /**
     * Creates a clone of the given converter.
     * In order to avoid all fancy clone() issues,
     * we just use our own method name...
     * 
     * @return
     */
    public abstract ManagedMessageConverter cloneConverter(IMessageCreator msgCreator);

}
