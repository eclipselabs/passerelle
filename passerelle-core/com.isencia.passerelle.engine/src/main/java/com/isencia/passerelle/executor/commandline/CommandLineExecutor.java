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

package com.isencia.passerelle.executor.commandline;

import java.io.File;
import com.isencia.constants.IPropertyNames;
import com.isencia.util.ClassPath;
import com.isencia.util.RuntimeStreamReader;
import com.isencia.util.commandline.EnvCommandline;


/**
 * CommandLineExecutor that allows to override model parameter settings from the cmd-line
 * using the runAssembly.bat script
 * 
 * @author delerw
 */
public class CommandLineExecutor {
    /**
     * Executes a model. The CommandLineExecutor first builds the complete classpath dynamicaly,
     * starts a new JVM with the new classpath and the EngineExecutor.
     *
     * @param args 
     * <ul>
     * <li> 0 : Java runtime executable<br>
     * <li> 1 : Passerelle installation Home folder
     * <li> 2 : model path
     * <li> 3... : parameter overrides in "name=value" format
     * </ul>
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("No model specified");
            System.exit(1);
        }

        String javaExec = args[0];
        String passerelleHome = args[1];
        String modelName = args[2];
        String[] parameterOverrides = new String[args.length-3];
        for (int i = 3; i < args.length; i++) {
        	parameterOverrides[i-3]= args[i];
		}
        
        ClassPath libClassPath = new ClassPath(passerelleHome + File.separator + "lib");
        String systemClassPath = System.getProperty("PASSERELLE_CLASSPATH");
        String classPath = libClassPath.toString() + (systemClassPath!=null?systemClassPath:"");
        String execStr = javaExec  
        					+ " -cp " + classPath 
        					+ " -D"+IPropertyNames.APP_HOME+"=" + passerelleHome 
        					+ " -Dlog4j.configuration=file:" + passerelleHome + "/"+IPropertyNames.APP_CFG_DEFAULT+"/log4j.properties";

        EnvCommandline commandline = new EnvCommandline(execStr);
        commandline.createArgument().setValue("com.isencia.passerelle.executor.AsyncModelExecutor");
        commandline.createArgument().setValue(modelName);
        commandline.addArguments(parameterOverrides);
        
        Object errorStreamLock = new Object();
        Object outputStreamLock = new Object();

        try {
        	Process process = commandline.execute();
            synchronized (errorStreamLock) {
                synchronized (outputStreamLock) {
                    // any output message ?
                    RuntimeStreamReader outputStream = new RuntimeStreamReader(outputStreamLock, process.getInputStream(), RuntimeStreamReader.Type.output, System.out);

                    // any error message ?
                    RuntimeStreamReader errorStream = new RuntimeStreamReader(errorStreamLock, process.getErrorStream(), RuntimeStreamReader.Type.error, System.err);

                    outputStream.start();
                    errorStream.start();

                    outputStreamLock.wait();
                }

                errorStreamLock.wait();
            }
            System.out.println("Model Execution finished");
            System.exit(process.exitValue());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}