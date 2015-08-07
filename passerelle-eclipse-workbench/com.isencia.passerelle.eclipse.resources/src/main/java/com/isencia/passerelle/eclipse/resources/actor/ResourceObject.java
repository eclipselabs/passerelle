package com.isencia.passerelle.eclipse.resources.actor;

public class ResourceObject {

	private Object resource;
	private String resourceTypeName;
	private String editorId;
	
	public String getEditorId() {
		return editorId;
	}

	public void setEditorId(String editorId) {
		this.editorId = editorId;
	}

	public void setResource(Object resource) {
		this.resource = resource;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	/**
	 * Either returns an IResource for files local to the
	 * workspace or returns an File for external files.
	 * 
	 * @return
	 */
	public Object getResource() {
		return resource;
	}
	
	/**
	 * Appears in the workbench actions, on right click for instance.
	 * @return
	 */
	public String getResourceTypeName() {
		return resourceTypeName;
	}

}
