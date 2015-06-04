/**
 * 
 */
package com.isencia.passerelle.process.model.util;

import java.util.regex.Pattern;

import com.isencia.passerelle.process.model.Matcher;
import com.isencia.passerelle.process.model.ResultItem;

/**
 * Implements a matching on <code>ResultItem</code> name,
 * based on regular expression pattern matching.
 * 
 * @author erwin
 *
 */
public class ResultImplNameMatcherByRegEx implements Matcher<ResultItem<?>> {
	
	private Pattern pattern;
	
	public ResultImplNameMatcherByRegEx(String pattern) {
		this.pattern = Pattern.compile(pattern);
	}

	public boolean matches(ResultItem<?> thing) {
		return pattern.matcher(thing.getName()).matches();
	}
}