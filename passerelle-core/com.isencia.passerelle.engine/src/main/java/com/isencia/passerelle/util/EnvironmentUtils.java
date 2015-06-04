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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import com.isencia.constants.IPropertyNames;


/**
 * EnvironmentUtils
 * 
 * A basic set of utils to obtain environment info.
 * 
 * @author erwin
 */
public class EnvironmentUtils {
	
	private static final String DEFAULT_PASSERELLE_USERLIBRARY_PATH = "/UserLibrary.xml";
	private final static String PASSERELLE_USERLIB_PATH_ENV_VARIABLE = "com.isencia.passerelle.user.library";
	
	// where the user's actor classes etc are maintained
	private static File userFolder;
	// where the UserLibrary definition file is maintained
	private static URL userLibraryURL;
	
	private static File lastSelectedDirectory;
	
	/**
	 * Returns the application root folder. 
	 * 
	 * If a system property com.isencia.home is defined, this is taken as the root folder.
	 * If not, the current working directory of the Java process is taken.
	 * 
	 * @return
	 */
	public static String getApplicationRootFolder() {
		String folder = System.getProperty(IPropertyNames.APP_HOME);
		if(folder!=null && folder.trim().length()>0) {
			return folder;
		} else {
			return System.getProperty("user.dir");
		}
	}
	
	/**
	 * 
	 * @return the folder where the user's actor classes etc are maintained
	 * @throws Exception 
	 */
	public static File getUserFolder() {
		if(userFolder==null) {
			URL userLibURL = getUserLibraryURL();
			File userLibFile = new File(userLibURL.getPath());
			userFolder = userLibFile.getParentFile();
		}
		return userFolder;
	}
	
	/**
	 * 
	 * @return the path to the userlibrary definition file, relative to the classpath
	 */
	public static URL getUserLibraryURL() {
		if(userLibraryURL==null) {
	    	String path = System.getProperty(PASSERELLE_USERLIB_PATH_ENV_VARIABLE, DEFAULT_PASSERELLE_USERLIBRARY_PATH);
	    	userLibraryURL = EnvironmentUtils.class.getResource(path);
	    	if(userLibraryURL==null) {
	    		File userLibFile = new File(getApplicationRootFolder()+"/user",path);
	    		if(userLibFile.exists()) {
					try {
						userLibraryURL = userLibFile.toURI().toURL();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
	    	}
		}
		return userLibraryURL;
	}

	/**
	 * 
	 * @return the last directory that was selected by the user e.g. in a FileChooser
	 * (and registered here via setLastSelectedDirectory)
	 */
	public static File getLastSelectedDirectory() {
		return lastSelectedDirectory;
	}

	/**
	 * save the last selected directory
	 * 
	 * @param lastSelectedDirectory
	 */
	public static void setLastSelectedDirectory(File lastSelectedDirectory) {
		EnvironmentUtils.lastSelectedDirectory = lastSelectedDirectory;
	}
	
	/**
	 * 
	 * @return a directory that is selected based on either the user's last selected directory,
	 * or if not yet available, the user's home folder
	 * 
	 */
	public static File getUserRelevantDirectory() {
		File dir = getLastSelectedDirectory();
		if(dir==null) {
			String usrHomeFolder = System.getProperty("user.home");
			if(usrHomeFolder!=null)
				dir = new File(usrHomeFolder);
		}
		return dir;
	}
}
