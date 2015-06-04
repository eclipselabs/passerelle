package com.isencia.passerelle.workbench;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
//import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
//import org.eclipse.ui.internal.WorkbenchMessages;
//import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.CommandAction;
//import org.eclipse.ui.internal.registry.ActionSetRegistry;
//import org.eclipse.ui.internal.registry.IActionSetDescriptor;
//import org.eclipse.ui.internal.registry.ViewDescriptor;
//import org.eclipse.ui.internal.registry.ViewRegistry;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
  // them
  // in the fill methods. This ensures that the actions aren't recreated
  // when fillActionBars is called with FILL_PROXY.

  // File Actions
	private IContributionItem newWizardShortList;
	private IWorkbenchAction closeAction;
	private IWorkbenchAction createSubmodelAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAsAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction exitAction;
	// Edit Actions
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;

	// Import/Export
	private IWorkbenchAction importAction;
	private IWorkbenchAction exportAction;

	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction closeEditorAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;

	// Run Actions
	private IAction runConfigurationsAction;

	// Window Actions
	private IContributionItem viewList;
	private IAction preferencesAction;

	private IContributionItem perspectiveList;
	private IAction customizePerspective;
	private IAction resetPerspective;
	private IAction savePerspective;
	private IAction closePerspective;
	private IAction closeAllPerspective;

	// Help Actions
	private IWorkbenchAction showHelpAction;
	private IWorkbenchAction searchHelpAction;
	private IWorkbenchAction dynamicHelpAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction updateSoftware;
	private IWorkbenchAction checkUpdates;
	private IWorkbenchAction keyAssist;
  private CommandAction restartAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	/**
	 * Creates the actions and registers them. Registering is needed to ensure
	 * that key bindings work. The corresponding commands keybindings are
	 * defined in the plugin.xml file. Registering also provides automatic
	 * disposal of the actions when the window is closed.
	 * 
	 */
	protected void makeActions(final IWorkbenchWindow window) {
		// File
		closeAction = ActionFactory.CLOSE.create(window);
		register(closeAction);
		createSubmodelAction = ActionFactory.EXPORT.create(window);
		register(createSubmodelAction);
		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		register(closeAllAction);
		saveAction = ActionFactory.SAVE.create(window);
		register(saveAction);
		saveAsAction = ActionFactory.SAVE_AS.create(window);
		register(saveAsAction);
		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		register(saveAllAction);
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
    restartAction = new CommandAction(window, IWorkbenchCommandConstants.FILE_RESTART);
    restartAction.setId("restart");
    restartAction.setText("Restart");
    restartAction.setToolTipText("Restart the workbench");
    register(restartAction);

		// Import - Export
		importAction = ActionFactory.IMPORT.create(window);
		register(importAction);
		exportAction = ActionFactory.EXPORT.create(window);
		register(exportAction);
		// Edit
		undoAction = ActionFactory.UNDO.create(window);
		register(undoAction);
		redoAction = ActionFactory.REDO.create(window);
		register(redoAction);
		cutAction = ActionFactory.CUT.create(window);
		register(cutAction);
		copyAction = ActionFactory.COPY.create(window);
		register(copyAction);
		pasteAction = ActionFactory.PASTE.create(window);
		register(pasteAction);
		deleteAction = ActionFactory.DELETE.create(window);
		register(deleteAction);

		closeEditorAction = ActionFactory.CLOSE.create(window);
		register(closeEditorAction);

		// Run
		// runConfigurationsAction = new
		// org.eclipse.debug.ui.actions.internal.ui.actions.OpenRunConfigurations();
		// register(runConfigurationsAction);

		// Window
		perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST
				.create(window);

		resetPerspective = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(resetPerspective);

		savePerspective = ActionFactory.SAVE_PERSPECTIVE.create(window);
		register(savePerspective);

		customizePerspective = ActionFactory.EDIT_ACTION_SETS.create(window);
		register(customizePerspective);

		closePerspective = ActionFactory.CLOSE_PERSPECTIVE.create(window);
		register(closePerspective);

		closeAllPerspective = ActionFactory.CLOSE_ALL_PERSPECTIVES
				.create(window);
		register(closeAllPerspective);

		viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		newWizardShortList = ContributionItemFactory.NEW_WIZARD_SHORTLIST
				.create(window);
		preferencesAction = ActionFactory.PREFERENCES.create(window);
		register(preferencesAction);

		// Help
		showHelpAction = ActionFactory.HELP_CONTENTS.create(window);
		register(showHelpAction);

		searchHelpAction = ActionFactory.HELP_SEARCH.create(window);
		register(searchHelpAction);

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window);
		register(dynamicHelpAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);

//		// Remove unwanted actions
//		ActionSetRegistry reg = WorkbenchPlugin.getDefault()
//				.getActionSetRegistry();
//		IActionSetDescriptor actionSets[] = reg.getActionSets();
//		HashSet<String> actionsToBeRemoved = new HashSet<String>();
//		actionsToBeRemoved
//				.add("org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");
//		actionsToBeRemoved
//				.add("org.eclipse.ui.edit.text.actionSet.openExternalFile");
//
//		actionsToBeRemoved.add("org.eclipse.ui.actionSet.openFiles");
//		for (int i = 0; i < actionSets.length; i++) {
//			if (actionsToBeRemoved.contains(actionSets[i].getId())) {
//				org.eclipse.core.runtime.IExtension ext = actionSets[i]
//						.getConfigurationElement().getDeclaringExtension();
//				reg.removeExtension(ext, new Object[] { actionSets[i] });
//			}
//		}
//
//		// Remove unwanted views
//		HashSet<String> viewsToBeRemoved = new HashSet<String>();
//		viewsToBeRemoved.add("org.eclipse.ui.views.TaskList");
//		viewsToBeRemoved.add("org.eclipse.ui.views.ProblemView");
//		viewsToBeRemoved.add("org.eclipse.ui.views.BookmarkView");
//		viewsToBeRemoved.add("org.eclipse.ui.views.ProgressView");
//		viewsToBeRemoved.add("org.eclipse.ui.views.TaskList");
//
//		ViewRegistry viewReg = (ViewRegistry) WorkbenchPlugin.getDefault()
//				.getViewRegistry();
//		IViewDescriptor viewDescriptors[] = viewReg.getViews();
//		for (int i = 0; i < viewDescriptors.length; i++) {
//			if (viewsToBeRemoved.contains(viewDescriptors[i].getId())
//					&& (viewDescriptors[i] instanceof ViewDescriptor)) {
//				ViewDescriptor descriptor = (ViewDescriptor) viewDescriptors[i];
//				org.eclipse.core.runtime.IExtension ext = descriptor
//						.getConfigurationElement().getDeclaringExtension();
//				viewReg.removeExtension(ext,
//						new Object[] { viewDescriptors[i] });
//			}
//		}

	}

	protected void fillMenuBar(IMenuManager menuBar) {
		// TODO replace constants with Intl Strings
		// Messages.getString("ApplicationActionBarAdvisor.window")
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		MenuManager fileNewWizardMenu = new MenuManager("New");
		MenuManager editMenu = new MenuManager(
				"Edit", IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$        
		MenuManager runMenu = new MenuManager(
				"&Run", IWorkbenchActionConstants.M_LAUNCH); //$NON-NLS-1$        
		MenuManager windowMenu = new MenuManager(
				"&Window", IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$        
		MenuManager windowPerspectiveMenu = new MenuManager("Open Perspective");
		MenuManager windowShowViewMenu = new MenuManager("Show View");

		MenuManager softwareMenu = new MenuManager("&Software",
				IWorkbenchActionConstants.M_HELP);
		MenuManager helpMenu = new MenuManager("&Help", "Help");

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		// Add a group marker indicating where action set menus will appear.
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(runMenu);
		menuBar.add(windowMenu);
		menuBar.add(softwareMenu);
		menuBar.add(helpMenu);

		// File
		fileNewWizardMenu.add(newWizardShortList);
		fileMenu.add(fileNewWizardMenu);
		fileMenu.add(new Separator());
		fileMenu.add(closeAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(new Separator());
		fileMenu.add(importAction);
		fileMenu.add(exportAction);
		fileMenu.add(new Separator());
    fileMenu.add(restartAction);
		fileMenu.add(exitAction);
		// Edit
		editMenu.add(undoAction);
		editMenu.add(redoAction);
		editMenu.add(new Separator());
		editMenu.add(cutAction);
		editMenu.add(copyAction);
		editMenu.add(pasteAction);
		editMenu.add(new Separator());
		editMenu.add(deleteAction);

		// Window
		windowPerspectiveMenu.add(perspectiveList);
		windowMenu.add(windowPerspectiveMenu);
		windowShowViewMenu.add(viewList);
		windowMenu.add(windowShowViewMenu);
		windowMenu.add(new Separator());
		windowMenu.add(customizePerspective);
		windowMenu.add(resetPerspective);
		windowMenu.add(savePerspective);
		windowMenu.add(closePerspective);
		windowMenu.add(closeAllPerspective);

		windowMenu.add(new Separator());
		windowMenu.add(preferencesAction);

		// Help

		helpMenu.add(showHelpAction);
		helpMenu.add(searchHelpAction);
		helpMenu.add(dynamicHelpAction);
		helpMenu.add(new Separator());
		helpMenu.add(aboutAction);
		helpMenu.add(new Separator());
	}

	protected void fillCoolBar(ICoolBarManager coolBar) {
		// File
		IToolBarManager fileToolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		fileToolbar.add(saveAction);
		fileToolbar.add(saveAllAction);
		coolBar.add(new ToolBarContributionItem(fileToolbar, "file"));

		// Edit
		IToolBarManager editToolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		editToolbar.add(undoAction);
		editToolbar.add(redoAction);
		editToolbar.add(new Separator());
		editToolbar.add(cutAction);
		editToolbar.add(copyAction);
		editToolbar.add(pasteAction);
		editToolbar.add(new Separator());
		editToolbar.add(deleteAction);
		coolBar.add(new ToolBarContributionItem(editToolbar, "edit"));

		// Run
		IToolBarManager runToolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		// runToolbar.add(undoAction);
		// runToolbar.add(redoAction);
		coolBar.add(new ToolBarContributionItem(runToolbar, "run"));

		// Additions
		coolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

	}
}
