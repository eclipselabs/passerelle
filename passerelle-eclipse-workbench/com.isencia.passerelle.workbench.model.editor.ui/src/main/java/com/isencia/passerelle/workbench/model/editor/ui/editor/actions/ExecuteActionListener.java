package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.EventListener;

public interface ExecuteActionListener extends EventListener {

	public void buttonRefreshRequested(final ExecuteActionEvent evt);

	public void executionRequested(ExecuteActionEvent evt);

	public void stopRequested(ExecuteActionEvent evt);
}
