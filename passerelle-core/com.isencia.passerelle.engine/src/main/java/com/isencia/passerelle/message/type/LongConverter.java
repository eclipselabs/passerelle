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

import java.lang.reflect.Constructor;
import com.isencia.passerelle.message.MessageException;

import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Convert between LongToken and Passerelle messages
 * 
 * @author erwin
 */
public class LongConverter extends ConverterBase {
	protected boolean areTypesCompatible(Class fromType, Type toType) {
		boolean res = BaseType.LONG.equals(toType);
		if(fromType!=null) {
			// check some cases where we can easily convert to a Long
			res = res && 
				(String.class.equals(fromType)
			  || Boolean.class.equals(fromType)
			  || Number.class.isAssignableFrom(fromType));
		}
		return res;
	}

	protected boolean areTypesCompatible(Type fromType, Class toType) {
		return BaseType.LONG.equals(fromType);
	}

	protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException {
		LongToken result;
		if(content==null) {
			result = new LongToken();
		} else if(Long.class.isInstance(content)) {
			result = new LongToken(((Long)content).longValue());
		} else {
			try {
				// do a conversion via a String representation
				result = new LongToken(content.toString());
			} catch (IllegalActionException e) {
				throw new UnsupportedOperationException();
			}
		}
		return result;
	}

	protected Object convertTokenToContent(Token typedToken, Class targetType) throws MessageException {
		if(targetType!=null && !Long.class.equals(targetType)) {
			// do a conversion via a String representation
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {Long.toString(((LongToken)typedToken).longValue())});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			// we just return a Long as default,
			// if no specific target type is given
			try {
				return new Long(((LongToken)typedToken).longValue());
			} catch (ClassCastException e) {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	protected boolean areTypesCompatible(Class fromType, Class toType) {
		return toType==null || Long.class.isAssignableFrom(toType);
	}

	protected Object convertContentToType(Object content, Class targetType) throws UnsupportedOperationException, MessageException {
		if(targetType!=null && !Long.class.equals(targetType)) {
			// do a conversion via a String representation
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {content.toString()});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			try {
				return new Long(Long.parseLong(content.toString()));
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		}
	}

}
