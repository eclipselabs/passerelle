/* Copyright 2011 - iSencia Belgium NV

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

package com.isencia.util.swing.components;

import java.awt.Image;
import java.beans.SimpleBeanInfo;

/**
 * A BeanInfo class for the JSpinField bean.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class JSpinFieldBeanInfo extends SimpleBeanInfo {

  /** 16x16 color icon. */
  Image icon;
  /** 32x32 color icon. */
  Image icon32;
  /** 16x16 mono icon. */
  Image iconM;
  /** 32x32 mono icon. */
  Image icon32M;

  /**
   * Constructs a new BeanInfo class for the JSpinField bean.
   */
  public JSpinFieldBeanInfo() {
    icon = loadImage("be.isencia.util.swing.images/JSpinFieldColor16.gif");
    icon32 = loadImage("be.isencia.util.swing.images/JSpinFieldColor32.gif");
    iconM = loadImage("be.isencia.util.swing.images/JSpinFieldMono16.gif");
    icon32M = loadImage("be.isencia.util.swing.images/JSpinFieldMono32.gif");
  }

  /**
   * This method returns an image object that can be used to represent the bean
   * in toolboxes, toolbars, etc.
   */
  public Image getIcon(int iconKind) {
    switch (iconKind) {
    case ICON_COLOR_16x16:
      return icon;
    case ICON_COLOR_32x32:
      return icon32;
    case ICON_MONO_16x16:
      return iconM;
    case ICON_MONO_32x32:
      return icon32M;
    }
    return null;
  }
}
