package com.isencia.passerelle.workbench.model.actor;

import org.eclipse.ui.IWorkbenchPart;
import com.isencia.passerelle.eclipse.resources.actor.ResourceObject;

public interface IPartListenerActor {

	/**
	 * Notify if a part associated with this actor is opened.
	 * @param part
	 */
	public void partPreopen(ResourceObject ob);

	/**
	 * Notify if a part associated with this actor is opened.
	 * @param part
	 */
	public void partOpened(IWorkbenchPart part, ResourceObject ob);

}
