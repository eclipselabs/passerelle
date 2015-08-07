package com.isencia.passerelle.eclipse.resources.actor;

import org.eclipse.core.resources.IResource;





/**
 * Actors may be marked as IResourceActor.
 * 
 * In this case it is an actor that acts on a file or has a file as a 
 * key part of its information which can be opened.
 * 
 * @author gerring
 *
 */
public interface IResourceActor {

	/**
	 * This method is called to set the moml file which
	 * is interacting with the IResourceActor.
	 * 
	 * In this way the actor knows which moml file part
	 * is requesting which of its resources. It is called
	 * before getResource(...)
	 * 
	 * @param iFile
	 */
	public void setMomlResource(IResource momlFile);

	/**
	 * 
	 * @return
	 */
	public int getResourceCount();
	
	/**
	 * 
	 * @param iresource
	 * @return
	 * @throws Exception 
	 */
	public ResourceObject getResource(final int iresource) throws Exception;

	

}
