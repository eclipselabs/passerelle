package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.List;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.StringAttribute;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.util.ptolemy.RegularExpressionParameter;
import com.isencia.passerelle.util.ptolemy.ResourceParameter;
import com.isencia.passerelle.util.ptolemy.StringChoiceParameter;
import com.isencia.passerelle.util.ptolemy.StringMapParameter;
import com.isencia.passerelle.workbench.model.editor.ui.Constants;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.CComboBoxPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.CheckboxPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.ColorPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.FilePickerPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.FloatPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.IntegerPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.RegularExpressionDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.ResourcePropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.StringChoicePropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.StringMapPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.TextAreaPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.descriptor.TextPropertyDescriptor;
import com.isencia.passerelle.workbench.model.editor.ui.properties.CellEditorAttribute;
import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;

/**
 * Editing support for parameter column.
 * 
 * @author gerring
 */
public class VariableEditingSupport extends EditingSupport {

  private static Logger logger = LoggerFactory.getLogger(VariableEditingSupport.class);

  private ActorAttributesTableViewer actorAttributesView;

  public VariableEditingSupport(ActorAttributesTableViewer viewer) {
    super(viewer);
    this.actorAttributesView = viewer;
  }

  // private Object previousSelection;

  @Override
  protected CellEditor getCellEditor(Object element) {

    PropertyDescriptor desc = null;

    if (element instanceof CellEditorAttribute) {
      return ((CellEditorAttribute) element).createCellEditor(getViewer().getControl());
    } else if (element instanceof GeneralAttribute) {

      if (((GeneralAttribute) element).getType().equals(GeneralAttribute.ATTRIBUTE_TYPE.NAME)) {
        //When combining TextAreaPD with plain TextPD, a height set for an area remains operational
        // for all following plain TextPDs used.
        //This slightly adapted TextPD does an explicit reset of the height.
        desc = new TextPropertyDescriptor(VariableEditingSupport.class.getName() + ".nameText", "Name");
      }

    } else if (element instanceof Variable) {
      desc = getPropertyDescriptor((Variable) element);
    } else if (element instanceof StringAttribute) {
      desc = getPropertyDescriptor((StringAttribute) element, BaseType.STRING);
    }
    if (desc != null) {
      CellEditor createPropertyEditor = desc.createPropertyEditor((Composite) getViewer().getControl());
      // TODO later revert if context specific help
      // String contextId = HelpUtils.getContextId(element);
      // if (contextId != null) {
      // try {
      // PlatformUI.getWorkbench().getHelpSystem().setHelp(
      // createPropertyEditor.getControl(), contextId);
      // PlatformUI.getWorkbench().getHelpSystem()
      // .displayDynamicHelp();
      // } catch (Exception e) {
      //
      // }
      // }
      return createPropertyEditor;
    }
    return null;

  }

  public PropertyDescriptor getPropertyDescriptor(final Variable parameter) {
    final Type type = parameter.getType();
    return getPropertyDescriptor(parameter, type);
  }

  /**
   * NOTE: the attribute may now implement 'CellEditorParameter' in which case this method is not asked for the cell editor. This allows exotic editors to be
   * created without changing the core passerelle code.
   * 
   * @param parameter
   * @param type
   * @return
   */
  public PropertyDescriptor getPropertyDescriptor(final Attribute parameter, final Type type) {

    if (parameter instanceof ColorAttribute) {
      return new ColorPropertyDescriptor(parameter.getName(), parameter.getDisplayName());
    } else if (parameter instanceof ResourceParameter) {
      ResourcePropertyDescriptor des = new ResourcePropertyDescriptor((ResourceParameter) parameter);
      return des;
      // If we use this parameter, we can sent the file extensions to the
      // editor
    } else if (parameter instanceof FileParameter) {
      FilePickerPropertyDescriptor des = new FilePickerPropertyDescriptor(parameter.getName(), parameter.getDisplayName());
      final FileParameter fp = (FileParameter) parameter;
      if (parameter instanceof com.isencia.passerelle.util.ptolemy.FileParameter) {
        des.setFilter(((com.isencia.passerelle.util.ptolemy.FileParameter) fp).getFilterExtensions());
      }
      try {
        if (fp.asFile() != null)
          des.setCurrentPath(fp.asFile().getParent());
      } catch (IllegalActionException e) {
        logger.error("Cannot get file path!", e);
      }
      return des;
    } else if (parameter instanceof RegularExpressionParameter) {
      return new RegularExpressionDescriptor((RegularExpressionParameter) parameter);
    } else if (parameter instanceof FileParameter) {
      return new FilePickerPropertyDescriptor(parameter.getName(), parameter.getDisplayName());
    } else if (parameter instanceof StringMapParameter) {
      final StringMapPropertyDescriptor des = new StringMapPropertyDescriptor((StringMapParameter) parameter);
      return des;
    } else if (parameter instanceof StringChoiceParameter) {
      final StringChoicePropertyDescriptor des = new StringChoicePropertyDescriptor((StringChoiceParameter) parameter);
      return des;
    } else if (hasOptions(parameter)) {
      final String[] choices = ((Parameter) parameter).getChoices();
      final CComboBoxPropertyDescriptor des = new CComboBoxPropertyDescriptor(parameter.getName(), parameter.getDisplayName(), choices);
      return des;
    } else if (BaseType.INT.equals(type)) {
      return new IntegerPropertyDescriptor(parameter.getName(), parameter.getDisplayName());
    } else if (BaseType.FLOAT.equals(type)) {
      return new FloatPropertyDescriptor(parameter.getName(), parameter.getDisplayName());
    } else if (BaseType.BOOLEAN.equals(type)) {
      return new CheckboxPropertyDescriptor(parameter.getName(), parameter.getDisplayName());
    } else {
      PropertyDescriptor propertyDescriptor = null;
      List txtStyleAttrs = parameter.attributeList(TextStyle.class);
      if (txtStyleAttrs != null && !txtStyleAttrs.isEmpty()) {
        propertyDescriptor = new TextAreaPropertyDescriptor(parameter.getName(), parameter.getDisplayName(), 50);
      } else {
        propertyDescriptor = new TextPropertyDescriptor(parameter.getName(), parameter.getDisplayName());
      }
      return propertyDescriptor;
    }
  }

  private boolean hasOptions(Attribute parameter) {
    if (parameter.getContainer() instanceof Actor) {
      initializeOptionsFactory((Actor) parameter.getContainer());
    }
    return parameter instanceof Parameter && ((Parameter) parameter).getChoices() != null && ((Parameter) parameter).getChoices().length > 0;
  }

  private void initializeOptionsFactory(Actor actor) {
    // try {
    // Attribute attribute = actor.getAttribute(Actor.OPTIONS_FACTORY_CFG_NAME, OptionsFactory.class);
    // if (attribute != null) {
    // actor.initialize();
    // if (actor.getOptionsFactory() != null) {
    // List parameters = actor.attributeList(Parameter.class);
    // for (Iterator iter = parameters.iterator(); iter.hasNext();) {
    // Parameter p = (Parameter) iter.next();
    // actor.getOptionsFactory().setOptionsForParameter(p);
    // }
    // }
    // }
    // } catch (IllegalActionException e) {
    // }
  }

  @Override
  protected boolean canEdit(Object element) {
    if (element instanceof GeneralAttribute)
      return ((GeneralAttribute) element).getType().equals(GeneralAttribute.ATTRIBUTE_TYPE.NAME);
    return true;
  }

  @Override
  protected Object getValue(Object element) {
    if (element instanceof GeneralAttribute)
      return ((GeneralAttribute) element).getValue();
    if (element instanceof StringAttribute)
      return ((StringAttribute) element).getExpression();
    final Variable param = (Variable) element;
    try {
      if (!param.isStringMode() && param.getToken() != null && param.getToken() instanceof BooleanToken) {
        return ((BooleanToken) param.getToken()).booleanValue();
      }
    } catch (Exception ne) {
      logger.error("Cannot set read token from " + param.getName(), ne);
    }
    return param.getExpression();
  }

  public String showHelpSelectedParameter(Variable param) {
    Attribute attr = (Attribute) param;
    if (param.getContainer() != null) {
      String helpBundle = Constants.HELP_BUNDLE_ID;
      String actorName = param.getContainer().getClass().getName().replace(".", "_");
      return helpBundle + "." + actorName + "_" + attr.getName();
    }
    return "";
  }

  @Override
  protected void setValue(Object element, Object value) {
    try {
      if (element instanceof GeneralAttribute) {
        actorAttributesView.setActorName((GeneralAttribute) element, (String) value);
      } else {
        actorAttributesView.setAttributeValue(element, value);
      }
    } catch (Exception ne) {
      logger.error("Cannot set variable value " + value, ne);
    }
  }
}
