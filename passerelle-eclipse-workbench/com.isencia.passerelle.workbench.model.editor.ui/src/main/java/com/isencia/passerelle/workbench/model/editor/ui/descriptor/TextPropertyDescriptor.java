/**
 * 
 */
package com.isencia.passerelle.workbench.model.editor.ui.descriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * A single-line TextPD that enforces a height of 0.
 * 
 * With plain TextPDs, combined with TextAreaPDs, the height applied for a text area
 * can not be reset by a next cell using a TextPD.
 * This is resolved in here.
 * 
 * @author erwin
 *
 */
public class TextPropertyDescriptor extends org.eclipse.ui.views.properties.TextPropertyDescriptor {

  /**
   * @param id
   * @param displayName
   */
  public TextPropertyDescriptor(Object id, String displayName) {
    super(id, displayName);
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
      super(parent);
    }
    @Override
    public LayoutData getLayoutData() {
      LayoutData layoutData = super.getLayoutData();
      layoutData.minimumHeight = 0;
      return layoutData;
    }
  }
}
