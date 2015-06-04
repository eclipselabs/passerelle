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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * RuntimeStreamReader 
 * <p>
 * A utility class to handle streams from launched processes in a correct way.
 * </p>
 * <p>
 * For basic use cases, this is used with a PrintStream or Writer that will receive any output or error outputs from the running process.
 * A more advanced usage can be implemented by registering a listener that will receive the output/error line-by-line.
 * </p>
 * @author wim & erwin
 */
public class RuntimeStreamReader extends Thread {
  
  private InputStream inputStr;
  private Object lock;
  private Type type;
  private Writer plainTextWriter;

  private PrintStream plainTextPrintStream;
  private RuntimeStreamListener listener;

  public RuntimeStreamReader(Object lock, InputStream is, Type type, Writer writer) {
    this.inputStr = is;
    this.type = type;
    this.plainTextWriter = writer;
    this.lock = lock;
  }

  public RuntimeStreamReader(Object lock, InputStream is, Type type, PrintStream prtStream) {
    this.inputStr = is;
    this.type = type;
    this.plainTextPrintStream = prtStream;
    this.lock = lock;
  }

  public RuntimeStreamReader(Object lock, InputStream is, Type type, RuntimeStreamListener listener) {
    this.inputStr = is;
    this.type = type;
    this.listener = listener;
    this.lock = lock;
  }

  /**
   * DOCUMENT ME!
   */
  public void run() {
    try {
      PrintWriter pw = null;
      if (null != plainTextWriter) {
        pw = new PrintWriter(plainTextWriter);
      }

      if (null != plainTextPrintStream) {
        pw = new PrintWriter(plainTextPrintStream);
      }
      InputStreamReader isr = new InputStreamReader(inputStr);
      BufferedReader br = new BufferedReader(isr);
      String line = null;

      while ((line = br.readLine()) != null) {
        if (pw != null) {
          pw.println(line);
          pw.flush();
        }
        
        if(listener!=null) {
          listener.acceptLine(line);
        }
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } finally {
      synchronized (lock) {
        lock.notifyAll();
      }
    }
  }

  /**
   * Simple callback/listener that can implement custom/advanced
   * logic that should be applied on process output/error stream text.
   *
   */
  public interface RuntimeStreamListener {
    void acceptLine(String newLine);
  }

  public static final class Type {
    public static final Type output = new Type(1, "Output");
    public static final Type error = new Type(2, "Error");
    private String name = "";

    private Type(int type, String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }
  }
}