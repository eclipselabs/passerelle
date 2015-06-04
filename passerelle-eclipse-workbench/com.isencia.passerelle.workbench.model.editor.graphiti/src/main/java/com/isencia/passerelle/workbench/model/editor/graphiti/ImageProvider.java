/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.workbench.model.editor.graphiti;

import org.eclipse.graphiti.ui.platform.AbstractImageProvider;

public class ImageProvider extends AbstractImageProvider {

  private static final String ROOT_FOLDER_FOR_IMG = "icons/";

  @Override
  protected void addAvailableImages() {
    // outline
    addImageFilePath(ImageConstants.IMG_OUTLINE_TREE, ROOT_FOLDER_FOR_IMG + "tree.gif");
    addImageFilePath(ImageConstants.IMG_OUTLINE_THUMBNAIL, ROOT_FOLDER_FOR_IMG + "thumbnail.gif");
    
    addImageFilePath(ImageConstants.IMG_ACTOR, ROOT_FOLDER_FOR_IMG + "actor.gif");
    addImageFilePath(ImageConstants.IMG_COMPOSITE, ROOT_FOLDER_FOR_IMG + "composite.gif");
    addImageFilePath(ImageConstants.IMG_DIRECTOR, ROOT_FOLDER_FOR_IMG + "director.gif");
    addImageFilePath(ImageConstants.IMG_INPUTPORT, ROOT_FOLDER_FOR_IMG + "input.gif");
    addImageFilePath(ImageConstants.IMG_OUTPUTPORT, ROOT_FOLDER_FOR_IMG + "output.gif");
    addImageFilePath(ImageConstants.IMG_PARAMETER, ROOT_FOLDER_FOR_IMG + "parameter.gif");
  }
}