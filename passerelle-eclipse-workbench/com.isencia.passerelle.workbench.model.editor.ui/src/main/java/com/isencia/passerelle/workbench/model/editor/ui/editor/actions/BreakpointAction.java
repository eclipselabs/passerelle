package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView;
import com.isencia.passerelle.workbench.model.ui.command.AttributeCommand;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class BreakpointAction extends SelectionAction {
	
	private static final Logger logger = LoggerFactory.getLogger(BreakpointAction.class);
	
	public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.editor.actions.breakPointAction";
	
	private PasserelleModelMultiPageEditor parent;

	public BreakpointAction(IEditorPart part, PasserelleModelMultiPageEditor parent) {
		super(part);
		this.parent = parent;
		setLazyEnablementCalculation(true);
	}

	private final String icon = "icons/break_point.png";

	@Override
	protected void init() {
		super.init();
		
		setText("Toggle actor breakpoint");
		setId(ID);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(false);
	}

	private ActorEditPart editorPart;
	
	@Override
	protected boolean calculateEnabled() {
		return editorPart!=null && editorPart.isDebuggable();
	}

	@Override
	public void run() {

		Actor actor = (Actor)editorPart.getActor();

		final Attribute att = actor.getAttribute("_break_point");
		if (att == null) return;
		
		try {
			ActorAttributesView view = (ActorAttributesView)EclipseUtils.getActivePage().findView("com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView");
			AttributeCommand cmd = new AttributeCommand(view!=null?view.getViewer():null, att, !getBooleanValue(att,false));
			parent.getEditor().getEditDomain().getCommandStack().execute(cmd);
			parent.refreshActions();
			editorPart.refresh();
			
		} catch (IllegalActionException e) {
			logger.error("Cannot set debug attribute!", e);
		}

	}
	
	private static Boolean getBooleanValue(Attribute att, boolean defaultValue) {
		try {
			if (att==null) return defaultValue;
			if (!(att instanceof Parameter)) return defaultValue;
			Token tok = ((Parameter)att).getToken();
			if (!(tok instanceof BooleanToken)) return defaultValue;
			return ((BooleanToken)tok).booleanValue();
		} catch (Throwable ne) {
			return defaultValue;
		}
	}

	@Override
	protected void setSelection(ISelection selection) {
		super.setSelection(selection);
		if (selection instanceof StructuredSelection) {
			StructuredSelection ss = (StructuredSelection)selection;
			editorPart = ss.getFirstElement() instanceof ActorEditPart
					   ? (ActorEditPart)ss.getFirstElement()
					   : null;
		}
	}

}
