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
package com.isencia.passerelle.defaulttypeconverterprovider;

import java.util.ArrayList;
import java.util.List;
import com.isencia.passerelle.ext.TypeConverterProvider;
import com.isencia.passerelle.message.type.ArrayConverter;
import com.isencia.passerelle.message.type.BooleanConverter;
import com.isencia.passerelle.message.type.ComplexConverter;
import com.isencia.passerelle.message.type.DoubleConverter;
import com.isencia.passerelle.message.type.IntegerConverter;
import com.isencia.passerelle.message.type.LongConverter;
import com.isencia.passerelle.message.type.StringConverter;
import com.isencia.passerelle.message.type.TypeConverter;


public class DefaultTypeConvertorProvider implements TypeConverterProvider {
	private List<TypeConverter> converters = new ArrayList<TypeConverter>();

	public DefaultTypeConvertorProvider() {
		converters.add(new ArrayConverter());
		converters.add(new BooleanConverter());
		converters.add(new IntegerConverter());
		converters.add(new LongConverter());
		converters.add(new DoubleConverter());
		converters.add(new ComplexConverter());
		converters.add(new StringConverter());
	}

	public String getName() {
		return "DEFAULT-OSGI";
	}

	public List<TypeConverter> getTypeConverters() {
		return converters;
	}
}
