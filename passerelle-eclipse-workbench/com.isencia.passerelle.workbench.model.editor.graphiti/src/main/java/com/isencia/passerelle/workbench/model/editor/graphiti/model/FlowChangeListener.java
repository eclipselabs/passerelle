package com.isencia.passerelle.workbench.model.editor.graphiti.model;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;

public class FlowChangeListener implements ChangeListener, ValueListener {
  
  private final static Logger LOGGER = LoggerFactory.getLogger(FlowChangeListener.class);
  
  private IDiagramTypeProvider dtp;

  /**
   * 
   * @param editor
   */
  public FlowChangeListener(IDiagramTypeProvider editor) {
    this.dtp = editor;
  }

  /**
   * Tries to trigger an update of the diagram elements that are linked
   * to the given settable (typically an actor parameter).
   */
  @Override
  public void valueChanged(Settable settable) {
    LOGGER.trace("valueChanged entry for {}",settable);
    
    final PictogramElement[] dirtyPes = getDiagramTypeProvider().getNotificationService().calculateRelatedPictogramElements(new Object[]{settable});
    
    // Do an asynchronous update in the UI thread.
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        if (getDiagramTypeProvider().isAutoUpdateAtRuntime()) {
          // Bug 352109: Enable unconditional auto refresh for case 3)
          // standard refresh with saved editor
          if (getDiagramTypeProvider().isAutoUpdateAtRuntimeWhenEditorIsSaved()
              || getDiagramTypeProvider().getDiagramEditor().isDirty()) {
            // The notification service takes care of not only the
            // linked BOs but also asks the diagram provider about
            // related BOs.
            getDiagramTypeProvider().getNotificationService().updatePictogramElements(dirtyPes);
          }
        } else {
          getDiagramTypeProvider().getDiagramBehavior().refresh();
        }
      }

    });
  }

  @Override
  public void changeExecuted(ChangeRequest change) {
    LOGGER.trace("changeExecuted entry for {}",change);
    ;
    // Do an asynchronous update in the UI thread.
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        if (getDiagramTypeProvider().isAutoUpdateAtRuntime()) {
          // Bug 352109: Enable unconditional auto refresh for case 3)
          // standard refresh with saved editor
          if (getDiagramTypeProvider().isAutoUpdateAtRuntimeWhenEditorIsSaved()
              || getDiagramTypeProvider().getDiagramEditor().isDirty()) {
            // The notification service takes care of not only the
            // linked BOs but also asks the diagram provider about
            // related BOs.
            getDiagramTypeProvider().getNotificationService().updatePictogramElements(new PictogramElement[] {getDiagramTypeProvider().getDiagram()});
          }
        } else {
          getDiagramTypeProvider().getDiagramBehavior().refresh();
        }
      }

    });
  }

  @Override
  public void changeFailed(ChangeRequest change, Exception exception) {
    // TODO Auto-generated method stub
    
  }
  
  protected IDiagramTypeProvider getDiagramTypeProvider() {
    return dtp;
  }

}
