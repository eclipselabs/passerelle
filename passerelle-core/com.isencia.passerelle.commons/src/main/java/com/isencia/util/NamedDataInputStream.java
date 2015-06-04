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

import java.io.DataInputStream;
import java.io.InputStream;

/**
 * NamedDataInputStream This class is used to associate a filename with a
 * DataInputStream The host platform's file naming conventions are assumed for
 * the filename.
 * 
 * @author dirk
 */
class NamedDataInputStream extends DataInputStream {

  // Name of the file associated with the DataInputStream.
  public String fullyQualifiedFileName;

  // Indicates whether or not the file is contained in a .zip file.
  public boolean inZipFile;

  /**
   * @param in
   * @param fullyQualifiedName
   * @param inZipFile
   */
  protected NamedDataInputStream(InputStream in, String fullyQualifiedName, boolean inZipFile) {

    super(in);
    this.fullyQualifiedFileName = fullyQualifiedName;
    this.inZipFile = inZipFile;

  }
}