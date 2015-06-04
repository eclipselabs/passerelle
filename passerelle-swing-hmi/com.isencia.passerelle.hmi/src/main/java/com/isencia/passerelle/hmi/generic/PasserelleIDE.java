package com.isencia.passerelle.hmi.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.constants.IPropertyNames;
import com.isencia.util.ClassPath;
import com.isencia.util.commandline.ManagedCommandline;


public class PasserelleIDE {
  private static final String JVM_OPTIONS = "jvm.options";
  private final static String LINE_SEPARATOR = System.getProperty("line.separator");
  public static final String INIFILE_PATH_STRING = System.getProperty("hmi.iniFile", "hmi.ini");

  /**
   * Launcher for the Passerelle HMI/IDE, to be used in shell scripts. This
   * launcher constructs the classpath from all entries in the lib folder and
   * subfolders, reads and constructs some system properties and then launches
   * the PasserelleIDEMain. Following system properties can be set in the shell
   * script :
   * <ul>
   * <li>hmi.debug : when set to true, this will launch PasserelleIDEMain ready
   * for remote debugging on port 8000. Default value (when not set) is false.</li>
   * <li>hmi.modelURL : when set, it should point to a folder/URL where
   * predefined Passerelle models are available. These will be shown in the File
   * > Models menu, in the top section. Default value (when not set) is empty,
   * in which case the File > Models menu will only show recently used models.</li>
   * <li>hmi.filterDef : points to the file where the HMI filter definitions and
   * layout options will be stored. By default these are stored in
   * conf/filter_def.xml.</li>
   * </ul>
   * 
   * @param args
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException, IOException {
    String javaHome = args[0];
    String passerelleHome = args[1];
    String jvmProps = "";
    Properties log4jProps = new Properties();
    log4jProps.setProperty("log4j.rootLogger", "INFO,LAUNCHER_LOGFILE");
    log4jProps.setProperty("log4j.appender.LAUNCHER_LOGFILE", "org.apache.log4j.FileAppender");
    log4jProps.setProperty("log4j.appender.LAUNCHER_LOGFILE.File", passerelleHome + "/logs/passerelle-HMI-launcher.log");
    log4jProps.setProperty("log4j.appender.LAUNCHER_LOGFILE.Append", "true");
    log4jProps.setProperty("log4j.appender.LAUNCHER_LOGFILE.layout", "org.apache.log4j.PatternLayout");
    log4jProps.setProperty("log4j.appender.LAUNCHER_LOGFILE.layout.ConversionPattern", "%d %t %-5p %x %c{1} - %m%n");
    Logger logger = LoggerFactory.getLogger(PasserelleIDE.class);

    PropertyConfigurator.configure(log4jProps);

    String execStr = "'" + javaHome + "/bin/java' ";

    String confFolder = passerelleHome + "/"+IPropertyNames.APP_CFG_DEFAULT;

    File iniPropsFile = new File(INIFILE_PATH_STRING);
    if (iniPropsFile.exists()) {
      InputStream propsInput = null;
      try {
        propsInput = new FileInputStream(iniPropsFile);
        Properties initProps = new Properties();
        initProps.load(propsInput);
        logger.info("HMI configured from " + iniPropsFile.getCanonicalPath());

        jvmProps = initProps.getProperty(JVM_OPTIONS, "");
        execStr += jvmProps + " ";

        for (Object _propKey : initProps.keySet()) {
          String propKey = (String) _propKey;
          if (!JVM_OPTIONS.equalsIgnoreCase(propKey)) {
            // System.setProperty(propKey, initProps.getProperty(propKey));
            execStr += " -D" + propKey + "=" + initProps.getProperty(propKey);
          }
        }
      } catch (Exception e) {
        logger.error("Error setting initialisation properties", e);
      } finally {
        if (propsInput != null) try {
          propsInput.close();
        } catch (Exception e) {
        }
      }
    }

    // take the default passerelle IDE libs and the ones in additional
    // subfolders
    ClassPath libClassPath = new ClassPath(passerelleHome + "/lib");

    String systemClassPath = System.getProperty("PASSERELLE_CLASSPATH");
    String classPath = confFolder + ";" + passerelleHome + "/user;" + libClassPath + (systemClassPath != null ? systemClassPath : "");

    execStr = execStr + " -D" + IPropertyNames.APP_HOME + "=" + passerelleHome + " -Dlog4j.configuration=file:" + confFolder + "/log4j.properties" + " -cp "
        + classPath + " com.isencia.passerelle.hmi.generic.PasserelleIDEMain";

    ManagedCommandline cmdLine = new ManagedCommandline(execStr);
    try {
      Process process = cmdLine.execute();
      logger.info("Started Passerelle HMI, using" + LINE_SEPARATOR + "\t" + execStr);
      process = cmdLine.waitForProcessFinished();
      int waitFor = process.waitFor();
      logger.info("exit code " + waitFor);
      logger.info("Process output" + LINE_SEPARATOR + "\t" + cmdLine.getStdoutAsString());
      logger.error("Process error" + LINE_SEPARATOR + "\t" + cmdLine.getStderrAsString());
    } catch (Exception e) {
      logger.error("Error starting Passerelle HMI subprocess", e);
    }
  }
}
