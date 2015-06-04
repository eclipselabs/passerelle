package com.isencia.passerelle.workbench.model.editor.ui.palette;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteStack;
import org.eclipse.gef.palette.PanningSelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.MarqueeSelectionTool;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.part.EditorPart;
import ptolemy.actor.Director;
import com.isencia.passerelle.editor.common.model.PaletteGroup;
import com.isencia.passerelle.editor.common.model.PaletteItemDefinition;
import com.isencia.passerelle.editor.common.model.SubModelPaletteItemDefinition;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.ColorRegistry;
import com.isencia.passerelle.workbench.model.editor.ui.WorkbenchUtility;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class PaletteBuilder extends com.isencia.passerelle.editor.common.model.PaletteBuilder {
  @Override
  public void logError(Exception e) {
    EclipseUtils.logError(e, e.getMessage(), IStatus.ERROR);
    // EclipseUtils.displayErrorDialog("Error during open palette", e.getMessage());
  }

  private static PaletteBuilder builder;

  public static PaletteBuilder getInstance() {
    if (builder == null) {
      builder = new PaletteBuilder();
    }
    return builder;
  }

  public static final String FAVORITE_GROUPS = "FavoriteGroups";
  public static final String DEFAULT_FAVORITES_NAME = "Favorites";

  private CreationFactory selectedItem;

  public CreationFactory getSelectedItem() {
    return selectedItem;
  }

  public void setSelectedItem(CreationFactory selectedItem) {
    this.selectedItem = selectedItem;
  }

  public static String[] getFavoriteGroupNames() throws Exception {
    String groups = ModelUtils.getFavouritesStore().getString(FAVORITE_GROUPS);
    if (groups == null || groups.trim().equals("")) {
      return new String[] { DEFAULT_FAVORITES_NAME };
    }
    return groups.split(",");
  }

  /*
   * (non-Javadoc)
   * @see com.isencia.passerelle.editor.common.model.PaletteBuilder#newDefaultIdeIcon()
   */
  @Override
  protected Object newDefaultIdeIcon() {
    return Activator.getImageDescriptor("icons/ide.gif");
  }

  @Override
  protected Object newDefaultFolderIcon() {
    return Activator.getImageDescriptor("icons/folder.gif");
  }

  @Override
  protected Object createIcon(Object defaultIcon, String iconClazzAttribute, String iconAttribute, String bundleId) {
    try {
      if (iconAttribute != null && !iconAttribute.trim().equals("")) {
        return Activator.getImageDescriptor(bundleId, iconAttribute);
      }
    } catch (Exception e) {

    }
    if (defaultIcon == null) {
      return Activator.getImageDescriptor("icons/ide.gif");
    }
    return defaultIcon;
  }

  PaletteBuilder() {
    super();
    // TODO Auto-generated constructor stub
  }

  private static PaletteRoot paletteRoot;

  public static PaletteContainer createFavoriteContainer(String favoriteGroup) {
    PaletteContainer createPaletteContainer = createPaletteContainer(favoriteGroup, Activator.getImageDescriptor("icons/favourites.gif"), true);
    return createPaletteContainer;
  }

  public static void synchFavorites(PaletteViewer paletteViewer) throws Exception {

    StringBuffer containers = new StringBuffer();
    List containertLis = paletteRoot.getChildren();
    for (Object e : containertLis) {

      if (e instanceof PaletteDrawer) {
        PaletteContainer favoritesContainer = (PaletteDrawer) e;

        containers.append(favoritesContainer.getLabel());
        containers.append(",");
        StringBuffer entries = new StringBuffer();
        for (Object o : favoritesContainer.getChildren()) {
          if (o instanceof CombinedTemplateCreationEntry) {
            CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) o;
            ClassTypeFactory entryType = (ClassTypeFactory) entry.getTemplate();
            Object objectType = entryType.getObjectType();
            if (entryType.getNewObject() instanceof SubModelPaletteItemDefinition) {
              entries.append(((SubModelPaletteItemDefinition) entryType.getNewObject()).getName());
              entries.append(",");
            } else {
              entries.append(((Class) objectType).getName());
              entries.append(",");

            }
          }
          addFavoriteGroup(favoritesContainer.getLabel(), favoritesContainer);

          ModelUtils.getFavouritesStore().putValue(favoritesContainer.getLabel(), entries.toString());
        }

      }
    }
    ModelUtils.getFavouritesStore().putValue(PaletteBuilder.FAVORITE_GROUPS, containers.toString());
    WorkbenchUtility.addMouseListenerToPaletteViewer(paletteViewer);
    try {
      ModelUtils.getFavouritesStore().save();
    } catch (IOException ex) {
    }

  }

  public static PaletteContainer getDefaultFavoriteGroup() {
    PaletteEntry entry = favoritesContainers.get(PaletteBuilder.DEFAULT_FAVORITES_NAME);
    if (entry instanceof PaletteContainer) {
      return (PaletteContainer) entry;
    }
    return createFavoriteContainer(PaletteBuilder.DEFAULT_FAVORITES_NAME);
  }

  public static PaletteEntry getFavoriteGroup(String name) {
    return favoritesContainers.get(name);
  }

  public static void addFavoriteGroup(String name, PaletteEntry e) {
    favoritesContainers.put(name, e);

  }

  static public HashMap<String, PaletteEntry> favoritesContainers = new HashMap<String, PaletteEntry>();

  static private PaletteContainer createControlGroup(PaletteRoot root) {

    org.eclipse.gef.palette.PaletteGroup controlGroup = new org.eclipse.gef.palette.PaletteGroup("ControlGroup");

    List entries = new ArrayList();

    final ToolEntry tool = new PanningSelectionToolEntry();
    entries.add(tool);
    root.setDefaultEntry(tool);

    PaletteStack marqueeStack = new PaletteStack("Stack", "", null); //$NON-NLS-1$
    marqueeStack.add(new MarqueeToolEntry());
    MarqueeToolEntry marquee = new MarqueeToolEntry();
    marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR, new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED));
    marqueeStack.add(marquee);
    marquee = new MarqueeToolEntry();
    marquee.setToolProperty(MarqueeSelectionTool.PROPERTY_MARQUEE_BEHAVIOR, new Integer(MarqueeSelectionTool.BEHAVIOR_CONNECTIONS_TOUCHED
        | MarqueeSelectionTool.BEHAVIOR_NODES_CONTAINED));
    marqueeStack.add(marquee);
    marqueeStack.setUserModificationPermission(PaletteEntry.PERMISSION_NO_MODIFICATION);
    entries.add(marqueeStack);

    final ConnectionCreationToolEntry ctool = new ConnectionCreationToolEntry("Connection", "Connection", null,
        Activator.getImageDescriptor("icons/connection16.gif"), Activator.getImageDescriptor("icons/connection24.gif"));
    entries.add(ctool);
    controlGroup.addAll(entries);
    return controlGroup;
  }

  /**
   * Change to make first palette open and others closed, as we will put the most important actors in this palette.
   * 
   * @param name
   * @param image
   * @return
   */
  static private PaletteContainer createPaletteContainer(final String name, final ImageDescriptor image, final boolean open) {

    PaletteDrawer drawer = new PaletteDrawer(name, image);
    if (open) {
      drawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
    } else {
      drawer.setInitialState(PaletteDrawer.INITIAL_STATE_CLOSED);
    }

    return drawer;
  }

  private EditorPart parent;

  public PaletteRoot createPalette(EditorPart parent) throws Exception {
    this.parent = parent;
    if (paletteRoot == null) {
      paletteRoot = new PaletteRoot();
      paletteRoot.addAll(createCategories(paletteRoot, parent, null));

    }

    return paletteRoot;
  }

  List createCategories(PaletteRoot root, EditorPart parent, PaletteGroup utilitiesGroup) throws Exception {

    List categories = new ArrayList();
    categories.add(createControlGroup(root));
    if (utilitiesGroup != null) {
      PaletteContainer paletteContainer = createPaletteContainer(utilitiesGroup.getName(), (ImageDescriptor) utilitiesGroup.getIcon(), true);
      for (PaletteItemDefinition def : utilitiesGroup.getPaletteItems()) {
        CombinedTemplateCreationEntry entry = createPaletteEntryFromPaletteDefinition(def);
        paletteContainer.add(entry);
      }

      categories.add(paletteContainer);
    }
    String[] favoriteGroups = getFavoriteGroupNames();
    for (String favoriteGroup : favoriteGroups) {
      PaletteContainer createPaletteContainer = createFavoriteContainer(favoriteGroup);
      createPaletteContainer.setDescription("Click and drag favourite actors from the 'Palette' view.");
      favoritesContainers.put(favoriteGroup, createPaletteContainer);
      categories.add(createPaletteContainer);
      String favorites = ModelUtils.getFavouritesStore().getString(favoriteGroup);
      if (favorites != null && !favorites.trim().equals("")) {
        String[] names = favorites.split(",");
        for (String name : names) {
          addFavorite(name, (PaletteContainer) createPaletteContainer);
        }
      }
    }

    return categories;
  }

  public boolean containsFavorite(PaletteContainer container, Object type, Object name) {
    List children = container.getChildren();
    for (Object child : children) {
      if (child instanceof CombinedTemplateCreationEntry) {
        CombinedTemplateCreationEntry entry = (CombinedTemplateCreationEntry) child;
        ClassTypeFactory entryType = (ClassTypeFactory) entry.getTemplate();
        if ((((Class) entryType.getObjectType()).getName().equals(type) && entryType.getNewObject().equals(name))
            || (entryType.getNewObject() instanceof SubModelPaletteItemDefinition && ((SubModelPaletteItemDefinition) entryType.getNewObject()).getName()
                .equals(name))) {
          return true;
        }
      }
    }
    return false;
  }

  public void removeFavorite(String name) throws Exception {
    ModelUtils.getFavouritesStore().putValue(name, "");
  }

  public boolean addFavorite(String className, PaletteContainer container) {
    PaletteItemDefinition paletteItem = getPaletteItem(className);
    if (paletteItem == null) {
      // not in default actors palette, maybe it's a submodel?
      paletteItem = getSubModelGroup().getPaletteItem(className);
    }
    if (paletteItem != null && !containsFavorite(container, className, paletteItem.getName())) {
      CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition = createPaletteEntryFromPaletteDefinition(paletteItem);
      container.add(createPaletteEntryFromPaletteDefinition);
      return true;
    }
    return false;
  }

  public Color getColor(Class clazz) {
    if (clazz == null) {
      return null;
    }
    return getColor(clazz.getName());
  }

  public Color getColor(String clazz) {
    PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
    if (itemDefinition != null) {
      return ColorRegistry.getInstance().getColor(itemDefinition.getColor());
    }
    return null;
  }

  public String getType(Class clazz) {
    if (clazz.equals(Flow.class)) {
      return "Subflow";
    }
    return getType(clazz.getName());
  }

  public String getType(String clazz) {
    try {
      if (Director.class.isAssignableFrom(Class.forName(clazz))) {
        return "Director";
      }
    } catch (ClassNotFoundException e) {

    }
    PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
    if (itemDefinition != null) {
      return itemDefinition.getName();
    }
    return clazz;
  }

  public static CombinedTemplateCreationEntry createPaletteEntryFromPaletteDefinition(PaletteItemDefinition def) {
    if (def instanceof SubModelPaletteItemDefinition) {
      return new CombinedTemplateCreationEntry(def.getName(), def.getName(), new ClassTypeFactory(def.getClazz(), (SubModelPaletteItemDefinition) def),
          (ImageDescriptor) def.getIcon(), //$NON-NLS-1$
          (ImageDescriptor) def.getIcon()//$NON-NLS-1$
      );
    } else {
      return new CombinedTemplateCreationEntry(def.getName(), def.getName(), new ClassTypeFactory(def.getClazz(), def.getName()),
          (ImageDescriptor) def.getIcon(), //$NON-NLS-1$
          (ImageDescriptor) def.getIcon()//$NON-NLS-1$
      );
    }

  }

  public void removeSubModel(String name) throws Exception {
    PaletteGroup submodels = getSubModelGroup();
    submodels.removePaletteItem(getPaletteItem(name));

  }

  public ImageDescriptor getIcon(Class clazz) {
    if (clazz == null) {
      return null;
    }
    return getIcon(clazz.getName());
  }

  public ImageDescriptor getIcon(String clazz) {
    PaletteItemDefinition itemDefinition = getPaletteItem(clazz);
    if (itemDefinition != null) {
      return (ImageDescriptor) itemDefinition.getIcon();
    }
    return Activator.getImageDescriptor("icons/folder.gif");
  }

}
