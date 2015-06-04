package com.isencia.passerelle.workbench.model.editor.ui.views.execTrace;

public class TraceEntry {
  String time;
  String source;
  String msg;
  public TraceEntry(String time, String source, String msg) {
    this.time = time;
    this.source = source;
    this.msg = msg;
  }
}