package com.isencia.passerelle.workbench.model.editor.ui.editor;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerEditPart;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.editor.common.model.SubModelPaletteItemDefinition;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class PaletteMouseListener implements MouseMotionListener {

  private static Logger logger = LoggerFactory.getLogger(PaletteMouseListener.class);

  private DrawerEditPart drawerFigure;
  private PaletteViewer paletteViewer;

  public PaletteMouseListener(DrawerEditPart drawerFigure, PaletteViewer paletteViewer) {
    super();
    this.drawerFigure = drawerFigure;
    this.paletteViewer = paletteViewer;
  }

  public void mouseDragged(MouseEvent me) {

  }

  public void mouseEntered(MouseEvent me) {
    try {
      addFavorite();
    } catch (Exception e) {
      logger.error("Cannot add favourite!", e);
    }

  }

  private void addFavorite() throws Exception {

    PaletteBuilder builder = PaletteBuilder.getInstance();
    CreationFactory config = builder.getSelectedItem();
    if (config != null) {
      Class type = (Class) config.getObjectType();
      drawerFigure.getDrawer().getLabel();
      if (type.equals(Flow.class)) {
        SubModelPaletteItemDefinition item = (SubModelPaletteItemDefinition) config.getNewObject();
        builder.addFavorite(item.getName(), (PaletteContainer) builder.getFavoriteGroup(drawerFigure.getDrawer().getLabel()));

      } else {
        builder.addFavorite(type.getName(), (PaletteContainer) builder.getFavoriteGroup(drawerFigure.getDrawer().getLabel()));
      }
      builder.synchFavorites(paletteViewer);
      builder.setSelectedItem(null);
    }
  }

  public void mouseExited(MouseEvent me) {

  }

  public void mouseHover(MouseEvent me) {

  }

  public void mouseMoved(MouseEvent me) {
  }

}
