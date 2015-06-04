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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.core.PasserelleToken;
import com.isencia.passerelle.ext.TypeConverterProvider;
import com.isencia.passerelle.ext.impl.DefaultTypeConverterProvider;
import com.isencia.passerelle.message.MessageException;

import ptolemy.data.Token;
import ptolemy.data.type.Type;

/**
 * Provides an easy access to a preconfigured series
 * of converters using the Chain-of-Responsibility design pattern.
 * 
 * 
 * @author erwin
 */
public class TypeConversionChain implements TypeConverter {

	private final static Logger logger = LoggerFactory.getLogger(TypeConversionChain.class);

	private final static TypeConverterProvider DEFAULT_CONVERTER_PROVIDER = new DefaultTypeConverterProvider();
	private TypeConverterProvider converterProvider = DEFAULT_CONVERTER_PROVIDER;
	
	private final static TypeConversionChain instance = new TypeConversionChain();
	
	private TypeConversionChain() {
	}
	
	public static TypeConversionChain getInstance() {
		return instance;
	}
	
	public synchronized TypeConverterProvider getConverterProvider() {
		return converterProvider;
	}

	public synchronized void setConverterProvider(TypeConverterProvider converterProvider) {
		if(converterProvider!=null)
			this.converterProvider = converterProvider;
		else 
			this.converterProvider = DEFAULT_CONVERTER_PROVIDER;
		
		logger.info("Set TypeConverterProvider to {}",this.converterProvider.getName());
	}

	// used internally by ArrayConverter
	protected Token convertContentToToken(Object content, Type targetType) throws UnsupportedOperationException, MessageException {
		Token res = null;
		Iterator<TypeConverter> iter = null;
		boolean foundConversion = false;
		for (iter = converterProvider.getTypeConverters().iterator(); iter.hasNext();) {
			ConverterBase converter = (ConverterBase) iter.next();
			try {
				res = converter.convertContentToToken(content,targetType);
				foundConversion = true;
				break;
			} catch (UnsupportedOperationException e) {
				// continue the chain
			}
		}
		if(!foundConversion) {
			// conversion failed
			throw new UnsupportedOperationException();
		}
		
		return res;
	}

	// used internally by ArrayConverter
	protected Object convertTokenToContent(Token typedToken, Class targetType) throws UnsupportedOperationException, MessageException {
		Object res = null;
		Iterator iter = null;
		boolean foundConversion = false;
		for (iter = converterProvider.getTypeConverters().iterator(); iter.hasNext();) {
			ConverterBase converter = (ConverterBase) iter.next();
			try {
				res = converter.convertTokenToContent(typedToken,targetType);
				foundConversion = true;
				break;
			} catch (UnsupportedOperationException e) {
				// continue the chain
			}
		}
		if(!foundConversion) {
			// conversion failed
			throw new UnsupportedOperationException();
		}
		
		return res;
	}

	public Token convertPasserelleTokenToPtolemyToken(PasserelleToken passerelleMsgToken, Type ptolemyTargetType) throws UnsupportedOperationException, PasserelleException {
		Token res = null;
		Iterator iter = null;
		boolean foundConversion = false;
		for (iter = converterProvider.getTypeConverters().iterator(); iter.hasNext();) {
			TypeConverter converter = (TypeConverter) iter.next();
			try {
				res = converter.convertPasserelleTokenToPtolemyToken(passerelleMsgToken,ptolemyTargetType);
				foundConversion = true;
				break;
			} catch (UnsupportedOperationException e) {
				// continue the chain
			}
		}
		if(!foundConversion) {
			// conversion failed
			throw new UnsupportedOperationException();
		}
		
		return res;
	}
		
	public PasserelleToken convertPtolemyTokenToPasserelleToken(Token origToken, Class targetContentType) throws UnsupportedOperationException, PasserelleException {
		PasserelleToken res = null;
		Iterator iter = null;
		boolean foundConversion = false;
		for (iter = converterProvider.getTypeConverters().iterator(); iter.hasNext();) {
			TypeConverter converter = (TypeConverter) iter.next();
			try {
				res = converter.convertPtolemyTokenToPasserelleToken(origToken, targetContentType);
				foundConversion = true;
				break;
			} catch (UnsupportedOperationException e) {
				// continue the chain
			}
		}
		if(!foundConversion) {
			// conversion failed
			throw new UnsupportedOperationException();
		}
		
		return res;
	}

	public PasserelleToken convertPasserelleMessageContent(PasserelleToken origToken, Class targetContentType) throws UnsupportedOperationException, PasserelleException {
		PasserelleToken res = null;
		Iterator iter = null;
		boolean foundConversion = false;
		for (iter = converterProvider.getTypeConverters().iterator(); iter.hasNext();) {
			TypeConverter converter = (TypeConverter) iter.next();
			try {
				res = converter.convertPasserelleMessageContent(origToken, targetContentType);
				foundConversion = true;
				break;
			} catch (UnsupportedOperationException e) {
				// continue the chain
			}
		}
		if(!foundConversion) {
			// conversion failed
			throw new UnsupportedOperationException();
		}
		
		return res;
	}
	
}
