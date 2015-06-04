package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.editor.common.model.SubModelPaletteItemDefinition;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class DeleteSubmodelAction extends Action {

  private static final Logger logger = LoggerFactory.getLogger(DeleteSubmodelAction.class);

  private final String icon = "icons/delete.gif";
  private Object definition;

  public DeleteSubmodelAction(Object actionOrGroup) {

    super();
    setId(getClass().getName());
    setText("Delete submodel");
    this.definition = actionOrGroup;
    Activator.getImageDescriptor(icon);
    setHoverImageDescriptor(Activator.getImageDescriptor(icon));
    setImageDescriptor(Activator.getImageDescriptor(icon));
    setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
    setEnabled(checkEnabled());
  }

  protected boolean checkEnabled() {
    if (!(definition instanceof SubModelPaletteItemDefinition))
      return false;
    return true;
  }

  @Override
  public void run() {
    if (!(definition instanceof SubModelPaletteItemDefinition))
      return;
    if (definition != null) {
      try {
        final SubModelPaletteItemDefinition item = (SubModelPaletteItemDefinition) definition;
        final String name = item.getName();
        PaletteBuilder instance = PaletteBuilder.getInstance();
        PaletteItemDefinition paletteItem = instance.getSubModelGroup().getPaletteItem(name);
        instance.getSubModelGroup().removePaletteItem(paletteItem );
        Activator.getDefault().getRepositoryService().deleteSubmodel(name);

        SubModelViewUtils.refreshPallette();
      } catch (Exception e) {
        logger.error("Cannot edit submodel!", e);
      }
    }
  }

}
