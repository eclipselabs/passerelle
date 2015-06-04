package com.isencia.passerelle.workbench.model.editor.ui.palette;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * Interface to allow varying drag support, especially the CreationFactories used, 
 * matching the active model editor and its drop requirements.
 *  
 * @author erwin
 *
 */
public interface DragSupportBuilder {

  /**
   * Adds the correctly configured drag support, consisting of the supported operations, a drag target listener,
   * and the associated CreationFactory.
   * @param treeViewer
   */
  void addDragSupport(TreeViewer treeViewer);
  
}
