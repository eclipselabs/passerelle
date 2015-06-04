package com.isencia.passerelle.workbench.model.ui.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

public class StringUtils {
	public static final String APPLICATION_ROOT_KEY = "com.isencia.sherpa.preferences";

	private static SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static java.util.prefs.Preferences root;

	/**
	 * Checks if at least one of the contain items is in the source list
	 * 
	 * @param source
	 *            comma separated list of items
	 * @param contains
	 *            items to search for in the source
	 * @return true if at least 1 contain item is in the source list otherwise
	 *         false
	 */
	public static boolean contains(String source, String... contains) {
		if (source == null)
			return false;
		if (contains == null || contains.length == 0)
			return false;
		String[] tokens = source.split(",");
		if (tokens == null || tokens.length == 0)
			return false;
		Set<String> tokenSet = new HashSet<String>();
		for (String token : tokens) {
			if (token == null)
				continue;
			tokenSet.add(token.trim());
		}
		for (String containOption : contains) {
			if (containOption == null)
				continue;
			if (tokenSet.contains(containOption.trim()))
				return true;
		}
		return false;
	}
	public static boolean contains(HashSet<String> source, String... contains) {
		
		for (String containOption : contains) {
			if (containOption == null)
				continue;
			if (source.contains(containOption.trim()))
				return true;
		}
		return false;
	}

	public static String getElement(String source, String beforePattern,
			String afterPattern) {
		if (source == null)
			return "";

		String element = "";
		int beginIndex = 0;
		int endIndex = source.length();
		if (afterPattern.length() > 0)
			beginIndex = source.indexOf(afterPattern) + 1;
		if (beforePattern.length() > 0)
			endIndex = source.indexOf(beforePattern);

		element = source.substring(beginIndex, endIndex);

		return element;
	}

	public static boolean compareContains(Object object, String compareString) {
		if (object == null || compareString == null)
			return false;

		return (object.toString().equals(compareString));
	}

	public static boolean containsAtLeastOneSubString(String source,
			String... subStrings) {
		if (source == null)
			return false;
		if (subStrings == null)
			return false;
		for (String subString : subStrings) {
			if (containsSubstring(source, subString)) {
				return true;
			}
		}
		return false;
	}

	public static boolean startsWithAtLeastOneSubstring(String source,
			String... subStrings) {
		if (source == null)
			return false;
		if (subStrings == null)
			return false;
		for (String subString : subStrings) {
			if (startWithSubstring(source, subString)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsAllSubStrings(String source,
			String... subStrings) {
		if (source == null)
			return false;
		if (subStrings == null)
			return false;
		for (String subString : subStrings) {
			if (!containsSubstring(source, subString)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkReqex(String source, String regex) {
		if (source == null)
			return false;
		if (regex == null)
			return false;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(source);

		return m.matches();

	}

	public static String findFirstOccurrenceReqex(String source, String regex) {
		if (source == null)
			return null;
		if (regex == null)
			return null;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(source);
		while (m.find()) {
			return source.substring(m.start(), m.end());
		}
		return null;

	}

	public static String findOccurrenceReqex(String source, String regex,
			int occurrence) {
		if (source == null)
			return null;
		if (regex == null)
			return null;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(source);

		while (m.find()) {
			occurrence = occurrence - 1;
			if (occurrence == 0) {
				return source.substring(m.start(), m.end());
			}
		}
		return null;

	}

	public static boolean startWithSubstring(String source, String subString) {
		if (source == null)
			return false;
		if (subString == null)
			return false;
		
		return source.startsWith(subString);
	}
	public static boolean endsWithSubstring(String source, String subString) {
		if (source == null)
			return false;
		if (subString == null)
			return false;
		
		return source.endsWith(subString);
	}

	public static boolean containsSubstring(String source, String subString) {
		return getIndexSubstring(source, subString) >= 0;
	}

	public static int getIndexSubstring(String source, String subString) {
		if (source == null)
			return -1;
		if (subString == null)
			return -1;
		return source.indexOf(subString);
	}

	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		int i = 0;
		for (int length = str.length(); i < length; i++)
			if (str.charAt(i) != ' ')
				return false;

		return true;
	}

	public static String getPreference(Map<String, String> defaultMap,
			String[] path, String name) {
		String defaultValue = defaultMap.get(name);
		try {
			IPreferencesService preferencesService = Platform
					.getPreferencesService();
			if (preferencesService != null) {
				Preferences root = preferencesService.getRootNode().node(
						InstanceScope.SCOPE).node(APPLICATION_ROOT_KEY);
				Preferences node = root;
				for (String key : path) {
					node = node.node(key);
				}
				return node.get(name, defaultValue);
			} else {
				java.util.prefs.Preferences root = getRoot();
				java.util.prefs.Preferences node = root;
				for (String key : path) {
					node = node.node(key);
				}
				return node.get(name, defaultValue);
			}

		} catch (Exception e) {

			return defaultValue;
		}

	}

	private static java.util.prefs.Preferences getRoot() {
		if (root == null) {
			root = java.util.prefs.Preferences
					.systemNodeForPackage(StringUtils.class);
		}
		return root;
	}

	public static String stringBefore(String source, String pattern) {
		if (source == null)
			return "";
		String[] tokens = source.split(pattern);
		if (tokens == null || tokens.length == 0)
			return "";
		for (String token : tokens) {
			if (token == null)
				continue;
			return token.trim();
		}

		return "";
	}

	public static boolean checkContainsScope(String source, Integer from,
			Integer until) {
		if (source == null)
			return false;
		if (from == null || until == null)
			return false;
		String[] tokens = source.split(",");
		if (tokens == null || tokens.length == 0)
			return false;
		Set<String> tokenSet = new HashSet<String>();
		for (String token : tokens) {
			if (token == null)
				continue;
			tokenSet.add(token.trim());
		}
		for (int i = from; i < (until + 1); i++) {
			String containOption = Integer.toString(i);
			if (tokenSet.contains(containOption.trim()))
				return true;
		}
		return false;
	}
	public static String[] createRange(Integer from,
			Integer until) {
		if (from == null || until == null)
			return new String[]{};
		ArrayList<String> range =  new ArrayList<String>();
		for (int i = from; i < (until + 1); i++) {
			range.add(Integer.toString(i));
		}
		return range.toArray(new String[]{});
	}
}