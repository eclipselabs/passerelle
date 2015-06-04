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

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

/**
 * Convert between StringToken and Passerelle messages
 * 
 * @author erwin
 */
public class StringConverter extends ConverterBase {
	protected boolean areTypesCompatible(Class fromType, Type toType) {
		// we assume we convert everything to strings, by just calling toString()
		// so we just need to make sure the target type is STRING
		return BaseType.STRING.equals(toType);
	}

	protected boolean areTypesCompatible(Type fromType, Class toType) {
		boolean res = BaseType.STRING.equals(fromType);
		// in order to make sure we can do an easy conversion
		// let's look for constructors with a String parameter
		if(toType!=null) {
			try {
				toType.getConstructor(new Class[] {String.class});
			} catch (NoSuchMethodException e) {
				res = false;
			}
		}
		return res;
	}
	
	protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException {
		return new StringToken(content!=null?content.toString():null);
	}

	protected Object convertTokenToContent(Token typedToken, Class targetType) throws UnsupportedOperationException {
		if(typedToken==null)
			return null;
		
		if(targetType!=null) {
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {((StringToken)typedToken).stringValue()});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			// we just return a String as default,
			// if no specific target type is given
			try {
				return ((StringToken)typedToken).stringValue();
			} catch (ClassCastException e) {
				throw new UnsupportedOperationException();
			}
		}
	}
	
	protected boolean areTypesCompatible(Class fromType, Class toType) {
		return toType==null || String.class.isAssignableFrom(toType);
	}

	protected Object convertContentToType(Object content, Class targetType) throws UnsupportedOperationException, MessageException {
		if(targetType!=null && !String.class.equals(targetType)) {
			// do a conversion via a String representation
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {content.toString()});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			try {
				return content.toString();
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		}
	}
	
}
