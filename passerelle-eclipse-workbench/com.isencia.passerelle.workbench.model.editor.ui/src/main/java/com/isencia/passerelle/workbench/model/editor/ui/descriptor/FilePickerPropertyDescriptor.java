package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.workbench.model.editor.ui.cell.DirectoryBrowserEditor;
import com.isencia.passerelle.workbench.model.editor.ui.cell.FileBrowserEditor;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * 
 * This class provides a bridge between a FileParameter and its custom settings, and the available SWT UI components to
 * configure a value for that FileParameter.
 * <p>
 * <b>IMPORTANT LIMITATION:</b> Remark that in SWT, contrary to Swing, we could not find support for the option
 * JFileChooser.FILES_AND_DIRECTORIES. I.e. the user will be able to either select a file or a directory, as SWT offers
 * separate FileDialog and DirectoryDialog components.
 * </p>
 * <p>
 * This implies that the Ptolemy options allowFiles and allowDirectories on a FileParameter can not be both true. In the
 * cases when both are false or both are true, the option will be enforced to select a file, not a directory!
 * </p>
 */
public class FilePickerPropertyDescriptor extends PropertyDescriptor {
  private static Logger LOGGER = LoggerFactory.getLogger(FilePickerPropertyDescriptor.class);

  private String[] fileFilter;
  private String currentPath;
  private boolean needsDirectorySelection;

  /**
   * @param fp
   */
  public FilePickerPropertyDescriptor(FileParameter fp) {
    super(fp.getName(), fp.getDisplayName());
    if (fp instanceof com.isencia.passerelle.util.ptolemy.FileParameter) {
      setFilter(((com.isencia.passerelle.util.ptolemy.FileParameter) fp).getFilterExtensions());
    }
    try {
      if (fp.asFile() != null)
        setCurrentPath(fp.asFile().getParent());
    } catch (IllegalActionException e) {
      LOGGER.error("Cannot get FileParameter's current path!", e);
    }
    try {
      setFileOrDirectoryPickerMode(fp);
    } catch (IllegalActionException e) {
      LOGGER.error("Cannot get FileParameter's file or directory mode!", e);
      setNeedsDirectoryMode(false);
    }
  }

  public CellEditor createPropertyEditor(Composite parent) {
    if (needsDirectorySelection) {
      DirectoryBrowserEditor editor = new DirectoryBrowserEditor(parent);
      editor.setCurrentPath(currentPath);
      if (getValidator() != null)
        editor.setValidator(getValidator());
      return editor;
    } else {
      FileBrowserEditor editor = new FileBrowserEditor(parent);
      editor.setFilter(fileFilter);
      editor.setCurrentPath(currentPath);
      if (getValidator() != null)
        editor.setValidator(getValidator());
      return editor;
    }
  }

  public void setFilter(String[] filter) {
    this.fileFilter = filter;
  }

  public void setCurrentPath(String filterPath) {
    this.currentPath = filterPath;
  }

  public void setNeedsDirectoryMode(boolean needsDirectoryMode) {
    this.needsDirectorySelection = needsDirectoryMode;
  }

  private void setFileOrDirectoryPickerMode(FileParameter parameter) throws IllegalActionException {
    // Check to see whether the attribute being configured
    // specifies whether files or directories should be
    // listed.
    // By default, only files are selectable.
    boolean allowFiles = true;
    boolean allowDirectories = false;
    // attribute is always a NamedObj
    Parameter marker = (Parameter) parameter.getAttribute("allowFiles", Parameter.class);
    if (marker != null) {
      Token value = marker.getToken();
      if (value instanceof BooleanToken) {
        allowFiles = ((BooleanToken) value).booleanValue();
      }
    }
    if (allowFiles) {
      setNeedsDirectoryMode(false);
      return;
    } else {
      marker = (Parameter) parameter.getAttribute("allowDirectories", Parameter.class);
      if (marker != null) {
        Token value = marker.getToken();
        if (value instanceof BooleanToken) {
          allowDirectories = ((BooleanToken) value).booleanValue();
        }
      }
    }
    if (allowDirectories) {
      setNeedsDirectoryMode(true);
    }
  }

}
