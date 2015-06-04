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

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;

public class WizardWorkflowEditor extends PasserelleModelMultiPageEditor implements IReusableEditor {

  private static Logger logger = LoggerFactory.getLogger(WizardWorkflowEditor.class);

  public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.editors.wizardEditor";

  private WizardModelEditor wizEd;

  private boolean subPagesActive = false;


  protected void createPages() {

    subPagesActive = false;
    try {
      createWizardPage(0);
      createWorkflowPage(getWorkflowPageIndex());
      createXmlPage(2);
      createDocPage(3);

    } catch (Exception e) {
      logger.error("Cannot open passerelle editor " + getEditorInput().getName(), e);
    }

    getSite().getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        try {
          final int page = Activator.getDefault().getPreferenceStore().getInt(getPageKey());
          if (page > -1 && page < getPageCount())
            setActivePage(page);

 //         EclipseUtils.getActivePage().showView(EventLogView.ID);
          EclipseUtils.getActivePage().showView(ActorAttributesView.ID);
          EclipseUtils.getActivePage().activate(WizardWorkflowEditor.this);
        } catch (Throwable ignored) {
          // Nowt
        }
      }
    });

  }

  protected int getWorkflowPageIndex() {
    return 1;
  }

  protected String getPageKey() {
    return ID + ".selectedPage." + getEditorInput().getName();
  }

  protected void initializePageSwitching() {
    super.initializePageSwitching();
    this.subPagesActive = true;
  }

  private void createWizardPage(final int pageIndex) throws PartInitException {

    this.wizEd = new WizardModelEditor();

    addPage(wizEd, getEditorInput());
    setPageText(pageIndex, "Run");
    // setPageImage(pageIndex, Activator.getImageDescriptor("icons/run_workflow.gif").createImage());
    pages.add(wizEd);
  }

  protected void createDocPage(int pageIndex) throws Exception {
    // Does not work anymore
  }

  private IEditorInput createWikiInput(IFile wiki) {
    return new FileEditorInput(wiki);
  }

  @Override
  public void setActorSelected(final String actorName, final boolean isSelected, final int colorCode) {

    wizEd.setActorSelected(actorName, isSelected, colorCode);
    super.setActorSelected(actorName, isSelected, colorCode);
  }

  public void pageChange(final int ipage) {

    super.pageChange(ipage);

    if (!subPagesActive)
      return;
    if (EclipseUtils.getActivePage() == null)
      return;
    if (!EclipseUtils.getActivePage().isEditorAreaVisible())
      return;
    if (ipage == 0) {
      try {
        EclipseUtils.getActivePage().showView("org.eclipse.ui.navigator.ProjectExplorer");
      } catch (Throwable e) {
        logger.error("Cannot select actor tree view!");
      }
    }

    Activator.getDefault().getPreferenceStore().setValue(getPageKey(), ipage);
  }

  @Override
  public void setPasserelleEditorActive() {
    setActivePage(getWorkflowPageIndex());
  }

  public void setInput(IEditorInput input) {
      super.setInput(input);
  }
}
