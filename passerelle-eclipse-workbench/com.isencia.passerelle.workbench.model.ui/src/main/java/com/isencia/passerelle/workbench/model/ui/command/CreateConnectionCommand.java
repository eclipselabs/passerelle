package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.editor.common.business.ICommand;
import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CreateConnectionCommand extends Command implements IRefreshConnections, ICommand {

  private IPasserelleMultiPageEditor editor;
  private Link link;

  public Link getLink() {
    return link;
  }

  public CreateConnectionCommand(NamedObj source, NamedObj target, IPasserelleMultiPageEditor editor) {
    super();
    this.sourceNamedObj = source;
    this.targetNamedObj = target;
    this.editor = editor;
    this.container = editor.getSelectedContainer();
  }

  public CreateConnectionCommand(IPasserelleMultiPageEditor editor) {
    super();
    this.editor = editor;

  }

  private final static Logger logger = LoggerFactory.getLogger(CreateConnectionCommand.class);

  public Logger getLogger() {
    return logger;
  }

  protected CompositeEntity container;

  public void setContainer(CompositeEntity container) {
    this.container = container;
  }

  protected TypedIORelation connection;

  protected NamedObj sourceNamedObj;
  protected NamedObj targetNamedObj;

  public boolean canExecute() {
    return (sourceNamedObj != null && targetNamedObj != null);
  }

  public void execute() {
    doExecute();
  }

  public void doExecute() {
    if (sourceNamedObj != null && targetNamedObj != null) {
      CompositeEntity temp = getContainer(sourceNamedObj, targetNamedObj);
      if (temp != null) {
        container = temp;
      }
      if (container == null) {
        return;
      }
      // Perform Change in a ChangeRequest so that all Listeners are
      // notified
      ModelChangeRequest modelChangeRequest = new ModelChangeRequest(this.getClass(), container, "connection") {
        /**
         * @return the link
         */

        @Override
        protected void _execute() throws Exception {
          try {
            if (sourceNamedObj == null || targetNamedObj == null) {
              return;
            }
            ComponentRelation relation = null;
            if ((targetNamedObj instanceof ComponentPort) && (sourceNamedObj instanceof ComponentPort)) {
              relation = container.connect((ComponentPort) sourceNamedObj, (ComponentPort) targetNamedObj);
            } else if (sourceNamedObj instanceof Vertex && targetNamedObj instanceof Vertex) {
              relation = (TypedIORelation) ((Vertex) sourceNamedObj).getContainer();
              relation.link((TypedIORelation) ((Vertex) targetNamedObj).getContainer());

              ((Relation) ((Vertex) targetNamedObj).getContainer()).link((Relation) ((Vertex) sourceNamedObj).getContainer());
            } else {
              if (targetNamedObj instanceof Vertex) {
                relation = (ComponentRelation) ((Vertex) targetNamedObj).getContainer();
                ((ComponentPort) sourceNamedObj).link(relation);
              } else {
                relation = (ComponentRelation) ((Vertex) sourceNamedObj).getContainer();
                ((ComponentPort) targetNamedObj).link(relation);
              }
            }
            Link generateLink = null;
//            if (sourceNamedObj instanceof Vertex && targetNamedObj instanceof Vertex) {
//              double[] otherVertexLocation = ModelUtils.getLocation(sourceNamedObj);
//              double[] vertexLocation = ModelUtils.getLocation(targetNamedObj);
//              if (vertexLocation[0] < otherVertexLocation[0]) {
//                generateLink = editor.generateLink(relation, sourceNamedObj, targetNamedObj);
//              } else {
//                generateLink = editor.generateLink(relation, targetNamedObj, sourceNamedObj);
//              }
//
//            } else {
              generateLink = editor.generateLink(relation, sourceNamedObj, targetNamedObj);
//            }
            setLink(generateLink);
          } catch (Exception e) {
            if (link != null) {
              editor.registerLink(link);
            } else {
              logger.error("Unable to create connection", e);
              EclipseUtils.logError(e, "Unable to create connection", IStatus.ERROR);
            }
          }

        }
      };
      container.requestChange(modelChangeRequest);
      this.link = modelChangeRequest.getLink();
    }
  }

  private CompositeEntity getContainer(NamedObj source, NamedObj target) {
    if (container != null)
      return container;
    // if (editor != null && (source instanceof TypedIOPort || target instanceof TypedIOPort)) {
    // return editor.getSelectedPage().getContainer();
    // }

    return editor.getSelectedPage().getContainer();
  }

  public String getLabel() {
    return "";
  }

  public NamedObj getSource() {
    return sourceNamedObj;
  }

  public NamedObj getTarget() {
    return targetNamedObj;
  }

  public void redo() {
    doExecute();
  }

  public void setSource(NamedObj newSource) {
    sourceNamedObj = newSource;
  }

  public void setTarget(NamedObj newTarget) {
    targetNamedObj = newTarget;
  }

  public void undo() {
    if (link != null) {
      try {
        new DeleteLinkCommand(container, link, editor).doExecute();
      } catch (Exception e) {
      }
    }
  }

}
