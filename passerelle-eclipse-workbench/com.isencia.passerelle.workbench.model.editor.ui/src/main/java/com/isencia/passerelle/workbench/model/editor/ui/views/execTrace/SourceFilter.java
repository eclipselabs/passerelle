package com.isencia.passerelle.workbench.model.editor.ui.views.execTrace;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class SourceFilter extends ViewerFilter {

  private String searchString;

  public void setSearchText(String s) {
    // Search must be a substring of the existing value
    if (s == null || s.length() == 0) {
      this.searchString = null;
    } else {
      this.searchString = ".*" + s + ".*";
    }
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    if (searchString == null || searchString.length() == 0) {
      return true;
    }
    TraceEntry p = (TraceEntry) element;
    if (p.source.matches(searchString)) {
      return true;
    }
    return false;
  }
}
