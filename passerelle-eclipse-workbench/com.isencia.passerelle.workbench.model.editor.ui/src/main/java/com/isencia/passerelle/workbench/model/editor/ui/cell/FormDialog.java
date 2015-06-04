package com.isencia.passerelle.workbench.model.editor.ui.cell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class FormDialog extends org.eclipse.ui.forms.FormDialog {

	@Override
	protected void createFormContent(IManagedForm mform) {
		mform.getForm().setText("Properties");
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = new FormToolkit(getShell().getDisplay());

		GridLayout layout = new GridLayout();
		form.getBody().setLayout(layout);

		final String href = getHelpResource();
		if (href != null) {
			IToolBarManager manager = form.getToolBarManager();
			Action helpAction = new Action("help") { //$NON-NLS-1$
				public void run() {
					BusyIndicator.showWhile(form.getDisplay(), new Runnable() {
						public void run() {
							PlatformUI.getWorkbench().getHelpSystem()
									.displayHelpResource(href);
						}
					});
				}
			};
			helpAction.setToolTipText(PDEUIMessages.PDEFormPage_help);
			helpAction.setImageDescriptor(PDEPluginImages.DESC_HELP);
			manager.add(helpAction);
			form.updateToolBar();

		}

	}

	protected String getHelpResource() {
		return null;
	}

	public FormDialog(Shell shell) {
		super(shell);
		// TODO Auto-generated constructor stub
	}

}
