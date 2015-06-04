/* Copyright 2010 - European Synchrotron Radiation Facility

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.workbench.model.editor.ui.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.ExecuteActionEvent;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.ExecuteActionListener;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RunAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.StopAction;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeView;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;

public class WizardModelEditor extends EditorPart implements ExecuteActionListener {
	

	private static final Logger logger = LoggerFactory.getLogger(WizardModelEditor.class);

	public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.editor.WizardModelEditor"; //$NON-NLS-1$
	
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

	private Text       actorText;
	private RunAction  runAction;
	private StopAction stopAction;
	private Button     btnStart, btnStop;
	private Form       frmRunWorkflow;


	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	/**
	 * Create contents of the editor part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {		
		
		final IToolBarManager toolMan= getEditorSite().getActionBars().getToolBarManager();
		final ActionContributionItem run  = (ActionContributionItem)toolMan.find(RunAction.class.getName());
		final ActionContributionItem stop = (ActionContributionItem)toolMan.find(StopAction.class.getName());
		
		this.runAction  = (RunAction)run.getAction();
		this.stopAction = (StopAction)stop.getAction();
		runAction.addExecuteActionListener(this);
	
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		this.frmRunWorkflow = formToolkit.createForm(container);
		formToolkit.paintBordersFor(frmRunWorkflow);
		frmRunWorkflow.setText("Run '"+getEditorInput().getName()+"'");
		
		Label lblPleaseUseThe = formToolkit.createLabel(frmRunWorkflow.getBody(), "Please use the start and stop buttons for running this workflow.", SWT.NONE);
		lblPleaseUseThe.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		lblPleaseUseThe.setFont(SWTResourceManager.getFont("Sans", 10, SWT.NORMAL));
		lblPleaseUseThe.setBounds(10, 10, 554, 17);
		
		final Section sctnRunningActor = formToolkit.createSection(frmRunWorkflow.getBody(), Section.TWISTIE | Section.TITLE_BAR);
		sctnRunningActor.setBounds(10, 129, 740, 142);
		formToolkit.paintBordersFor(sctnRunningActor);
		sctnRunningActor.setText("Progress");
		
		actorText = formToolkit.createText(sctnRunningActor, "", SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		sctnRunningActor.setClient(actorText);
		sctnRunningActor.setExpanded(true);
		
		final Section sctnStartStop = formToolkit.createSection(frmRunWorkflow.getBody(), Section.TWISTIE | Section.TITLE_BAR);
		sctnStartStop.setBounds(10, 54, 740, 59);
		formToolkit.paintBordersFor(sctnStartStop);
		sctnStartStop.setText("Controls");
		sctnStartStop.setExpanded(true);
		
		Composite composite = formToolkit.createComposite(sctnStartStop, SWT.NONE);
		formToolkit.paintBordersFor(composite);
		sctnStartStop.setClient(composite);
		
		this.btnStart = formToolkit.createButton(composite, "Start", SWT.NONE);
		btnStart.setImage(ResourceManager.getPluginImage("com.isencia.passerelle.workbench.model.editor.ui", "icons/run_workflow.gif"));
		btnStart.setBounds(10, 0, 88, 29);
		btnStart.setEnabled(runAction.isEnabled());
		btnStart.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				runModel();
			}
		});
		
		this.btnStop = formToolkit.createButton(composite, "Stop", SWT.NONE);
		btnStop.setImage(ResourceManager.getPluginImage("com.isencia.passerelle.workbench.model.editor.ui", "icons/stop_workflow.gif"));
		btnStop.setBounds(104, 0, 88, 29);
		btnStop.setEnabled(stopAction.isEnabled());
		btnStop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				stopModel();
			}
		});

		sctnStartStop.layout(sctnStartStop.getChildren());
		container.layout(container.getChildren());
		
		sctnStartStop.getDisplay().asyncExec(new Runnable() {
			public void run() {
				sctnStartStop.setExpanded(true);
				sctnRunningActor.setExpanded(true);
			}
		});
		
	}
	
	public void dispose() {
		formToolkit.dispose();
		runAction.removeExecuteActionListener(this);
		runAction  = null;
		stopAction = null;
	}

	@Override
	public void setFocus() {
		frmRunWorkflow.setFocus();
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		// Do the Save operation
	}

	@Override
	public void doSaveAs() {
		// Do the Save As operation
	}
	
	private void runModel() {
		btnStart.setEnabled(false);
		btnStop.setEnabled(true);
		runAction.run();
	}
	private void stopModel() {
		stopAction.run();
		btnStart.setEnabled(true);
		btnStop.setEnabled(false);
	}


	public void buttonRefreshRequested(final ExecuteActionEvent evt) {
		btnStart.setEnabled(runAction.isEnabled());
		btnStop.setEnabled(stopAction.isEnabled());
	}

	public void setActorSelected(final String actorName, final boolean isSelected, final int colorCode) {
		
		if (actorName==null) return;
		if (actorText==null||actorText.isDisposed()) return;

		actorText.append("'");
		actorText.append(actorName);
		actorText.append("'");
		if (isSelected) {
			actorText.append(", Running");
		} else {
			actorText.append(", Completed");
		}
		actorText.append("\n");

	}


	public void executionRequested(ExecuteActionEvent evt) {
		actorText.setText("");
	}


	public void stopRequested(ExecuteActionEvent evt) {
		actorText.append("Workflow stopped");
	}

}
