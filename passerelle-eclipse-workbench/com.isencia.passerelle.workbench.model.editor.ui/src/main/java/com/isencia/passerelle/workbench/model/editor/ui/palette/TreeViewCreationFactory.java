package com.isencia.passerelle.workbench.model.editor.ui.palette;

import org.eclipse.gef.requests.CreationFactory;

import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.editor.common.model.SubModelPaletteItemDefinition;

public class TreeViewCreationFactory implements CreationFactory {
  PaletteItemDefinition selected;

  public TreeViewCreationFactory(PaletteItemDefinition selected) {
    super();
    this.selected = selected;
  }

  public Object getObjectType() {
    // TODO Auto-generated method stub
    PaletteItemDefinition selected2 = (PaletteItemDefinition) selected;

    return selected2.getClazz();
  }

  public Object getNewObject() {
    // TODO Auto-generated method stub
    PaletteItemDefinition selected2 = (PaletteItemDefinition) selected;
    if (selected2 instanceof SubModelPaletteItemDefinition) {
      return (SubModelPaletteItemDefinition) selected2;
    }
    return selected2.getName();
  }

}
