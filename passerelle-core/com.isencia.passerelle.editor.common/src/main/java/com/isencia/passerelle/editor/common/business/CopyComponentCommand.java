/* Copyright 2014 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.editor.common.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.utils.EditorUtils;

public class CopyComponentCommand implements ICommand {

  public static final String DEFAULT_OUTPUT_PORT = "OutputPort";

  public static final String DEFAULT_INPUT_PORT = "InputPort";

  private static Logger logger = LoggerFactory.getLogger(CopyComponentCommand.class);

  private CompositeEntity parent;
  private NamedObj child;
  private NamedObj newChild;

  /**
   * @return the newChild
   */
  public NamedObj getNewChild() {
    return newChild;
  }

  public NamedObj getChild() {
    return child;
  }

  private double[] location;

  public CopyComponentCommand(CompositeEntity parent, NamedObj child,double[] location) {
    this.parent = parent;
    this.child = child;
    this.location = location;
  }

  public Logger getLogger() {
    return logger;
  }

  public boolean canExecute() {
    return (child != null && parent != null);
  }

  public void execute() {
    doExecute();
  }

  public void doExecute() {
    try {
      newChild = (NamedObj) child.clone(parent.workspace());
      boolean input = false;
      if (child instanceof TypedIOPort){
        input = ((TypedIOPort)child).isInput();
      }
      String name = EditorUtils.findUniqueName(parent, child.getClass(), input ? EditorUtils.DEFAULT_INPUT_PORT : EditorUtils.DEFAULT_OUTPUT_PORT, child.getName());

      newChild.setName(name);
      newChild.setDisplayName(name);

      EditorUtils.setLocation(newChild, location);
      EditorUtils.setContainer(newChild, parent);
    } catch (Exception e) {

    }
  }

  public void redo() {
    if (newChild instanceof NamedObj) {
      EditorUtils.setContainer(newChild, parent);
    }
  }

  public void undo() {

    if (newChild instanceof NamedObj) {
      EditorUtils.setContainer(newChild, null);
    }

  }

  public double[] getLocation() {
    return location;
  }

  public void setLocation(double[] location) {
    this.location = location;
  }

}
