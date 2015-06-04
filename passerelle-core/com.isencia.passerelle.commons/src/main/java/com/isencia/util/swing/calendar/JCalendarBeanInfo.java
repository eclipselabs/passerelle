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

package com.isencia.util.swing.calendar;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;
import java.beans.SimpleBeanInfo;
import java.util.Locale;
import javax.swing.JPanel;
import com.isencia.util.swing.components.LocaleEditor;

/**
 * A BeanInfo class for JCalendar.
 * 
 * @version 1.1 02/04/02
 * @author Kai Toedter
 */
public class JCalendarBeanInfo extends SimpleBeanInfo {

  /** 16x16 color icon. */
  Image icon;
  /** 32x32 color icon. */
  Image icon32;
  /** 16x16 mono icon. */
  Image iconM;
  /** 32x32 mono icon. */
  Image icon32M;

  /**
   * Constructs a new BeanInfo class for the JCalendar bean.
   */
  public JCalendarBeanInfo() {
    icon = loadImage("be.isencia.util.swing.images/JCalendarColor16.gif");
    iconM = loadImage("be.isencia.util.swing.images/JCalendarMono16.gif");
    icon32 = loadImage("be.isencia.util.swing.images/JCalendarColor32.gif");
    icon32M = loadImage("be.isencia.util.swing.images/JCalendarMono32.gif");
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

  /**
   * This method returns an array of PropertyDescriptors describing the editable
   * properties supported by this bean.
   */
  public PropertyDescriptor[] getPropertyDescriptors() {
    try {
      if (PropertyEditorManager.findEditor(Locale.class) == null) {
        BeanInfo beanInfo = Introspector.getBeanInfo(JPanel.class);
        PropertyDescriptor[] p = beanInfo.getPropertyDescriptors();

        int length = p.length;
        PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[length + 1];
        for (int i = 0; i < length; i++)
          propertyDescriptors[i + 1] = p[i];

        propertyDescriptors[0] = new PropertyDescriptor("locale", JCalendar.class);
        propertyDescriptors[0].setBound(true);
        propertyDescriptors[0].setConstrained(false);
        propertyDescriptors[0].setPropertyEditorClass(LocaleEditor.class);
        return propertyDescriptors;
      }
    } catch (IntrospectionException e) {
      e.printStackTrace();
    }
    return null;
  }
}
