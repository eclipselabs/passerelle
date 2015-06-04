package com.isencia.passerelle.workbench.model.ui.command;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.viewers.ColumnViewer;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;
import com.isencia.passerelle.workbench.model.utils.ModelChangeRequest;

public class RenameCommand extends Command {
  private ColumnViewer viewer;
  private NamedObj model;
  private String oldName;
  private GeneralAttribute attribute;
  private String newName;

  public RenameCommand(final ColumnViewer viewer, NamedObj model, String newName, GeneralAttribute attribute) throws Exception {
    this(model, newName);
    this.viewer = viewer;
    this.attribute = attribute;
  }

  public RenameCommand(final ColumnViewer viewer, NamedObj model, String newName) throws Exception {
    this(model, newName);
    this.viewer = viewer;
  }

  /**
   * @param model
   * @param newName
   * @throws Exception
   *           if newName is not a valid name
   */
  public RenameCommand(NamedObj model, String newName) throws Exception {
    super();
    this.model = model;
    this.newName = newName;

    if (!newName.matches("[a-zA-Z0-9_ ]+")) {
      throw new Exception("The new name contains non-alphanumeric characters.");
    }
  }

  public void execute() {

    model.requestChange(new ModelChangeRequest(this.getClass(), model, "rename") {
      @Override
      protected void _execute() throws Exception {

        oldName = model.getName();
        try {
          model.setName(newName);
          model.setDisplayName(newName);
        } catch (IllegalActionException e) {

        } catch (NameDuplicationException e) {

        } finally {
          if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.cancelEditing();
            viewer.refresh(attribute);
          }
        }
      }
    });
  }

  public void setModel(NamedObj model) {
    this.model = (NamedObj) model;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public void undo() {
    model.requestChange(new ModelChangeRequest(this.getClass(), model, "rename") {
      @Override
      protected void _execute() throws Exception {

        try {
          model.setName(oldName);
          model.setDisplayName(oldName);
        } catch (IllegalActionException e) {

        } catch (NameDuplicationException e) {

        } finally {
          if (viewer != null && !viewer.getControl().isDisposed()) {
            viewer.cancelEditing();
            viewer.refresh(attribute);
          }
        }
      }
    });
  }
}
