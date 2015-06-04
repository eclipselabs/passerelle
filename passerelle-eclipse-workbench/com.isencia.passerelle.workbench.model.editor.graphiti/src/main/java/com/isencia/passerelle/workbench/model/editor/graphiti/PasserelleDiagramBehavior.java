package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.core.resources.IFile;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.editor.DefaultPersistencyBehavior;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.editor.IDiagramContainerUI;
import org.eclipse.graphiti.ui.editor.IDiagramEditorInput;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.DiagramFlowRepository;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.FlowChangeListener;
import com.isencia.passerelle.workbench.model.editor.graphiti.model.PasserellePersistencyBehavior;

public class PasserelleDiagramBehavior extends DiagramBehavior {
  private Flow flow;
  private FlowChangeListener flowChangeListener;

  public PasserelleDiagramBehavior(IDiagramContainerUI diagramContainer) {
    super(diagramContainer);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected DefaultPersistencyBehavior createPersistencyBehavior() {
    return new PasserellePersistencyBehavior(this);
  }

  @Override
  protected void registerBusinessObjectsListener() {
    flowChangeListener = new FlowChangeListener(getDiagramTypeProvider());
    Flow f = getFlow();
    if (f != null) {
      f.addChangeListener(flowChangeListener);
    }
  }

  @Override
  protected void unregisterBusinessObjectsListener() {
    Flow f = getFlow();
    if (f != null) {
      f.removeChangeListener(flowChangeListener);
    }
  }

  /**
   * @return the flow that is open in this editor instance; Can be null if no Flow is opened yet somehow.
   */
  public Flow getFlow() {
    if (flow == null) {
      try {
        Diagram diagram = getDiagramTypeProvider().getDiagram();
        flow = DiagramFlowRepository.getFlowForDiagram(diagram);
      } catch (NullPointerException e) {
        // ignore, means somehow the flow is not linked yet
      }
    }
    return flow;
  }
  
  public IFile getDiagramFile() {
    IDiagramEditorInput input = getInput();
    if(input instanceof DiagramEditorInput) {
      return (IFile) ((DiagramEditorInput)input).getAdapter(IFile.class);
    } else {
      return null;
    }
  }

}
