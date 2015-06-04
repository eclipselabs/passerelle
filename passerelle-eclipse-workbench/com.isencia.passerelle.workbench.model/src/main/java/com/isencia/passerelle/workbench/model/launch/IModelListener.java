package com.isencia.passerelle.workbench.model.launch;

public interface IModelListener {

	public void executionStarted();

	public void executionTerminated(final int returnCode);
}
