
package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Class behaves like swing ButtonGroup because I 
 * could not find a jface class for this. Probably
 * because Actions are not very commonly uses and
 * actions are configured via the plugin.xml
 */
public class CheckableActionGroup implements IPropertyChangeListener {

	private Collection<IAction> actions = new ArrayList<IAction>(7);
	
	public void add(IAction action) {
		if (action.getStyle() != IAction.AS_CHECK_BOX) throw new RuntimeException("Only check actions are supported!");
		action.addPropertyChangeListener(this);
		actions.add(action);
	}

	private boolean off = false;

	public void propertyChange(PropertyChangeEvent event) {
		if (off) return;
		try {
			off = true;
			final Action action = (Action)event.getSource();
			final Collection<IAction> others = new ArrayList<IAction>(actions);
			others.remove(action);
			action.setChecked(true);
			for (IAction other : others) other.setChecked(false);
		} finally {
			off = false;
		}
	}
	
	public void dipose() {
		actions.clear();
	}
}
