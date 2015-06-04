package com.isencia.passerelle.workbench.model.editor.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.help.WorkbenchHelpSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.editor.common.utils.ParameterUtils;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.HelpUtils;
import com.isencia.passerelle.workbench.model.editor.ui.PreferenceConstants;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.DeleteAttributeHandler;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.properties.NamedObjComparator;
import com.isencia.passerelle.workbench.model.ui.EntityNameAttribute;
import com.isencia.passerelle.workbench.model.ui.GeneralAttribute;
import com.isencia.passerelle.workbench.model.ui.command.AttributeCommand;
import com.isencia.passerelle.workbench.model.ui.command.RenameCommand;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class ActorAttributesTableViewer extends TableViewer implements CommandStackEventListener {

  public static final class AttributesContentProvider implements IStructuredContentProvider, ChangeListener {
    private final static Attribute[] EMPTY_ATTRS = new Attribute[0];
    private Object[] attributes;
    private NamedObj entity;
    private final TableViewer viewer;

    public AttributesContentProvider(TableViewer viewer, NamedObj entity) {
      this.attributes = buildAttributes(entity);
      this.entity = entity;
      this.viewer = viewer;
    }

    private Object[] buildAttributes(NamedObj entity) {
      if (entity == null) {
        return EMPTY_ATTRS;
      } else {
        entity.addChangeListener(this);
        boolean expert = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.EXPERT);
        final List<Attribute> attrList = new ArrayList<Attribute>();
        if (entity instanceof Variable) {
          // In this case we need to show the name and value of the variable itself in the attrs view,
          // i.o. those of child attributes/parameters.
          // If we would ever need to handle "sub-attributes" of parameters, this split in logic must be removed.
          // But then we need to think about a way to clearly differentiate the entity/parameter itself from its sub-attributes,
          // in the table view.
          attrList.add((Attribute) entity);
        } else {
          Class<?> filter = null;
          if (entity instanceof TextAttribute) {
            filter = StringAttribute.class;
          } else {
            filter = Parameter.class;
          }
          @SuppressWarnings("unchecked")
          Iterator<Attribute> parameterIterator = entity.attributeList(filter).iterator();
          while (parameterIterator.hasNext()) {
            Attribute parameter = parameterIterator.next();
            if (!(parameter instanceof Parameter) || (ParameterUtils.isVisible(entity, (Parameter) parameter, expert))) {
              attrList.add(parameter);
            }
          }
          Collections.sort(attrList, new NamedObjComparator());
        }
        boolean addExpertElements = (entity instanceof Actor && expert);
        final List<Object> ret = addExpertElements ? new ArrayList<Object>(attrList.size() + 3) : new ArrayList<Object>(attrList.size() + 1);
        if (addExpertElements) {
          ret.add(new GeneralAttribute(GeneralAttribute.ATTRIBUTE_TYPE.TYPE, PaletteBuilder.getInstance().getType(entity.getClass())));
          ret.add(new GeneralAttribute(GeneralAttribute.ATTRIBUTE_TYPE.CLASS, entity.getClass().getName()));
        }
        ret.add(new EntityNameAttribute(GeneralAttribute.ATTRIBUTE_TYPE.NAME, entity));
        ret.addAll(attrList);
        return ret.toArray(new Object[ret.size()]);
      }
    }

    public void dispose() {
      if (entity != null) {
        entity.removeChangeListener(this);
      }
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      if (entity != null) {
        entity.removeChangeListener(this);
      }
      if (newInput instanceof NamedObj) {
        entity = (NamedObj) newInput;
        attributes = buildAttributes(entity);
      } else {
        entity = null;
        attributes = buildAttributes(null);
      }
    }

    public Object[] getElements(Object inputElement) {
      return attributes;
    }

    @Override
    public void changeExecuted(ChangeRequest change) {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          viewer.refresh();
        }
      });
    }

    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          viewer.refresh();
        }
      });
    }
  }

  private static Logger LOGGER = LoggerFactory.getLogger(ActorAttributesTableViewer.class);

  private VariableEditingSupport valueColumnEditor;

  private NamedObj entity;
  private IWorkbenchPart actorSourcePart;

  public ActorAttributesTableViewer(NamedObj entity, IWorkbenchPart actorSourcePart, Composite parent, int style) {
    super(parent, style);
    this.entity = entity;
    this.actorSourcePart = actorSourcePart;
    getTable().setLinesVisible(true);
    getTable().setHeaderVisible(true);
    createColumns();
    setUseHashlookup(true);
    setColumnProperties(new String[] { "Property", "Value" });
    createPopupMenu();
    getTable().addKeyListener(new KeyListener() {
      public void keyReleased(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.F1) {
          try {
            showHelpSelectedParameter();
          } catch (IllegalActionException e1) {
          }
        }
        if (e.character == SWT.DEL) {
          try {
            deleteSelectedParameter();
          } catch (IllegalActionException e1) {
            LOGGER.error("Cannot delete ", e1);
          }
        }
      }
    });
    setContentProvider(new AttributesContentProvider(this, entity));
  }

  public void createTableModel(final IWorkbenchPart selectedEntitySourcePart, final NamedObj selectedEntity) {
    this.actorSourcePart = selectedEntitySourcePart;
    this.entity = selectedEntity;
    setContentProvider(new AttributesContentProvider(this, selectedEntity));
    try {
      if (getControl() != null && !getControl().isDisposed()) {
        setInput(entity);
        refresh();
      }
    } catch (Exception e) {
      LOGGER.error("Cannot set input", e);
    }
  }

  public void stackChanged(CommandStackEvent event) {
    refresh();
  }

  public void clear() {
    if (actorSourcePart != null && actorSourcePart instanceof PasserelleModelMultiPageEditor) {
      ((PasserelleModelMultiPageEditor) actorSourcePart).getEditor().getEditDomain().getCommandStack().removeCommandStackEventListener(this);
    }
    this.actorSourcePart = null;
    this.entity = null;
    if (getControl() != null && !getControl().isDisposed()) {
      setInput(null);
      refresh();
    }
  }

  public void dispose() {
    if (actorSourcePart != null && actorSourcePart instanceof PasserelleModelMultiPageEditor) {
      ((PasserelleModelMultiPageEditor) actorSourcePart).getEditor().getEditDomain().getCommandStack().removeCommandStackEventListener(this);
    }
    this.actorSourcePart = null;
    this.entity = null;
  }

  private void createColumns() {
    final TableViewerColumn name = new TableViewerColumn(this, SWT.LEFT, 0);
    name.getColumn().setText("Property");
    name.getColumn().setWidth(200);
    name.setLabelProvider(new PropertyLabelProvider());
    final TableViewerColumn value = new TableViewerColumn(this, SWT.LEFT, 1);
    value.getColumn().setText("Value");
    value.getColumn().setWidth(700);
    value.setLabelProvider(new VariableLabelProvider(this));
    this.valueColumnEditor = new VariableEditingSupport(this);
    value.setEditingSupport(valueColumnEditor);
  }

  public boolean canEditAttribute(final Object attribute) {
    return valueColumnEditor.canEdit(attribute);
  }

  public void deleteSelectedParameter() throws IllegalActionException {
    final ISelection sel = getSelection();
    if (sel != null && sel instanceof StructuredSelection) {
      final StructuredSelection s = (StructuredSelection) sel;
      final Object o = s.getFirstElement();
      if (o instanceof String)
        return; // Cannot delete name
      if (o instanceof Attribute) {
        setAttributeValue(o, null);
      }
    }
  }

  public void showHelpSelectedParameter() throws IllegalActionException {
    final ISelection sel = getSelection();
    if (sel != null && sel instanceof StructuredSelection) {
      final StructuredSelection s = (StructuredSelection) sel;
      final Object o = s.getFirstElement();
      String contextId = HelpUtils.getContextId(o);
      if (contextId != null) {
        // TODO revert this when using context specific help
        // WorkbenchHelp.displayHelp(contextId);
        WorkbenchHelpSystem.getInstance().displayHelpResource(contextId);
      }
    }
  }

  public void setActorName(final GeneralAttribute element, String name) {
    if (ModelUtils.isNameLegal(name)) {
      element.setValue(name);
      try {
        final RenameCommand cmd = new RenameCommand(this, entity, name, element);
        executeMethodOnEditorCommandStack(cmd);
      } catch (Exception ne) {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Invalid Name", ne.getMessage());
      }
    } else {
      MessageDialog.openError(Display.getCurrent().getActiveShell(), "Invalid Name", "The name '" + name + "' is not allowed.\n\n"
          + "Names should not contain '.'");
    }
  }

  public void setAttributeValue(Object element, Object value) throws IllegalActionException {
    executeMethodOnEditorCommandStack(new AttributeCommand(this, element, value));
  }

  private void executeMethodOnEditorCommandStack(final Command cmd) {
    // TODO make sure the editors are change listeners on the passerelle model,
    // then the explicit refresh calls should no longer be done here.
    if (this.actorSourcePart instanceof PasserelleModelMultiPageEditor) {
      final PasserelleModelMultiPageEditor ed = (PasserelleModelMultiPageEditor) this.actorSourcePart;
      ed.getEditor().getEditDomain().getCommandStack().execute(cmd);
      ed.getEditorSite().getActionBars().getToolBarManager().update(true);
      ed.getEditor().refresh();
    } else if (this.actorSourcePart instanceof PasserelleModelEditor) {
      final PasserelleModelEditor ed = (PasserelleModelEditor) this.actorSourcePart;
      ed.getEditDomain().getCommandStack().execute(cmd);
      ed.getEditorSite().getActionBars().getToolBarManager().update(true);
      ed.refresh();
    } else if (this.actorSourcePart instanceof DiagramEditor) {
      // TODO eventually this must be migrated to the graphiti/EMF command stack
      // at this stage a direct execution is the only option, but this prevents undo/redo...
      cmd.execute();
      ((DiagramEditor) this.actorSourcePart).getDiagramBehavior().refresh();
    }
  }

  /**
   * Initialize the menu.
   */
  private void createPopupMenu() {
    MenuManager menuMan = new MenuManager();
    menuMan.add(new Action("Delete Attribute", Activator.getImageDescriptor("icons/delete_obj.gif")) {
      public void run() {
        (new DeleteAttributeHandler()).run(null);
      }
    });
    menuMan.add(new Separator());
    menuMan.add(new Action("Help", Activator.getImageDescriptor("icons/help.gif")) {
      public void run() {
        try {
          showHelpSelectedParameter();
        } catch (IllegalActionException e) {
        }
      }
    });
    menuMan.add(new Action("Help Contents", Activator.getImageDescriptor("icons/help.gif")) {
      public void run() {
        WorkbenchHelpSystem.getInstance().displayHelp();
      }
    });
    getControl().setMenu(menuMan.createContextMenu(getControl()));
  }
}
