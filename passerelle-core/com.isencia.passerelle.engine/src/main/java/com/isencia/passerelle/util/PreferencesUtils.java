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
package com.isencia.passerelle.util;

import org.eclipse.core.internal.preferences.PreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

/**
 * This is a utility class that should always be used when needing to retrieve preferences
 * Otherwise you'll might end up getting the preferences from the wrong location
 *
 * @author durdav
 *
 */
public class PreferencesUtils {

	public static final String APPLICATION_ROOT_KEY = "com.isencia.sherpa.preferences";
	public static final String CONFIG_ROOT_KEY = "config";

	public static Preferences getRootNode() {
		Preferences root = PreferencesService.getDefault().getRootNode().node(InstanceScope.SCOPE);
		return root.node(APPLICATION_ROOT_KEY);
	}

	public static Preferences getConfigNode() {
		return getRootNode().node(CONFIG_ROOT_KEY);
	}
}