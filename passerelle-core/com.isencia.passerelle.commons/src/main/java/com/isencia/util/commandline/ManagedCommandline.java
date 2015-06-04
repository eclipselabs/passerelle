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
package com.isencia.util.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends <cod>EnvCommandline</code> by adding stdout and stderr stream handling as well as some assertions to check for proper execution of the command.
 * <p>
 * Modified to use better approach to handle stdout and stderr streams, using a "gobbler"thread to ensure sufficiently rapid and concurrent clearing of the OS
 * stream buffers. (cfr this <a href= "http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4">
 * </p>
 * JavaWorld article on correct launching of subprocesses in Java</a>)
 * 
 * @author <a href="mailto:rjmpsmith@hotmail.com">Robert J. Smith</a>
 * @author delerw
 */
public class ManagedCommandline extends EnvCommandline {

  private static final Logger logger = LoggerFactory.getLogger(ManagedCommandline.class);

  private Process currentProcess;

  private StreamGobbler stdOutGobbler;
  private StreamGobbler stdErrGobbler;
  private boolean streamLogsToLogging = false;
  private boolean streamLogsToLoggingAndSaved = false;

  /**
   * Constructor which takes a command line string and attempts to parse it into it's various components.
   * 
   * @param command The command
   */
  public ManagedCommandline(String command) {
    super(command);
  }

  /**
   * Default constructor
   */
  public ManagedCommandline() {
    super();
  }

  /**
   * Returns the exit code of the command as reported by the OS.
   * 
   * @return The exit code of the command, if no command was executed, returns -1
   */
  public int getExitCode() {
    return currentProcess != null ? currentProcess.exitValue() : -1;
  }

  public boolean hasTerminated() {
    try {
      currentProcess.exitValue();
      return true;
    } catch (IllegalThreadStateException e) {
      return false;
    }
  }

  /**
   * Returns the stdout from the command as a String
   * 
   * @return The standard output of the command as a <code>String</code>
   */
  public String getStdoutAsString() {
    return stdOutGobbler != null ? stdOutGobbler.getStreamDataAsString() : null;
  }

  /**
   * Returns the stdout from the command as a List of Strings where each String is one line of the output.
   * 
   * @return The standard output of the command as a <code>List</code> of output lines.
   */
  public List<String> getStdoutAsList() {
    return stdOutGobbler != null ? stdOutGobbler.getStreamDataAsList() : null;
  }

  /**
   * Returns the stderr from the command as a String
   * 
   * @return The standard error of the command as a <code>String</code>
   */
  public String getStderrAsString() {
    return stdErrGobbler != null ? stdErrGobbler.getStreamDataAsString() : null;
  }

  /**
   * Returns the stderr from the command as a List of Strings where each String is one line of the error.
   * 
   * @return The standard error of the command as a <code>List</code> of output lines.
   */
  public List<String> getStderrAsList() {
    return stdErrGobbler != null ? stdErrGobbler.getStreamDataAsList() : null;
  }

  /**
   * Clear out the whole command line.
   */
  public void clear() {
    super.clear();
    clearArgs();
  }

  /**
   * Clear out the arguments and stored command output, but leave the executable in place for another operation.
   */
  public void clearArgs() {
    currentProcess = null;
    stdOutGobbler = null;
    stdErrGobbler = null;
    super.clearArgs();
  }

  /**
   * Executes the command.
   */
  public Process execute() throws IOException {

    // Execute the command using the specified environment
    currentProcess = super.execute();

    String prefix = (getExecutable() != null) ? getExecutable() + " - " : "";
    stdOutGobbler = new StreamGobbler(currentProcess.getInputStream(), prefix + "StdOut");
    stdErrGobbler = new StreamGobbler(currentProcess.getErrorStream(), prefix + "StdErr");
    stdOutGobbler.start();
    stdErrGobbler.start();

    // Just to be compatible
    return currentProcess;
  }

  public Process getProcess() {
    return currentProcess;
  }

  public Process waitForProcessFinished() throws IOException, IllegalStateException {
    if (currentProcess == null)
      throw new IllegalStateException("No running process");

    // Wait for the command to complete
    try {
      currentProcess.waitFor();
    } catch (InterruptedException e) {
      logger.error("Thread was interrupted while executing command \"" + this.toString() + "\".", e);
    }

    // make sure last outputs are also captured, even if no new-line was at the
    // end...
    stdOutGobbler.readRemainder();
    stdErrGobbler.readRemainder();

    // Just to be compatible
    return currentProcess;
  }

  private class StreamGobbler extends Thread {

    private final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Create a StreamGobbler.
     * 
     * @param inputStream The stream to read from.
     * @param name The name of this StreamReaderThread, which is useful for debugging.
     */
    StreamGobbler(InputStream inputStream, String name) {
      super(name);
      this.inputStream = inputStream;
      inputStreamReader = new InputStreamReader(inputStream);
      streamDataAsStringBuilder = new StringBuilder();
      streamDataAsList = new ArrayList<String>();
    }

    /**
     * @return the streamDataAsStringBuilder
     */
    public String getStreamDataAsString() {
      return streamDataAsStringBuilder.toString();
    }

    /**
     * @return the streamDataAsList
     */
    public List<String> getStreamDataAsList() {
      return streamDataAsList;
    }

    /**
     * Read lines from the inputStream and append them to the stringBuffer.
     */
    public synchronized void run() {
      if (!inputStreamReaderClosed) {
        try {
          read();
        } catch (IOException e) {
          logger.error("Error reading from stream by Gobbler " + getName(), e);
        }
      }
    }

    // Read line-by-line from the stream until we get to the end of the stream
    private synchronized void read() throws IOException {
      BufferedReader lineByLineReader = new BufferedReader(inputStreamReader);
      String line = null;
      while ((line = lineByLineReader.readLine()) != null) {
        if (streamLogsToLogging) {
          logger.info(getName() + "> " + line);

        } else if (streamLogsToLoggingAndSaved) {
          logger.info(getName() + "> " + line);
          streamDataAsStringBuilder.append(line + LINE_SEPARATOR);
          streamDataAsList.add(line);

        } else {
          streamDataAsStringBuilder.append(line + LINE_SEPARATOR);
          streamDataAsList.add(line);
        }
      }
    }

    // Read any remaining data from the stream until we get to the end of the
    // stream
    public synchronized void readRemainder() throws IOException {
      // We read the data as a char[] instead of using readline()
      // so that we can get strings that do not end in end of
      // line chars.
      char[] chars = new char[80];
      int length; // Number of characters read.

      // Oddly, InputStreamReader.read() will return -1
      // if there is no data present, but the string can still
      // read.
      while (((length = inputStreamReader.read(chars, 0, 80)) != -1)) {
        if (logger.isDebugEnabled()) {
          // Note that ready might be false here since
          // we already read the data.
          logger.debug("_read(): Gobbler '" + getName() + "' Ready: " + inputStreamReader.ready() + " Value: '" + String.valueOf(chars, 0, length) + "'");
        }

        if (streamLogsToLogging) {
          logger.info(getName() + "> " + new String(chars));
        } else if (streamLogsToLoggingAndSaved) {
          logger.info(getName() + "> " + new String(chars));
          streamDataAsStringBuilder.append(chars, 0, length);
          streamDataAsList.add(new String(chars));
        } else {
          streamDataAsStringBuilder.append(chars, 0, length);
          streamDataAsList.add(new String(chars));
        }
      }
    }

    // Stream from which to read.
    private InputStream inputStream;

    // Stream from which to read.
    private InputStreamReader inputStreamReader;

    // Indicator that the stream has been closed.
    private boolean inputStreamReaderClosed = false;

    // StringBuffer to maintain data from configured input stream.
    private StringBuilder streamDataAsStringBuilder;
    private List<String> streamDataAsList;
  }

  public boolean isStreamLogsToLogging() {
    return streamLogsToLogging;
  }

  public void setStreamLogsToLogging(boolean streamLogsToLogging) {
    this.streamLogsToLogging = streamLogsToLogging;
  }

  public boolean isStreamLogsToLoggingAndSaved() {
    return streamLogsToLoggingAndSaved;
  }

  public void setStreamLogsToLoggingAndSaved(boolean streamLogsToLoggingAndSaved) {
    this.streamLogsToLoggingAndSaved = streamLogsToLoggingAndSaved;
  }

  public void setEnv(Map<String, String> env) {
    for (String key : env.keySet()) {
      setVariable(key, env.get(key));
    }
  }

}
