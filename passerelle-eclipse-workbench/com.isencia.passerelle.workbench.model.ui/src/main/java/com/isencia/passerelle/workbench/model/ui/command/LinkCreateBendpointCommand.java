package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

import com.isencia.passerelle.workbench.model.opm.OPMLink;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

/**
 * Command used to create a new bendpoint in a {@linkplain OPMLink}. This class is declared final since it has a very
 * specific functionality.
 * 
 * @author vainolo
 * 
 */
public final class LinkCreateBendpointCommand extends Command {

  /** Index on which the new bendpoint is added. */
  private int index;
  /** Location of new bendpoint. */
  private Point location;
  /** Link to which the bendpoint is added. */
  private OPMLink link;

  public OPMLink getLink() {
    return link;
  }

  private CompositeEntity parent;

  private CreateComponentCommand createVertexCommand;
  
  private ReConnectLinkCommand reconnectCommand;
  
  private CreateConnectionCommand createConnectionCommand;
  
  private IPasserelleMultiPageEditor editor;
  
  public LinkCreateBendpointCommand(IPasserelleMultiPageEditor editor) {
    super();
    this.editor = editor;
    this.parent = editor.getSelectedContainer();
  }

  @Override
  public void execute() {
    TypedIORelation relation =(TypedIORelation)link.getRelation();
    if (relation.attributeList(Vertex.class).size() == 0){
      createVertexCommand = new CreateComponentCommand(Vertex.class,"vertex",parent,new double[]{location.x,location.y},relation); 
    }else{
      createVertexCommand = new CreateComponentCommand(Vertex.class,"vertex",parent,new double[]{location.x,location.y},null);
    }
    createVertexCommand.execute();
    reconnectCommand = new ReConnectLinkCommand(editor,link);
    reconnectCommand.setNewTarget(createVertexCommand.getChild());
    reconnectCommand.execute();
    
    createConnectionCommand =  new CreateConnectionCommand(createVertexCommand.getChild(),(NamedObj)link.getTail(),editor);
    createConnectionCommand.execute();
  //  link.getBendpoints().add(index,location);

  }

  @Override
  public void undo() {
   // link.getBendpoints().remove(index);
  }

  /**
   * Set the index on which the bendpoint is added.
   * 
   * @param index
   *          Index on which the bendpoint should be added.
   */
  public void setIndex(final int index) {
    this.index = index;
    // TODO:validation checks.
  }

  /**
   * Set the location where the new bendpoint is added.
   * 
   * @param location
   *          point in the diagram where the new bendpoint is added.
   */
  public void setLocation(final Point location) {
    this.location = location;
  }

  /**
   * Set the link on which the new bendpoint is added.
   * 
   * @param link
   *          link on which the bendpoint is added.
   */
  public void setOPMLink(final OPMLink link) {
    this.link = link;
  }
}
