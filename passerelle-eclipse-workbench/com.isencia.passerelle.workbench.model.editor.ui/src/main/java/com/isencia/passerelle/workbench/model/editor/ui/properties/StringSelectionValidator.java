package com.isencia.passerelle.workbench.model.editor.ui.properties;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class StringSelectionValidator implements ICellEditorValidator {

  private List<String> choices;

  public StringSelectionValidator(String[] choices) {
    this.choices = Arrays.asList(choices);
  }

  public String isValid(Object value) {
    if (value == null)
      return null;
    if (!choices.contains(value.toString())) {
      return "Please select one from " + choices;
    }
    return null;
  }

}
