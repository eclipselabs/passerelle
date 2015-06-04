package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;

public class ExecutionFactory {

	public static void createWorkflowActions(IActionBars actionBars) {
		
		actionBars.getToolBarManager().add(new Separator(ExecutionFactory.class.getName()+"Group"));
		createAction(new RunAction(), actionBars);
		createAction(new StopAction(), actionBars);
	}

	private static void createAction(IAction action, IActionBars actionBars) {
		if (actionBars.getToolBarManager().find(action.getId())==null) {
			actionBars.getToolBarManager().add(action);
		}
	}

}
