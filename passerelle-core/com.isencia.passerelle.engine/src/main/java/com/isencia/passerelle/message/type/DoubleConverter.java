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

import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Convert between DoubleToken and Passerelle messages
 * 
 * @author erwin
 */
public class DoubleConverter extends ConverterBase {
	protected boolean areTypesCompatible(Class fromType, Type toType) {
		boolean res = BaseType.DOUBLE.equals(toType);
		if(fromType!=null) {
			// check some cases where we can easily convert to a Double
			res = res && 
				(String.class.equals(fromType)
			  || Boolean.class.equals(fromType)
			  || Number.class.isAssignableFrom(fromType));
		}
		return res;
	}

	protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException {
		DoubleToken result = null;
		if(content==null) {
			result = new DoubleToken();
		} else if(Double.class.isInstance(content)) {
			result = new DoubleToken(((Double)content).doubleValue());
		} else {
			try {
				// do a conversion via a String representation
				result = new DoubleToken(content.toString());
			} catch (IllegalActionException e) {
				throw new UnsupportedOperationException();
			}
		}
		return result;
	}

	protected boolean areTypesCompatible(Type fromType, Class toType) {
		return BaseType.DOUBLE.equals(fromType);
	}

	protected Object convertTokenToContent(Token typedToken, Class targetType) throws MessageException {
		if(targetType!=null && !Double.class.equals(targetType)) {
			// do a conversion via a String representation
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {Double.toString(((DoubleToken)typedToken).doubleValue())});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			try {
				return new Double(((DoubleToken)typedToken).doubleValue());
			} catch (ClassCastException e) {
				throw new UnsupportedOperationException();
			}
		}
	}

	protected boolean areTypesCompatible(Class fromType, Class toType) {
		return toType==null || Double.class.isAssignableFrom(toType);
	}

	protected Object convertContentToType(Object content, Class targetType) throws UnsupportedOperationException, MessageException {
		if(targetType!=null && !Double.class.equals(targetType)) {
			// do a conversion via a String representation
			try {
				Constructor c = targetType.getConstructor(new Class[] {String.class});
				return c.newInstance(new Object[] {content.toString()});
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		} else {
			try {
				return new Double(Double.parseDouble(content.toString()));
			} catch (Exception e) {
				throw new UnsupportedOperationException();
			}
		}
	}
}
