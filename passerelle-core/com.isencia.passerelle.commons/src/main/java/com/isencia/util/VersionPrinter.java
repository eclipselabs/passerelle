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

/**
 * <p>
 * Prints the version of the project on stdout.
 * </p>
 * 
 * @author erwin
 */
public class VersionPrinter {
  public static final String VERSION_MAJOR = "@version.major@";
  public static final String VERSION_MINOR = "@version.minor@";
  public static final String VERSION_ITERATION = "@version.iteration@";
  public static final String PROJECT_NAME = "@name@";

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   * Interface.
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  public static void main(String[] args) {
    System.out.println(PROJECT_NAME + " version: " + VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_ITERATION);
  }
}
