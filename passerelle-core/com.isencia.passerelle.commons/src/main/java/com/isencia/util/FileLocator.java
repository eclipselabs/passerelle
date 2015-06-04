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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * FileLocator Provides class methods for finding files in the directories or
 * zip archives that make up the CLASSPATH.
 * 
 * @author dirk
 */
public class FileLocator {

  static final Properties systemProperties = System.getProperties();

  static final String classPath = systemProperties.getProperty("java.class.path", ".");

  static final String pathSeparator = systemProperties.getProperty("path.separator", ";");

  /**
   * Returns the fully qualified file name associated with the passed
   * DataInputStream <i>if the DataInputStream was created using one of the
   * static locate methods supplied with this class </i>, otherwise returns a
   * zero length string.
   */
  public static String getFileNameFromStream(DataInputStream ds) {

    if (ds instanceof NamedDataInputStream) return ((NamedDataInputStream) ds).fullyQualifiedFileName;
    return "";

  }

  /**
   * Returns an indication of whether the passed DataInputStream is associated
   * with a member of a zip file <i>if the DataInputStream was created using one
   * of the static locate methods supplied with this class </i>, otherwise
   * returns false.
   */
  public static boolean isZipFileAssociatedWithStream(DataInputStream ds) {

    if (ds instanceof NamedDataInputStream) return ((NamedDataInputStream) ds).inZipFile;
    return false;

  }

  /**
   * locateClassFile returns a DataInputStream with mark/reset capability that
   * can be used to read the requested class file. The CLASSPATH is used to
   * locate the class.
   * 
   * @param classFileName The name of the class to locate. The class name should
   *          be given in fully-qualified form, for example:
   * 
   *          <pre>
   * 
   *      java.lang.Object
   *      java.io.DataInputStream
   * 
   * </pre>
   * @exception java.io.FileNotFoundException The requested class file could not
   *              be found.
   * @exception java.io.IOException The requested class file could not be
   *              opened.
   */
  public static DataInputStream locateClassFile(String classFileName) throws FileNotFoundException, IOException {

    boolean notFound = true;
    StringTokenizer st;
    String path = "";
    String pathNameForm;
    File cf = null;
    NamedDataInputStream result;

    st = new StringTokenizer(classPath, pathSeparator, false);
    pathNameForm = classFileName.replace('.', File.separatorChar) + ".class";

    while (st.hasMoreTokens() && notFound) {

      try {
        path = st.nextToken();
      } catch (NoSuchElementException nse) {
        break;
      }
      int pLen = path.length();
      String pathLast4 = pLen > 3 ? path.substring(pLen - 4) : "";
      if (pathLast4.equalsIgnoreCase(".zip") || pathLast4.equalsIgnoreCase(".jar")) {

        try {

          result = locateInZipFile(path, classFileName, true, true);
          if (result == null) continue;
          return (DataInputStream) result;

        } catch (ZipException zfe) {
          continue;
        } catch (IOException ioe) {
          continue;
        }

      } else {
        try {
          cf = new File(path + File.separator + pathNameForm);
        } catch (NullPointerException npe) {
          continue;
        }
        if ((cf != null) && cf.exists()) notFound = false;
      }
    }

    if (notFound) {

      /*
       * Make one last attempt to find the file in the current directory
       */

      int lastdot = classFileName.lastIndexOf('.');
      String simpleName = (lastdot >= 0) ? classFileName.substring(lastdot + 1) : classFileName;

      result = new NamedDataInputStream(new BufferedInputStream(new FileInputStream(simpleName + ".class")), simpleName + ".class", false);
      return (DataInputStream) result;
    }

    result = new NamedDataInputStream(new BufferedInputStream(new FileInputStream(cf)), path + File.separator + pathNameForm, false);
    return (DataInputStream) result;

  }

  /**
   * locateFileInClassPath returns a DataInputStream that can be used to read
   * the requested file. The CLASSPATH is used to locate the file.
   * 
   * @param fileName The name of the file to locate. The file name may be
   *          qualified with a partial path name, using '/' as the separator
   *          character or using separator characters appropriate for the host
   *          file system, in which case each directory or zip file in the
   *          CLASSPATH will be used as a base for finding the fully-qualified
   *          file.
   * @exception java.io.FileNotFoundException The requested class file could not
   *              be found.
   * @exception java.io.IOException The requested class file could not be
   *              opened.
   */
  public static DataInputStream locateFileInClassPath(String fileName) throws FileNotFoundException, IOException {

    boolean notFound = true;
    StringTokenizer st;
    String path = "";
    File cf = null;
    NamedDataInputStream result;

    String zipEntryName = File.separatorChar == '/' ? fileName : fileName.replace(File.separatorChar, '/');

    String localFileName = File.separatorChar == '/' ? fileName : fileName.replace('/', File.separatorChar);

    st = new StringTokenizer(classPath, pathSeparator, false);

    while (st.hasMoreTokens() && notFound) {

      try {
        path = st.nextToken();
      } catch (NoSuchElementException nse) {
        break;
      }
      int pLen = path.length();
      String pathLast4 = pLen > 3 ? path.substring(pLen - 4) : "";
      if (pathLast4.equalsIgnoreCase(".zip") || pathLast4.equalsIgnoreCase(".jar")) {

        try {

          result = locateInZipFile(path, zipEntryName, false, false);
          if (result == null) continue;
          return (DataInputStream) result;

        } catch (ZipException zfe) {
          continue;
        } catch (IOException ioe) {
          continue;
        }

      } else {
        try {
          cf = new File(path + File.separator + localFileName);
        } catch (NullPointerException npe) {
          continue;
        }
        if ((cf != null) && cf.exists()) notFound = false;
      }
    }

    if (notFound) {

      /*
       * Make one last attempt to find the file in the current directory
       */

      int lastpart = localFileName.lastIndexOf(File.separator);
      String simpleName = (lastpart >= 0) ? localFileName.substring(lastpart + 1) : localFileName;

      result = new NamedDataInputStream(new BufferedInputStream(new FileInputStream(simpleName)), simpleName, false);
      return (DataInputStream) result;
    }

    result = new NamedDataInputStream(new BufferedInputStream(new FileInputStream(cf)), path + File.separator + localFileName, false);
    return (DataInputStream) result;

  }

  private static NamedDataInputStream locateInZipFile(String zipFileName, String fileName, boolean wantClass, boolean buffered) throws ZipException,
      IOException {

    ZipFile zf;
    ZipEntry ze;
    zf = new ZipFile(zipFileName);

    if (zf == null) return null;
    String zeName = wantClass ? fileName.replace('.', '/') + ".class" : fileName;

    ze = zf.getEntry(zeName);
    if (ze == null) {
      zf.close(); // D55355, D56419
      zf = null;
      return null;
    }
    InputStream istream = zf.getInputStream(ze);
    if (buffered) istream = new BufferedInputStream(istream);
    return new NamedDataInputStream(istream, zipFileName + '(' + zeName + ')', true);

  }

  /**
   * locateLocaleSpecificFileInClassPath returns a DataInputStream that can be
   * used to read the requested file, but the name of the file is determined
   * using information from the current locale and the supplied file name (which
   * is treated as a "base" name, and is supplemented with country and language
   * related suffixes, obtained from the current locale). The CLASSPATH is used
   * to locate the file.
   * 
   * @param fileName The name of the file to locate. The file name may be
   *          qualified with a partial path name, using '/' as the separator
   *          character or using separator characters appropriate for the host
   *          file system, in which case each directory or zip file in the
   *          CLASSPATH will be used as a base for finding the fully-qualified
   *          file. Here is an example of how the supplied fileName is used as a
   *          base for locating a locale-specific file:
   * 
   *          <pre>
   * 
   *      Supplied fileName: a/b/c/x.y,  current locale: US English
   * 
   *                      Look first for: a/b/c/x_en_US.y
   *      (if that fails) Look next for:  a/b/c/x_en.y
   *      (if that fails) Look last for:  a/b/c/x.y
   * 
   *      All elements of the class path are searched for each name,
   *      before the next possible name is tried.
   * 
   * </pre>
   * @exception java.io.FileNotFoundException The requested class file could not
   *              be found.
   * @exception java.io.IOException The requested class file could not be
   *              opened.
   */
  public static DataInputStream locateLocaleSpecificFileInClassPath(String fileName) throws FileNotFoundException, IOException {

    String localeSuffix = "_" + Locale.getDefault().toString();
    int lastSlash = fileName.lastIndexOf('/');
    int lastDot = fileName.lastIndexOf('.');
    String fnFront, fnEnd;
    DataInputStream result = null;
    boolean lastAttempt = false;

    if ((lastDot > 0) && (lastDot > lastSlash)) {
      fnFront = fileName.substring(0, lastDot);
      fnEnd = fileName.substring(lastDot);
    } else {
      fnFront = fileName;
      fnEnd = "";
    }

    while (true) {
      if (lastAttempt)
        result = locateFileInClassPath(fileName);
      else
        try {
          result = locateFileInClassPath(fnFront + localeSuffix + fnEnd);
        } catch (Exception e) { /* ignore */
        }
      if ((result != null) || lastAttempt) break;
      int lastUnderbar = localeSuffix.lastIndexOf('_');
      if (lastUnderbar > 0)
        localeSuffix = localeSuffix.substring(0, lastUnderbar);
      else
        lastAttempt = true;
    }
    return result;

  }
}