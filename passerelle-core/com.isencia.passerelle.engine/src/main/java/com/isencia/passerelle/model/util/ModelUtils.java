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
package com.isencia.passerelle.model.util;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;

public class ModelUtils {

  /**
   * @param rootEntity
   * @param propName
   * @return
   * @throws IllegalActionException
   */
  public static Parameter getParameter(final CompositeActor rootEntity, final String propName) throws IllegalActionException {
    final String[] parts = propName.split("[\\.=]");
    if (parts.length == 0) {
      throw new IllegalActionException("Invalid parameter name " + propName);
    }
    Entity e = rootEntity;
    // parts[parts.length-1] is the parameter name
    // all the parts[] before that are part of the nested Parameter name
    for (int j = 0; j < parts.length - 1; j++) {
      if (e instanceof CompositeActor) {
        e = ((CompositeActor) e).getEntity(parts[j]);
        if (e == null) {
          throw new IllegalActionException("Invalid parameter name " + propName);
        }
      } else {
        break;
      }
    }
    final Parameter p = (Parameter) e.getAttribute(parts[parts.length - 1], Parameter.class);
    return p;
  }

  public static String getFullNameButWithoutModelName(final CompositeActor model, final Nameable namedObject) {
    // We need to obtain the full name of the NamedObj,
    // but without the model name in it.
    // But the namedObject might also be the root CompositeActor of the model,
    // in which case the Ptolemy name in fullName is the same as the model name.
    // Then we don't need to chop it.
		if (namedObject == null || (model!=null && namedObject.getName().equals(model.getName()))) {
      return "";
    }
    String fullName = namedObject.getFullName();
		if(model==null) {
			// just chop off leading '.'
			fullName = fullName.substring(1);
		} else if (model.getName()!=null && (fullName.length() > model.getName().length() + 1)) {
      final int i = fullName.indexOf(model.getName());
      if (i > 0) {
        // there's always an extra '.' in front of the model name...
        // and a trailing '.' just behind it...
        fullName = fullName.substring(i + model.getName().length() + 1);
      }
    }
    return fullName;
  }

  public static String getFullNameButWithoutModelName(final CompositeActor model, String fullName) {
    // we need to obtain the full name of the NamedObj,
    // but without the model name in it
		if(model==null || model.getName()==null)
			return fullName;
    final int i = fullName.indexOf(model.getName());
    if (i > 0) {
      // there's always an extra '.' in front of the model name...
      // and a trailing '.' just behind it...
      fullName = fullName.substring(i + model.getName().length() + 1);
    }
    return fullName;
  }
}
