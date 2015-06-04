package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import ptolemy.actor.CompositeActor;

import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;

public class OutlinePartFactory implements EditPartFactory {
	private PasserelleModelMultiPageEditor editor;
	private Set<OutlineEditPart> parts = new HashSet<OutlineEditPart>();

	public Set<OutlineEditPart> getParts() {
		return parts;
	}

	public OutlinePartFactory(PasserelleModelMultiPageEditor editor) {
		super();
		this.editor = editor;
	}

	public EditPart createEditPart(EditPart context, Object model) {
		OutlineEditPart editPart = null;
		if (model instanceof CompositeActor)
			editPart = new OutlineContainerEditPart(context, model, editor);
		else
		  editPart = new OutlineEditPart(model);
		parts.add(editPart);
		return editPart;
	}

}
