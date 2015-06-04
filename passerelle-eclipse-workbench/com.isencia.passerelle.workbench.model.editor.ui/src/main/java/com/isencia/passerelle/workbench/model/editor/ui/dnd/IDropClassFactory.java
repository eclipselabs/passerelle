package com.isencia.passerelle.workbench.model.editor.ui.dnd;

import org.eclipse.core.resources.IResource;

import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;

import ptolemy.kernel.util.NamedObj;

public interface IDropClassFactory {

	public Class<? extends NamedObj> getClassForPath(final IResource selected, final String filePath);

	/**
	 * 
	 * @param filePath
	 */
	public void setConfigurableParameters(CreateComponentCommand cmd, String filePath);
}
