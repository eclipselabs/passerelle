package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.workbench.model.opm.OPMLink;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

/**
 * Move a link bendpoint. This class is declared final since it has a very specific functionality.
 * 
 * @author vainolo
 */
public final class LinkMoveBendpointCommand extends Command implements IRefreshBendpoints {

  /** Old location of the moved bendpoint. */
  private Point oldLocation;
  /** New location of the moved bendpoint. */
  private Point newLocation;
  /** Index of the bendpoint in the link's bendpoint list. */
  private int index;
  /** Link that contains the bendpoint. */
  private OPMLink link;

  private CompositeEntity parent;

  public LinkMoveBendpointCommand(CompositeEntity parent) {
    super();
    this.parent = parent;
  }

  /** Move the bendpoint to the new location. */
  public void execute() {
    if (oldLocation == null) {
      oldLocation = link.getBendpoints().get(index);
    }

    parent.requestChange(new ModelChangeRequest(this.getClass(), link, "setLocation") {
      @Override
      protected void _execute() throws Exception {

        try {
          link.getBendpoints().set(index, newLocation);

        } catch (Exception e) {
        }
      }
    });
  }

  /** Restore the old location of the bendpoint. */
  @Override
  public void undo() {
    link.getBendpoints().set(index, oldLocation);
  }

  /**
   * Set the index where the bendpoint is located in the bendpoint list.
   * 
   * @param index
   *          the index where the bendpoint is located.
   */
  public void setIndex(final int index) {
    this.index = index;
  }

  /**
   * Set the link where the bendpoint is located.
   * 
   * @param link
   *          the link where the bendpoint is located.
   */
  public void setOPMLink(final OPMLink link) {
    this.link = link;
  }

  /**
   * Set the new location of the bendpoint.
   * 
   * @param newLocation
   *          the new location of the bendpoint.
   */
  public void setLocation(final Point newLocation) {
    this.newLocation = newLocation;
  }
  

  public OPMLink getLink() {
    return link;
  }
}
