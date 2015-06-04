package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.util.List;
import java.util.Locale;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.Constants;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.OutlineEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class DynamicHelpAction extends SelectionAction {
	private PasserelleModelMultiPageEditor parent;

	public DynamicHelpAction(IEditorPart part, PasserelleModelMultiPageEditor parent) {
		super(part);
		this.parent = parent;
		setLazyEnablementCalculation(true);
	}

	private final String icon = "icons/help.gif";

	@Override
	protected void init() {
		super.init();
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setText("Help");
		setId(ActionFactory.DYNAMIC_HELP.getId());
		Activator.getImageDescriptor(icon);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(false);

	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	public void run() {
		List selection = getSelectedObjects();

		for (Object o : selection) {
			if (o instanceof OutlineEditPart) {
				OutlineEditPart out = (OutlineEditPart) o;

				Object model = out.getModel();
				if (model instanceof Attribute) {
					Attribute attr = (Attribute) model;
					NamedObj container = attr.getContainer();
					String actorName = container.getClass().getName();
					displayDynamicHelp(actorName + "_" + attr.getName());

				} else {
					String actorName = model.getClass().getName();

					displayDynamicHelp(actorName);

				}
				break;

			} else if (o instanceof ActorEditPart) {
				String actorName = ((ActorEditPart) o).getEntity().getClass()
						.getName();
				displayDynamicHelp(actorName);
				break;
			}
		}

	}

	private void displayDynamicHelp(String actorName) {
		actorName = actorName.replace(".", "_");
		WorkbenchHelp.displayHelp(Constants.HELP_BUNDLE_ID + "." + actorName);
	}

	@Override
	protected void setSelection(ISelection selection) {
		// TODO Auto-generated method stub
		super.setSelection(selection);
	}

}
