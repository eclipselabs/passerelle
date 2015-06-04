/* Copyright 2010 - iSencia Belgium NV

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

package com.isencia.passerelle.util.ptolemy;


import javax.swing.filechooser.FileFilter;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.util.ExtensionFileFilter;

/**
 * An extension on the std FileParameter, that allows to define
 * a file filter by specifying the set of allowed extensions.
 * <br/>
 * As a consequence, the configuration tool(s) should limit the
 * possible file selections to files with one of the given extensions.
 * <p>
 * Optionally, we might consider to add validation logic in this
 * parameter class, so that it checks any value that is being set,
 * whether it complies with the configured filter.
 * <br/>
 * Currently, this is not yet implemented. So we really depend on correct
 * implementation(s) of the configuration tool(s) (e.g. the Query dialog).
 * </p>
 * @author erwin
 *
 */
public class ResourceParameter extends ptolemy.data.expr.FileParameter {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4667276734559946020L;
	
	private ExtensionFileFilter fileFilter = null;

	/**
	 * @param container
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public ResourceParameter(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
		super(container, name);
	}
	
	/**
	 * A constructor that limits the files that a configuration tool should allow for this parameter instance.
	 * 
	 * @param container
	 * @param name
	 * @param filterName the name that a FileFilter should show in a FileChooser, for the allowed file types
	 * @param filterExtensions the extensions that a FileChooser should allow for the desired file types
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public ResourceParameter(NamedObj container, String name, String filterName, String... filterExtensions) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		if(filterExtensions!=null && filterExtensions.length>0) {
			fileFilter = new ExtensionFileFilter(filterExtensions);
			fileFilter.setDescription(filterName);
		}
	}
	
	public boolean hasFilter() {
		return fileFilter!=null;
	}
	
	public FileFilter getFilter() {
		return fileFilter;
	}

	private int resourceType = 0;

	/**
	 * Will 
	 * @return
	 */
	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}
	
}
