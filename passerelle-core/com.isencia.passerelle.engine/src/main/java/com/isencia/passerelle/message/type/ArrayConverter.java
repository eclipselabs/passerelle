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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.MessageException;

/**
 * Convert between ArrayToken and Passerelle messages
 * 
 * @author erwin
 */
public class ArrayConverter extends ConverterBase {
    private final static String lineSeparator = System.getProperty("line.separator");

	protected boolean areTypesCompatible(Class fromType, Type toType) {
		boolean res = (toType instanceof ArrayType);
//		if(res && fromType!=null) {
			// check if contained type in the array is compatible with the fromType
//			res = ((ArrayType)toType).getElementType().??
//		}
		return res;
	}

	protected boolean areTypesCompatible(Type fromType, Class toType) {
		// for the moment, only support conversion between array and string or array
		return (fromType instanceof ArrayType) && (toType==null || String.class.equals(toType) || toType.isArray());
	}

	protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException {
		ArrayType arrayType = (ArrayType) targetType;
		Type elementType = arrayType!=null?arrayType.getElementType():null;
		
		if(content==null) {
			// Ptolemy ArrayToken can not be created empty, BLAST!
			// so return an ArrayToken with one empty element, as obtained from simple converters
			try {
				return new ArrayToken(new Token[] {TypeConversionChain.getInstance().convertContentToToken(null, elementType)});
			} catch (MessageException e) {
				throw e;
			} catch (Exception e) {
				throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR,"Error converting " + content,e);
			}
		} else if(content.getClass().isArray()) {
			try {
				Object[] coll = (Object[]) content;
				List elements = new ArrayList();
				for (int i = 0; i < coll.length; i++) {
					Object contentElement = coll[i];
					Token newElement = TypeConversionChain.getInstance().convertContentToToken(contentElement, elementType);
					elements.add(newElement);
				}
				return new ArrayToken((Token[])elements.toArray(new Token[0]));
			} catch (MessageException e) {
				throw e;
			} catch (Exception e) {
				throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR,"Error converting " + content,e);
			}
		} else if(Collection.class.isInstance(content)) {
			try {
				Collection coll = (Collection) content;
				List elements = new ArrayList();
				for (Iterator iter = coll.iterator(); iter.hasNext();) {
					Object contentElement = (Object) iter.next();
					Token newElement = TypeConversionChain.getInstance().convertContentToToken(contentElement, elementType);
					elements.add(newElement);
				}
				return new ArrayToken((Token[])elements.toArray(new Token[0]));
			} catch (MessageException e) {
				throw e;
			} catch (Exception e) {
				throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR,"Error converting " + content,e);
			}
		} else {
			// just try to treat the content string representation...
			String contentString = content.toString();
			List elements = new ArrayList();
			try {
				BufferedReader contentReader = new BufferedReader(new StringReader(contentString));
				String contentLine;
				while((contentLine = contentReader.readLine())!=null) {
					Token newElement = TypeConversionChain.getInstance().convertContentToToken(contentLine, elementType);
					elements.add(newElement);
				}
				return new ArrayToken((Token[])elements.toArray(new Token[0]));
			} catch (MessageException e) {
				throw e;
			} catch (Exception e) {
				throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR,"Error converting " + content,e);
			}
		}

	}

	protected Object convertTokenToContent(Token typedToken, Class targetType) throws MessageException {
		if(typedToken==null)
			return null;
		
		ArrayToken arrayToken = (ArrayToken) typedToken;
		if(arrayToken.length()==0)
			return null;

		if(targetType!=null && !targetType.isArray()) {
			if(Collection.class.isAssignableFrom(targetType)) {
				// move all elements in the collection
				Collection res;
				try {
					res = (Collection) targetType.newInstance();
					for(int i = 0; i<arrayToken.length();++i) {
						Token element = arrayToken.getElement(i);
						Object resContent = TypeConversionChain.getInstance().convertTokenToContent(element, targetType.getComponentType());
						res.add(resContent);
					}
					return res;
				} catch (MessageException e) {
					throw e;
				} catch (Exception e) {
					throw new MessageException(ErrorCode.MSG_CONTENT_TYPE_ERROR,"Error converting " + typedToken,e);
				}
			} else if(String.class.isAssignableFrom(targetType)) {
				// do a conversion to a String representation
				StringBuffer content = new StringBuffer();
				for(int i = 0; i<arrayToken.length();++i) {
					Token element = arrayToken.getElement(i);
					content.append(element.toString());
					content.append(lineSeparator);
				}
				return content.toString();
			} else {
				// we return only the first array element and try to
				// get a conversion for the desired array component type
				Token element = arrayToken.getElement(0);
				return TypeConversionChain.getInstance().convertTokenToContent(element, targetType);
			}
		} else {
			// we just return an array as default,
			// if no specific target type is given
			List res = new ArrayList();
			for(int i = 0; i<arrayToken.length();++i) {
				Token element = arrayToken.getElement(i);
				Object resContent = TypeConversionChain.getInstance().convertTokenToContent(element, targetType.getComponentType());
				res.add(resContent);
			}
			
			return res.toArray();
		}
	}

	protected boolean areTypesCompatible(Class fromType, Class toType) {
		return toType==null || toType.isArray();
	}

	protected Object convertContentToType(Object content, Class targetType) throws UnsupportedOperationException, MessageException {
		throw new UnsupportedOperationException();
	}

}
