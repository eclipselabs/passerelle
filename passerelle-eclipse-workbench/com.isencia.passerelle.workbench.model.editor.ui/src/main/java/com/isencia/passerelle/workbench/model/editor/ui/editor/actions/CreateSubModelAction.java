package com.isencia.passerelle.workbench.model.editor.ui.editor.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IProject;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;

import com.isencia.passerelle.actor.gui.PasserelleActorControllerFactory;
import com.isencia.passerelle.actor.gui.PasserelleEditorFactory;
import com.isencia.passerelle.actor.gui.PasserelleEditorPaneFactory;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.PasserelleModelMultiPageEditor;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeView;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.ui.utils.FileUtils;
import com.isencia.passerelle.workbench.model.ui.wizards.NameChecker;
import com.isencia.passerelle.workbench.model.ui.wizards.NameWizard;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class CreateSubModelAction extends SelectionAction implements NameChecker {

  private static final Logger logger = LoggerFactory.getLogger(CreateSubModelAction.class);

  private PasserelleModelMultiPageEditor parent;
  private final String icon = "icons/flow.png";
  public static String CREATE_SUBMODEL = "createSubModel";

  /**
   * Creates an empty model
   * 
   * @param part
   */
  public CreateSubModelAction() {
    this(null, null);
    setId(ActionFactory.NEW.getId());
  }

  /**
   * Creates an empty model
   * 
   * @param part
   */
  public CreateSubModelAction(final IEditorPart part) {
    this(part, null);
    setId(ActionFactory.NEW.getId());
  }

  /**
   * Creates the model from the contents of the part
   * 
   * @param part
   * @param parent
   */
  public CreateSubModelAction(final IEditorPart part, final PasserelleModelMultiPageEditor parent) {
    super(part);
    this.parent = parent;
    setLazyEnablementCalculation(true);
    if (parent != null)
      setId(ActionFactory.EXPORT.getId());
  }

  @Override
  protected void init() {
    super.init();
    Activator.getImageDescriptor(icon);
    setHoverImageDescriptor(Activator.getImageDescriptor(icon));
    setImageDescriptor(Activator.getImageDescriptor(icon));
    setDisabledImageDescriptor(Activator.getImageDescriptor(icon));
    setEnabled(false);

  }

  @Override
  public void run() {
    try {
      if (parent != null) {
        final Entity entity = parent.getSelectedContainer();
        final String name = getName(entity.getName());
        if (name != null) {
          entity.setName(name);
          exportEntityToClassFile(entity);
          if (parent != null && parent.getActorTreeViewPage() != null)
            parent.getActorTreeViewPage().refresh();
        }
      } else {
        final String name = getName("emptyComposite");
        if (name != null) {
          final InputStream stream = ModelUtils.getEmptyCompositeStream(name);
          Reader reader = new InputStreamReader(stream);
          try {
            Flow flow = FlowManager.readMoml(reader);
            Attribute ctrlFact = flow.getAttribute("_controllerFactory");
            if (ctrlFact == null) {
              new PasserelleActorControllerFactory(flow, "_controllerFactory");
            }
            Attribute editorFact = flow.getAttribute("_editorFactory");
            if (editorFact == null) {
              new PasserelleEditorFactory(flow, "_editorFactory");
            }
            Attribute editorPaneFact = flow.getAttribute("_editorPaneFactory");
            if (editorPaneFact == null) {
              new PasserelleEditorPaneFactory(flow, "_editorPaneFactory");
            }
            PaletteBuilder factory = PaletteBuilder.getInstance();

            Activator.getDefault().getRepositoryService().createSubmodel(flow);
            factory.addSubModel(null,null,flow.getName());
          } catch (Exception e) {

          }
          final IViewPart part = EclipseUtils.getPage().findView(ActorTreeView.ID);
          if (part != null && part instanceof ActorTreeView) {
            ((ActorTreeView) part).refresh();
          }
        }
      }

    } catch (Exception e) {
      logger.error("Cannot export sub-model", e);
    }
  }

  private String getName(final String name) {

    NameWizard wizard = new NameWizard(name, this);
    WizardDialog dialog = new WizardDialog(Display.getCurrent().getActiveShell(), wizard);
    dialog.create();
    dialog.getShell().setSize(400, 300);
    dialog.setTitle("Name of Composite");
    dialog.setMessage("Please choose a unique name for your exported composite.");
    if (dialog.open() == WizardDialog.OK) {
      return wizard.getRenameValue();
    }
    return null;
  }

  public Entity exportEntityToClassFile(Entity entity) throws Exception {
    CompositeActor entityAsClass = (CompositeActor) entity.clone();
    entityAsClass.setClassDefinition(true);

    Director d = entityAsClass.getDirector();
    if (d != null) {
      // remove the director from the class definition
      d.setContainer(null);
    }

    Attribute ctrlFact = entityAsClass.getAttribute("_controllerFactory");
    if (ctrlFact == null) {
      new PasserelleActorControllerFactory(entityAsClass, "_controllerFactory");
    }
    Attribute editorFact = entityAsClass.getAttribute("_editorFactory");
    if (editorFact == null) {
      new PasserelleEditorFactory(entityAsClass, "_editorFactory");
    }
    Attribute editorPaneFact = entityAsClass.getAttribute("_editorPaneFactory");
    if (editorPaneFact == null) {
      new PasserelleEditorPaneFactory(entityAsClass, "_editorPaneFactory");
    }

    PaletteBuilder factory = PaletteBuilder.getInstance();

    Activator.getDefault().getRepositoryService().createSubmodel(entityAsClass);
    Flow flow = Activator.getDefault().getRepositoryService().getSubmodel(entityAsClass.getName());
    factory.addSubModel(null, null,flow.getName());
    return entityAsClass;
  }

  @Override
  protected boolean calculateEnabled() {
    // TODO Auto-generated method stub
    return true;
  }

  public boolean isSubModel(String name) {
    return Activator.getDefault().getRepositoryService().getSubmodel(name) != null;
  }

  public boolean isNameValid(final String name) {
    return !isSubModel(name);
  }

  public String getErrorMessage(String name) {
    if (isSubModel(name)) {
      return "'" + name + "' is already existing as a composite.";
    }
    return null;
  }
}
