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

package com.isencia.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DOCUMENT ME!
 * 
 * @version $Id: ClassPath.java 2799 2005-04-25 11:33:57Z erwin $
 * @author Dirk Jacobs
 */
public class ClassPath {

  // ~ Instance variables
  // _____________________________________________________________________________________________________________________________________

  private String classPath = "";

  /**
   * Builds a classpath containing all .jar and .zip files in the given folder
   * and all its sub-folders.
   * 
   * @param dir
   */
  public ClassPath(String dir) {
    classPath = buildClassPath(dir, new JarZipNameFilter());
  }

  /**
   * Builds a classpath containing all files in the given folder and in all its
   * sub-folders, with names matching the given regular expression filter
   * pattern.
   * 
   * @param dir
   * @param filter
   */
  public ClassPath(String dir, String filterPattern) {
    classPath = buildClassPath(dir, new RegexpNameFilter(filterPattern));
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String toString() {
    return classPath;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param dir DOCUMENT ME!
   * @return DOCUMENT ME!
   */
  private String buildClassPath(String dir, FileFilter filter) {
    StringBuffer classPath = new StringBuffer();
    File lib = new File(dir);
    File[] libFiles = lib.listFiles(filter);

    if (libFiles == null) return classPath.toString();

    for (int i = 0; i < libFiles.length; i++) {
      if (libFiles[i].isDirectory()) {
        classPath.append(buildClassPath(libFiles[i].getPath(), filter));
      } else {
        classPath.append("'" + libFiles[i].getPath() + "'" + File.pathSeparator);
      }
    }

    return classPath.toString();
  }

  // ~ Classes
  // ________________________________________________________________________________________________________________________________________________

  private class JarZipNameFilter implements FileFilter {

    /**
     * @see java.io.FileFilter#accept(File)
     */
    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }

      String name = f.getName();
      if (name == null) return false;

      name = name.toLowerCase();
      if (name.endsWith(".jar") || name.endsWith(".zip")) return true;

      return false;
    }

  }

  /**
   * Supports filtering files based on matching their names versus a regular
   * expression
   * 
   * @author erwin
   */
  private class RegexpNameFilter implements FileFilter {
    private Pattern fileNamePattern = null;

    public RegexpNameFilter(String pattern) {
      fileNamePattern = Pattern.compile(pattern);
    }

    public boolean accept(File f) {
      if (f.isDirectory()) {
        return true;
      }
      String name = f.getName();
      if (name == null) return false;

      Matcher m = fileNamePattern.matcher(name);
      return m.matches();
    }

  }
}