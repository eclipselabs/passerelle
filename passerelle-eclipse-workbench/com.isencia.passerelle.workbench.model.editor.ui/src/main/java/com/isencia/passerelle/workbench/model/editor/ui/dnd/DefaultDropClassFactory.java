package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import org.eclipse.core.resources.IResource;

import com.isencia.passerelle.actor.io.FileReader;
import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;

import ptolemy.kernel.util.NamedObj;

/**
 * This factory should be overridden with user defined actors for different file types.
 * @author gerring
 *
 */
public class DefaultDropClassFactory implements IDropClassFactory {

	public Class<? extends NamedObj> getClassForPath(IResource source, String filePath) {
		return FileReader.class;
	}

	
	public void setConfigurableParameters(CreateComponentCommand cmd,
			String filePath) {
		// Nothing to do
	}

}
