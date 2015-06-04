package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.internal.GEFMessages;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightRetargetAction;
import org.eclipse.gef.ui.actions.MatchWidthRetargetAction;
import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.CommitFlowAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RunAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.ScreenshotAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.StopAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.UpdateFlowAction;

public class PasserelleActionBarContributor extends MultiPageEditorActionBarContributor {

	private ZoomComboContributionItem zoomCombo;

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#buildActions()
	 */
	protected void buildActions() {
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());

		addRetargetAction(new AlignmentRetargetAction(PositionConstants.LEFT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.CENTER));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.RIGHT));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.TOP));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.MIDDLE));
		addRetargetAction(new AlignmentRetargetAction(PositionConstants.BOTTOM));

		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());

		addRetargetAction(new MatchWidthRetargetAction());
		addRetargetAction(new MatchHeightRetargetAction());
		
		RetargetAction sa = new RetargetAction(ScreenshotAction.ID, "Create screenshot of workflow");
		sa.setImageDescriptor(Activator.getImageDescriptor("icons/camera.gif"));
		addRetargetAction(sa);

		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_RULER_VISIBILITY,
				GEFMessages.ToggleRulerVisibility_Label, IAction.AS_CHECK_BOX));

		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
				GEFMessages.ToggleSnapToGeometry_Label, IAction.AS_CHECK_BOX));

		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_GRID_VISIBILITY,
				GEFMessages.ToggleGrid_Label, IAction.AS_CHECK_BOX));
		IWorkbenchWindow iww = getPage().getWorkbenchWindow();
		addRetargetAction(new RetargetAction(ActionFactory.CLOSE.getId(), "Close editor"));
		addRetargetAction(new RetargetAction(ActionFactory.NEW.getId(),    "Create empty submodel"));
//		addRetargetAction(new RetargetAction(CommitFlowAction.ID,    "Commit flow"));
//		addRetargetAction(new RetargetAction(UpdateFlowAction.ID,    "Update flow"));
		addRetargetAction(new RetargetAction(ActionFactory.EXPORT.getId(), "Create submodel from workflow"));
		
		addRetargetAction((RetargetAction) ActionFactory.COPY.create(iww));
		addRetargetAction((RetargetAction) ActionFactory.CUT.create(iww));
		addRetargetAction((RetargetAction) ActionFactory.PASTE.create(iww));
	}

	/**
	 * @see org.eclipse.gef.ui.actions.ActionBarContributor#declareGlobalActionKeys()
	 */
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
		addGlobalActionKey(ActionFactory.SELECT_ALL.getId());
	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToToolBar(IToolBarManager)
	 */
	public void contributeToToolBar(IToolBarManager toolbarManager) {
	
		toolbarManager.add(new Separator());

		toolbarManager.add(new RunAction());
		toolbarManager.add(new StopAction());

		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_LEFT));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_CENTER));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_RIGHT));
		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_TOP));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_MIDDLE));
		toolbarManager.add(getAction(GEFActionConstants.ALIGN_BOTTOM));

		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.MATCH_WIDTH));
		toolbarManager.add(getAction(GEFActionConstants.MATCH_HEIGHT));

		toolbarManager.add(new Separator());
		toolbarManager.add(getAction(GEFActionConstants.ZOOM_IN));
		toolbarManager.add(getAction(GEFActionConstants.ZOOM_OUT));

		String[] zoomStrings = new String[] { ZoomManager.FIT_ALL, ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };
		
		this.zoomCombo = new ZoomComboContributionItem(getPage(), zoomStrings);
		toolbarManager.add(zoomCombo);
		
		toolbarManager.add(getAction(ScreenshotAction.ID));

	}

	/**
	 * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(IMenuManager)
	 */
	public void contributeToMenu(IMenuManager menubar) {
		super.contributeToMenu(menubar);
		MenuManager viewMenu = new MenuManager("Diagram");
		viewMenu.add(getAction(GEFActionConstants.ZOOM_IN));
		viewMenu.add(getAction(GEFActionConstants.ZOOM_OUT));
		viewMenu.add(new Separator());
		viewMenu.add(getAction(GEFActionConstants.TOGGLE_RULER_VISIBILITY));
		viewMenu.add(getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
		viewMenu.add(getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
		viewMenu.add(new Separator());
		viewMenu.add(getAction(GEFActionConstants.MATCH_WIDTH));
		viewMenu.add(getAction(GEFActionConstants.MATCH_HEIGHT));
		menubar.insertAfter(IWorkbenchActionConstants.M_EDIT, viewMenu);
		if (menubar.find(IWorkbenchActionConstants.GROUP_EDITOR) == null) {
			menubar.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
		}

	}

	private ActionRegistry registry = new ActionRegistry();

	/**
	 * Contains the {@link RetargetAction}s that are registered as global action
	 * handlers. We need to hold on to these so that we can remove them as
	 * PartListeners in dispose().
	 */
	private List<IAction> retargetActions = new ArrayList<IAction>();
	private List<String> globalActionKeys = new ArrayList<String>();

	/**
	 * Adds the given action to the action registry.
	 * 
	 * @param action
	 *            the action to add
	 */
	protected void addAction(IAction action) {
		getActionRegistry().registerAction(action);
	}

	/**
	 * Indicates the existence of a global action identified by the specified
	 * key. This global action is defined outside the scope of this contributor,
	 * such as the Workbench's undo action, or an action provided by a workbench
	 * ActionSet. The list of global action keys is used whenever the active
	 * editor is changed ({@link #setActiveEditor(IEditorPart)}). Keys provided
	 * here will result in corresponding actions being obtained from the active
	 * editor's <code>ActionRegistry</code>, and those actions will be
	 * registered with the ActionBars for this contributor. The editor's action
	 * handler and the global action must have the same key.
	 * 
	 * @param key
	 *            the key identifying the global action
	 */
	protected void addGlobalActionKey(String key) {
		globalActionKeys.add(key);
	}

	/**
	 * Adds the specified RetargetAction to this contributors
	 * <code>ActionRegistry</code>. The RetargetAction is also added as a
	 * <code>IPartListener</code> of the contributor's page. Also, the retarget
	 * action's ID is flagged as a global action key, by calling
	 * {@link #addGlobalActionKey(String)}.
	 * 
	 * @param action
	 *            the retarget action being added
	 */
	protected void addRetargetAction(RetargetAction action) {
		addAction(action);
		retargetActions.add(action);
		getPage().addPartListener(action);
		addGlobalActionKey(action.getId());
	}

	/**
	 * Disposes the contributor. Removes all {@link RetargetAction}s that were
	 * {@link org.eclipse.ui.IPartListener}s on the
	 * {@link org.eclipse.ui.IWorkbenchPage} and disposes them. Also disposes
	 * the action registry.
	 * <P>
	 * Subclasses may extend this method to perform additional cleanup.
	 * 
	 * @see org.eclipse.ui.part.EditorActionBarContributor#dispose()
	 */
	public void dispose() {
		for (int i = 0; i < retargetActions.size(); i++) {
			RetargetAction action = (RetargetAction) retargetActions.get(i);
			getPage().removePartListener(action);
			action.dispose();
		}
		registry.dispose();
		retargetActions = null;
		registry = null;
	}

	/**
	 * Retrieves an action from the action registry using the given ID.
	 * 
	 * @param id
	 *            the ID of the sought action
	 * @return <code>null</code> or the action if found
	 */
	protected IAction getAction(String id) {
		return getActionRegistry().getAction(id);
	}

	/**
	 * returns this contributor's ActionRegsitry.
	 * 
	 * @return the ActionRegistry
	 */
	protected ActionRegistry getActionRegistry() {
		return registry;
	}

	/**
	 * @see EditorActionBarContributor#init(IActionBars)
	 */
	public void init(IActionBars bars) {
		buildActions();
		declareGlobalActionKeys();
		super.init(bars);
	}

	/**
	 * @see org.eclipse.ui.IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActivePage(IEditorPart editor) {
		
		IActionBars bars = getActionBars();
		if (editor==null) return;
		ActionRegistry registry = (ActionRegistry) editor.getAdapter(ActionRegistry.class);
		if (registry==null) {
			zoomCombo.setZoomManager(null);

		} else {
			final PasserelleModelEditor ed = (PasserelleModelEditor)editor;
			zoomCombo.setZoomManager(ed.getZoomManager());
			for (String id : globalActionKeys) {
				bars.setGlobalActionHandler(id, registry.getAction(id));
			}
	
		}
		
	}

}
