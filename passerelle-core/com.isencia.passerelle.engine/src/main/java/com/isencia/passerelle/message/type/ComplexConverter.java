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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.apache.commons.math.complex.ComplexFormat;
import ptolemy.data.ComplexToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.math.Complex;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.MessageException;

/**
 * Convert between ComplexToken and Passerelle messages
 * 
 * @author erwin
 */
public class ComplexConverter extends ConverterBase {
  private ComplexFormat iFormat = new ComplexFormat(NumberFormat.getInstance(Locale.ENGLISH));
  private ComplexFormat jFormat = new ComplexFormat("j", NumberFormat.getInstance(Locale.ENGLISH));

  protected boolean areTypesCompatible(Class fromType, Type toType) {
    boolean res = BaseType.COMPLEX.equals(toType);
    if (fromType != null) {
      // check some cases where we can easily convert to a Double
      res = res
          && (Complex.class.equals(fromType) || org.apache.commons.math.complex.Complex.class.equals(fromType) || String.class.equals(fromType)
              || Boolean.class.equals(fromType) || Number.class.isAssignableFrom(fromType));
    }
    return res;
  }

  protected boolean areTypesCompatible(Type fromType, Class toType) {
    return BaseType.COMPLEX.equals(fromType);
  }

  protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException {
    ComplexToken result;
    if (content == null) {
      result = new ComplexToken();
    } else if (Complex.class.isInstance(content)) {
      Complex cVal = (Complex) content;
      double real = cVal.real;
      double imag = cVal.imag;
      result = new ComplexToken(new Complex(real, imag));
    } else {
      // do a conversion via a String representation
      String contentStr = content.toString();
      try {
        org.apache.commons.math.complex.Complex complex = null;
        try {
          complex = iFormat.parse(contentStr);
        } catch (ParseException e) {
          // Try the j-format
          complex = jFormat.parse(contentStr);
        }
        result = new ComplexToken(new Complex(complex.getReal(), complex.getImaginary()));
      } catch (ParseException e) {
        throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error converting " + content, e);
      }
    }
    return result;
  }

  protected Object convertTokenToContent(Token typedToken, Class targetType) throws MessageException {
    Complex cVal = ((ComplexToken) typedToken).complexValue();
    if (targetType != null && !Complex.class.equals(targetType)) {
      if (String.class.equals(targetType)) {
        return cVal.toString();
      } else {
        // only take real part
        try {
          Constructor c = targetType.getConstructor(new Class[] { String.class });
          return c.newInstance(new Object[] { Double.toString(cVal.real) });
        } catch (Exception e) {
          throw new UnsupportedOperationException();
        }
      }
    } else {
      // we just return a Complex as default,
      // if no specific target type is given
      try {
        double real = cVal.real;
        double imag = cVal.imag;
        return new Complex(real, imag);
      } catch (ClassCastException e) {
        throw new UnsupportedOperationException();
      }
    }
  }

  protected boolean areTypesCompatible(Class fromType, Class toType) {
    return toType == null || Complex.class.isAssignableFrom(toType);
  }

  protected Object convertContentToType(Object content, Class targetType) throws UnsupportedOperationException, MessageException {
    if (targetType != null && !Complex.class.equals(targetType)) {
      // do a conversion via a String representation
      try {
        Constructor c = targetType.getConstructor(new Class[] { String.class });
        return c.newInstance(new Object[] { content.toString() });
      } catch (Exception e) {
        throw new UnsupportedOperationException();
      }
    } else {
      // do a conversion to Complex via a String representation
      String contentStr = content.toString();
      try {
        org.apache.commons.math.complex.Complex complex = null;
        try {
          complex = iFormat.parse(contentStr);
        } catch (ParseException e) {
          // Try the j-format
          complex = jFormat.parse(contentStr);
        }
        return new Complex(complex.getReal(), complex.getImaginary());
      } catch (ParseException e) {
        throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR, "Error converting " + content, e);
      }
    }
  }

}
