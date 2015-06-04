package com.isencia.passerelle.workbench.model.editor.ui.editpart;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.Actor;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Changeable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.editor.common.model.LinkHolder;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.INameable;
import com.isencia.passerelle.workbench.model.editor.ui.ImageRegistry;
import com.isencia.passerelle.workbench.model.editor.ui.PreferenceConstants;
import com.isencia.passerelle.workbench.model.editor.ui.figure.AbstractNodeFigure;
import com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints;
import com.isencia.passerelle.workbench.model.ui.command.ChangeActorPropertyCommand;
import com.isencia.passerelle.workbench.model.ui.command.CreateComponentCommand;
import com.isencia.passerelle.workbench.model.ui.command.IRefreshConnections;
import com.isencia.passerelle.workbench.model.ui.command.RenameCommand;
import com.isencia.passerelle.workbench.model.ui.command.SetConstraintCommand;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * Base Edit Part
 */
abstract public class AbstractBaseEditPart extends org.eclipse.gef.editparts.AbstractGraphicalEditPart implements ChangeListener {

  protected DiagramEditPart getDiagram() {
    return ((DiagramEditPart) getParent());
  }

  // protected Set<Image> images = new HashSet<Image>();
  //
  // public Set<Image> getImages() {
  // return images;
  // }

  protected Image createImage(ImageDescriptor imageDescriptor) {
    Image image = ImageRegistry.getInstance().getImage(imageDescriptor);
    ;
    // images.add(image);
    return image;
  }

  public AbstractBaseEditPart() {
    super();
  }

  private static final Logger logger = LoggerFactory.getLogger(AbstractBaseEditPart.class);

  protected IPropertySource propertySource = null;

  private IPropertyChangeListener expertUpdater;

  public void activate() {

    if (isActive())
      return;
    super.activate();
    if (getEntity() instanceof Changeable) {
      Changeable changeable = (Changeable) getEntity();
      changeable.addChangeListener(this);
    }

    if (expertUpdater == null)
      expertUpdater = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
          if (event.getProperty().equals(PreferenceConstants.EXPERT)) {
            final PropertySheet sheet = (PropertySheet) EclipseUtils.getPage().findView(IPageLayout.ID_PROP_SHEET);
            if (sheet != null) {
              TabbedPropertySheetPage page = (TabbedPropertySheetPage) sheet.getCurrentPage();
              if (page != null)
                page.refresh();
            }
          }
        }
      };
    Activator.getDefault().getPreferenceStore().addPropertyChangeListener(expertUpdater);

  }

  /**
   * Makes the EditPart insensible to changes in the model by removing itself from the model's list of listeners.
   */
  public void deactivate() {

    Activator.getDefault().getPreferenceStore().removePropertyChangeListener(expertUpdater);

    if (!isActive())
      return;
    if (getEntity() instanceof Changeable) {
      Changeable changeable = (Changeable) getEntity();
      changeable.removeChangeListener(this);
    }
    super.deactivate();
  }

  /**
   * Returns the model associated with this as a NamedObj.
   * 
   * @return The model of this as an NamedObj.
   */
  public NamedObj getEntity() {
    return (NamedObj) getModel();
  }

  /**
   * Returns the Figure of this, as a node type figure.
   * 
   * @return Figure as a NodeFigure.
   */
  protected Figure getComponentFigure() {
    return (Figure) getFigure();
  }

  /**
   * Updates the visual aspect of this.
   */
  public void refreshVisuals() {
    double[] location = ModelUtils.getLocation(getEntity());
    Rectangle r = new Rectangle(new Point(location[0], location[1]), getComponentFigure().getPreferredSize(-1, -1));
    if (getParent() instanceof GraphicalEditPart)
      ((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), r);
  }

  public Object getAdapter(Class key) {
    if (IPropertySource.class == key) {
      return getPropertySource();
    }
    return super.getAdapter(key);
  }

  protected IPropertySource getPropertySource() {

    return null;
  }

  public void refreshConnections() {
    try {
      refreshSourceConnections();
      refreshTargetConnections();
    } catch (Exception e) {

    }
  }

  protected void onChangePropertyResource(Object source) {
    if (source == this.getModel()) {
      // Execute the dummy command force a dirty state
      getViewer().getEditDomain().getCommandStack().execute(new ChangeActorPropertyCommand());
    }
  }

  public String getText(Object source) {
    return ((NamedObj) source).getDisplayName();
  }

  public void changeFailed(ChangeRequest changerequest, Exception exception) {
    getLogger().trace("Change Failed : " + exception.getMessage());
  }

  public Logger getLogger() {
    return logger;
  }

  public void changeExecuted(ChangeRequest changerequest) {

    Object source = changerequest.getSource();
    if (changerequest instanceof ModelChangeRequest) {

      Class<?> type = ((ModelChangeRequest) changerequest).getType();
      if (CreateComponentCommand.class.equals(type)) {
        return;
      }
      if (SetConstraintCommand.class.equals(type)) {
        if (source == getModel() && source instanceof NamedObj) {
          refresh();
        } else {
          DiagramEditPart diagram = getDiagram();
          if (diagram != null && diagram.getLinkHolder() != null) {
            Set<Link> links = diagram.getLinkHolder().getLinks(source);
            for (Link link : links) {
              if (link.getTail().equals(getModel()) || link.getHead().equals(getModel())) {
                refresh();
              }
            }
          }
        }
        return;
      }
      if (RenameCommand.class.equals(type)) {
        if (source == getModel() && source instanceof NamedObj) {
          String name = getText(source);
          if ((getFigure() instanceof INameable) && name != null && !name.equals(((INameable) getFigure()).getName())) {
            ((INameable) getFigure()).setName(name);
            getFigure().repaint();
          }
        }
        return;
      }
//      if (changerequest.getDescription().contains("undo")){
//        refresh();
//      }
      if (IRefreshConnections.class.isAssignableFrom(type) && (this instanceof IActorNodeEditPart)) {
        try {
          refreshConnections();
        } catch (Exception e) {
        }
        return;
      }

    }
  }

  public static List getModelConnections(LinkHolder diagram, Actor no, boolean target) {

    List<Link> allLinks = new ArrayList<Link>();
    List ports = null;
    if (target)
      ports = no.inputPortList();
    else
      ports = no.outputPortList();
    for (Object port : ports) {
      Set<Link> links = diagram.getLinks(port);
      if (links != null) {
        for (Link link : links) {
          NamedObj container = no.getContainer();
          NamedObj relContainer = link.getRelation().getContainer();
          if (container != null && container.equals(relContainer)) {
            allLinks.add(link);
          }
        }

      }
    }
    return allLinks;
  }

  public static ConnectionAnchor getConnectionAnchor(ConnectionEditPart connEditPart, AbstractNodeFigure actorFigure, Actor no, boolean target) {
    Port port = null;
    Object model = connEditPart.getModel();
    if (model instanceof LinkWithBendPoints) {
      Link link = (Link) model;
      Object source = null;
      if (target)
        source = link.getTail();
      else
        source = link.getHead();
      if (source instanceof Port) {
        port = (Port) source;
        NamedObj container = port.getContainer();
        if (container != null && container.equals(no)) {
          return actorFigure.getConnectionAnchor(port.getName());
        }
      }
      if (source instanceof Vertex) {
        return actorFigure.getConnectionAnchor(((Vertex) source).getName());

      }
    }
    return null;
  }

}
