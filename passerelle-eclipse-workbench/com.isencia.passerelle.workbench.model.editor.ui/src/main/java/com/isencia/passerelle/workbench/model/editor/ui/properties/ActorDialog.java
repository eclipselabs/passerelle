package com.isencia.passerelle.workbench.model.editor.ui.properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesTableViewer;

public class ActorDialog extends Dialog {
	
	private NamedObj actor;
  private IWorkbenchPart part;
  private ActorAttributesTableViewer viewer;
	
  public ActorDialog(IWorkbenchPart part, NamedObj actor) {
    super(part.getSite().getShell());
    setShellStyle(SWT.RESIZE | SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
    this.actor = actor;
    this.part = part;
  }

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		this.viewer = new ActorAttributesTableViewer(actor,part,composite,
		    SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
	  data.heightHint = 200;
	  viewer.getTable().setLayoutData(data);
		return composite;
	}


	public void create() {
		super.create();
		getShell().setText("Edit Attributes of '"+actor.getDisplayName()+"'");
		viewer.createTableModel(part, actor);
	}
	
	@Override
	public boolean close() {
	  viewer.createTableModel(part, null);
	  return super.close();
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Close", true);
	}
}
