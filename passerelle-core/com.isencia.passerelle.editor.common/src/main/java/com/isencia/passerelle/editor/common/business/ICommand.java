package com.isencia.passerelle.editor.common.business;

public interface ICommand {

  void doExecute();
  void redo();
  void undo();

}
