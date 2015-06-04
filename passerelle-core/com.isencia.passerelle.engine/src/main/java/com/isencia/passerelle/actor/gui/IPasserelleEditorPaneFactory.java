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
package com.isencia.passerelle.actor.gui;

import com.isencia.passerelle.actor.gui.PasserelleQuery.QueryLabelProvider;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public interface IPasserelleEditorPaneFactory {

    /**
     * a callback interface to be able to plug-in custom checks
     * whether a given parameter should be visible in an editor or not
     *
     * @author erwin
     */
	public static interface ParameterEditorAuthorizer {
		boolean allowRename();
        boolean isAuthorizedForEditor(Settable p);
    }
	
	
	/** Return a new default widget for configuring the specified object.
	 *  This is used by the Configurer for objects that do not contain
	 *  an instance of EditorPaneFactory as an attribute. 
	 *  
	 *  The contents of the editor pane are filtered according to the Mode
	 *  of the request. 
	 *  <ul>
	 *  <li> In EXPERT mode, all parameters are shown.
	 *  <li> In DEFAULT mode, all parameters that are restricted for EXPERT are shown.
	 *  <li> In CONFIG mode, only parameters that are registered as CONFIGURABLE parameters are shown.
	 *  </ul>
	 *  
	 *  @return An instance of the PasserelleQuery class that is created
	 *  with styles according to the type given in each visible attribute.
	 */
	IPasserelleQuery createEditorPaneInMode(NamedObj object, Mode mode);

	/** Return a new default widget for configuring the specified object.
	 *  This is used by the Configurer for objects that do not contain
	 *  an instance of EditorPaneFactory as an attribute. 
	 *  
	 *  The contents of the editor pane are filtered according to the
	 *  authorizer that is given.
	 *  
	 *  An optional labelprovider can be used to customize parameter labels
	 *  in the UI.
	 *  
	 *  @return An instance of the PasserelleQuery class that is created
	 *  with styles according to the type given in each visible attribute.
	 */
	IPasserelleQuery createEditorPaneWithAuthorizer(NamedObj object, ParameterEditorAuthorizer authorizer,
			QueryLabelProvider labelProvider);

}