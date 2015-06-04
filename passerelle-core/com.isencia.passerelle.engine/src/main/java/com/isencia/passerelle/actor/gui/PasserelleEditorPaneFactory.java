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

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.PasserelleQuery.QueryLabelProvider;

import ptolemy.actor.gui.EditorPaneFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

/**
 * PasserelleEditorPaneFactory
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class PasserelleEditorPaneFactory extends EditorPaneFactory implements IPasserelleEditorPaneFactory {
    
    
	public PasserelleEditorPaneFactory() throws IllegalActionException, NameDuplicationException{
		super(new NamedObj(), "NotReferenced");
	}

	
	/**
	 * This Constructor is used by the ptolemy code only.
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public PasserelleEditorPaneFactory(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}

    /** 
     * Used by the ptolemy code only.
     * 
     *  Return a new widget for configuring the container.  In this
     *  base class, this method defers to the static createEditorPane method.
     *  Subclasses that implement specialized interaction should override
     *  this method to create an appropriate type of component.
     *  @return A new widget for configuring the container.
     */
    public Component createEditorPane() {
        return (Component)createPasserelleQueryEditorPane((NamedObj)getContainer()).getPasserelleComponent();
    }

    /** 
     * !!!!!!!!! Not used anymore. !!!!!!!!!!!
     * Return a new default widget for configuring the specified object.
     *  This is used by the Configurer for objects that do not contain
     *  an instance of EditorPaneFactory as an attribute.  
     *  
     *  @return An instance of the PasserelleQuery class that is created
     *  with styles according to the type given in each visible attribute.
     */
    public static Component createEditorPane(NamedObj object) {
    	throw new RuntimeException("static Method 'createEditorPane(NamedObj object)' not usable anymore");
        //return (Component)createPasserelleQueryEditorPane(object);
    }

    
    /**
     * The default method for constructing a std config dialog.
     * Specific for Passerelle, to support flexible dialog implementations and customizations.
     * 
     * @param object
     * @return
     */
    public IPasserelleQuery createPasserelleQueryEditorPane(NamedObj object) {
        List<Settable> parameters = object.attributeList(Settable.class);
        return _createEditorPane(object, parameters, null, null);
    }

    /**
     * Overridable method that does the actual construction of the dialog "query" component.
     * 
     * @param object
     * @param parameters
     * @param labelProvider helper that is able to define labels/aliases for the parameters when needed
     * @param authorizer helper that blocks/allows certain parameters based on implementation-specific criteria
     * @return
     */
	protected IPasserelleQuery _createEditorPane(NamedObj object, Iterable<Settable> parameters, QueryLabelProvider labelProvider, ParameterEditorAuthorizer authorizer) {
		boolean allowRename = authorizer!=null?authorizer.allowRename():true; 
		PasserelleQuery query = new PasserelleQuery(object, labelProvider, allowRename);
        query.setTextWidth(20);

        boolean foundOne = false;
        for (Settable parameter : parameters) {
            if (isParameterVisible(object, parameter, authorizer)) {
                foundOne = true;
                query.addStyledEntry(parameter);
            }
        }
        if (!foundOne && !allowRename) {
            return new PasserelleEmptyQuery(object.getName() + " has no parameters.");
        }
        return query;
	}
    
    public  IPasserelleQuery createEditorPaneInMode(NamedObj object, Mode mode) {
        if(object == null || !(object instanceof Actor)) {
            // this must be handled somehow by the "classic" implementation
            return createPasserelleQueryEditorPane(object);
        } else if(Mode.EXPERT.equals(mode) || Mode.DEFAULT.equals(mode)) {
            // this is handled OK by the "classic" implementation
            return createPasserelleQueryEditorPane(object);
        } else {
            // Mode.CONFIGURE is something special
            Actor actor = (Actor) object;
            Settable[] parameters = actor.getConfigurableParameters();
            // in order to use a common method that does the actual construction of the query component
            // and because actor.getConfigurableParameters() returns an array, we need to do a slightly
            // annoying conversion to a list again in here...
            return _createEditorPane(object, Arrays.asList(parameters), null, null);
        }
    }

    public IPasserelleQuery createEditorPaneWithAuthorizer(NamedObj object, ParameterEditorAuthorizer authorizer, QueryLabelProvider labelProvider) {
        if(object == null) {
            // this must be handled somehow by the "classic" implementation
            return createPasserelleQueryEditorPane(object);
        } else {
            List<Settable> parameters = object.attributeList(Settable.class);
            return _createEditorPane(object, parameters, labelProvider, authorizer);
        }
    }

    /**
     * Overridable utility method that determines whether the given parameter should be shown in the config dialog or not.
     * <br>
     * The decision is based on the parameter visibility according to the "mode" (expert, ...)
     * and to what the (optionally) given authorizer allows.
     * 
     * @param object
     * @param parameter
     * @param authorizer
     * @return
     */
	protected boolean isParameterVisible(NamedObj object, Settable parameter, ParameterEditorAuthorizer authorizer) {
		return PasserelleConfigurer.isVisible(object, parameter) && (authorizer==null || authorizer.isAuthorizedForEditor(parameter));
	}
}
