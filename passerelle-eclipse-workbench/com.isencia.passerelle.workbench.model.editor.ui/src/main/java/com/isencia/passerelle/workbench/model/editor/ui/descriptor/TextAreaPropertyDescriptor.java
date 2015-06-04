/**
 * 
 */
package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * A basic implementation of a multi-line text-area,
 * to be able to edit StringParameters that have set a TextStyle.
 * (wich is ptolemy's way for specifying a need for a text area for
 * multi-line strings)
 * 
 * @author erwin
 *
 */
public class TextAreaPropertyDescriptor extends TextPropertyDescriptor {
  private int minHeight;

  /**
   * @param id
   * @param displayName
   * @param minHeight 
   */
  public TextAreaPropertyDescriptor(Object id, String displayName, int minHeight) {
    super(id, displayName);
    this.minHeight = minHeight;
  }
  
  public CellEditor createPropertyEditor(Composite parent) {
    CellEditor editor = new TextAreaCellEditor(parent);
    if (getValidator() != null) {
      editor.setValidator(getValidator());
    }
    return editor;
  }

  /**
   * A cell editor that manages a text entry field.
   * The cell editor's value is the text string itself.
   * <p>
   * This class may be instantiated or subclassed.
   * </p>
   */
  private class TextAreaCellEditor extends org.eclipse.jface.viewers.TextCellEditor {

    public TextAreaCellEditor(Composite parent) {
      super(parent, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER_SOLID);
    }
    @Override
    public LayoutData getLayoutData() {
      LayoutData layoutData = super.getLayoutData();
      layoutData.minimumHeight = minHeight;
      return layoutData;
    }
  }
}
