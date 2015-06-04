package com.isencia.passerelle.workbench.model.editor.ui;

import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.workbench.model.editor.ui.editpart.ActorEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.OutlineEditPart;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;

public class HelpUtils {
  public static final String HELP_BUNDLE_ID = "com.isencia.passerelle.actor.help";

  // TODO enable again when we decide to use context specific help
  // public static String getContextId(Object element) {
  // if (element instanceof String) {
  // return HELP_BUNDLE_ID + ".name";
  // } else if (element instanceof Variable) {
  // return getContextIdOfVariable((Variable) element);
  // }
  // return null;
  // }

  public static String getContextId(Object element) {
    if (element instanceof String) {
      return HELP_BUNDLE_ID + ".name";

    } else if (element instanceof ActorEditPart) {

      final NamedObj parent = ((ActorEditPart) element).getEntity();
      final String actorName = parent.getClass().getName();

      String id = getHelpId(actorName);
      String path = "/" + id + "/html/" + actorName + ".html";
      return path;

    } else if (element instanceof OutlineEditPart) {
      OutlineEditPart out = (OutlineEditPart) element;
      Object model = out.getModel();
      if (model instanceof Variable) {
        final NamedObj parent = ((Variable) element).getContainer();
        final String actorName = parent.getClass().getName();
        String id = getHelpId(actorName);
        String path = "/" + id + "/html/" + actorName + "_attributes.html";
        return path;
      } else {
        String actorName = model.getClass().getName();
        String id = getHelpId(actorName);
        String path = "/" + id + "/html/" + actorName + ".html";
        return path;
      }

    } else if (element instanceof Variable) {

      final NamedObj parent = ((Variable) element).getContainer();
      final String actorName = parent.getClass().getName();
      String id = getHelpId(actorName);
      String path = "/" + id + "/html/" + actorName + "_attributes.html";
      return path;

    }
    return null;
  }

  private static String getHelpId(final String actorName) {
    String id = PaletteBuilder.getInstance().getBuildId(actorName);
    if (id == null) {
      id = HELP_BUNDLE_ID;
    } else {
      if (id.endsWith(".conf")) {
        id = id.substring(0, id.length() - 4) + "help";
      }
    }
    return id;
  }

  public static String getContextIdOfVariable(Variable param) {

    Attribute attr = (Attribute) param;
    if (param.getContainer() != null) {
      String helpBundle = HELP_BUNDLE_ID;
      String actorName = param.getContainer().getClass().getName().replace(".", "_");
      return helpBundle + "." + actorName + "_" + attr.getName();
    }
    return "";
  }
}
