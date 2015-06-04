/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class PasserelleGraphitiEditorPlugin extends AbstractUIPlugin {

  private static PasserelleGraphitiEditorPlugin pluginInstance;
  
  public PasserelleGraphitiEditorPlugin() {
    pluginInstance = this;
  }
  
  public static PasserelleGraphitiEditorPlugin getDefault() {
    return pluginInstance;
  }

  /**
   * Returns the Plugin-ID.
   * 
   * @return The Plugin-ID.
   */
  public static String getID() {
    return getDefault().getBundle().getSymbolicName();
  }

}
