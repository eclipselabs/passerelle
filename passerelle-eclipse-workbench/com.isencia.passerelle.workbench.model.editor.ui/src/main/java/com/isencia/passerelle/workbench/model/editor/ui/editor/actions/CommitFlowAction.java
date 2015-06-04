package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.kernel.Entity;

import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.wizards.NameChecker;

public class CommitFlowAction extends SelectionAction implements NameChecker {

	private static final Logger logger = LoggerFactory
			.getLogger(CommitFlowAction.class);
	public static String ID = "CommitFlowAction";
	private PasserelleModelMultiPageEditor parent;
	private final String icon = "icons/export.gif";
	public static String CREATE_SUBMODEL = "createSubModel";

	/**
	 * Creates an empty model
	 * 
	 * @param part
	 */
	public CommitFlowAction() {
		this(null, null);
		setId(ID);
	}

	/**
	 * Creates an empty model
	 * 
	 * @param part
	 */
	public CommitFlowAction(final IEditorPart part) {
		this(part, null);
		setId(ID);
	}

	/**
	 * Creates the model from the contents of the part
	 * 
	 * @param part
	 * @param parent
	 */
	public CommitFlowAction(final IEditorPart part,
			final PasserelleModelMultiPageEditor parent) {
		super(part);
		this.parent = parent;
		setLazyEnablementCalculation(true);
		if (parent != null)
				setId(ID);
	}

	@Override
	protected void init() {
		super.init();
		Activator.getImageDescriptor(icon);
		setHoverImageDescriptor(Activator.getImageDescriptor(icon));
		setImageDescriptor(Activator.getImageDescriptor(icon));
		setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
		setEnabled(false);

	}

	@Override
	public void run() {
		try {
			if (parent != null) {
				final Entity entity = parent.getSelectedContainer();
				Flow flow = (Flow) entity.toplevel();
				Activator.getDefault().getRepositoryService()
						.commitFlow(flow, "from workbench");

			}

		} catch (Exception e) {
			logger.error("Cannot commit flow", e);
		}
	}

	public boolean isNameValid(String name) {
		return true;
	}

	public String getErrorMessage(String name) {
		return null;
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

}
