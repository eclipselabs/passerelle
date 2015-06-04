package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.AlignmentAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.actions.StackAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleRulerVisibilityAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;
import com.isencia.passerelle.workbench.model.editor.ui.WorkbenchUtility;
import com.isencia.passerelle.workbench.model.editor.ui.dnd.FileTransferDropTargetListener;
import com.isencia.passerelle.workbench.model.editor.ui.dnd.PasserelleTemplateTransferDropTargetListener;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.BreakpointAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.CloseEditorAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.CopyNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.CreateSubModelAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.CutNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.DynamicHelpAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.HelpAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.OpenFileAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.PasteNodeAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RenameAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.RouterFactory;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.ScreenshotAction;
import com.isencia.passerelle.workbench.model.editor.ui.editor.actions.ViewAttributesAction;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.EditPartFactory;
import com.isencia.passerelle.workbench.model.editor.ui.figure.ActorFigure;
import com.isencia.passerelle.workbench.model.editor.ui.palette.DropFavouriteListener;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.ui.IPasserelleEditor;
import com.isencia.passerelle.workbench.model.ui.command.RefreshCommand;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

public class PasserelleModelEditor extends    GraphicalEditorWithFlyoutPalette
		                           implements IPasserelleEditor, 
		                                      ITabbedPropertySheetPageContributor,
		                                      IReusableEditor {

	// Static things
	private static Logger logger = LoggerFactory.getLogger(PasserelleModelEditor.class);
	protected static final String PALETTE_DOCK_LOCATION = "Dock location"; //$NON-NLS-1$
	protected static final String PALETTE_SIZE = "Palette Size"; //$NON-NLS-1$
	protected static final String PALETTE_STATE = "Palette state"; //$NON-NLS-1$
	protected static final int DEFAULT_PALETTE_SIZE = 130;
	
	public static final int DELETE_KEYCODE = 127;
	public static final int COPY_KEYCODE = 99;
	public static final int PASTE_KEYCODE = 112;

	private CompositeActor container;
	private RefreshCommand RefreshCommand;
	private int            index;
	
	private List<String> stackActionIDs = new ArrayList<String>();
	private List<String> editorActionIDs = new ArrayList<String>();
	private List<Object> editPartActionIDs = new ArrayList<Object>();

	private KeyHandler sharedKeyHandler;
	protected boolean editorSaving = false;

	private CompositeActor model = new CompositeActor();
	private ResourceTracker resourceListener = new ResourceTracker();
	private RulerComposite rulerComp;
	private PasserelleModelMultiPageEditor parent;

	public PasserelleModelEditor(PasserelleModelMultiPageEditor parent,
			                     CompositeActor                 actor,
			                     CompositeActor                 model) {
		this(parent, model);
		this.container = actor;
	}

	public PasserelleModelEditor(PasserelleModelMultiPageEditor parent,
			                     CompositeActor                 model) {
		this.parent = parent;
		this.model = model;
		setEditDomain(parent.getDefaultEditDomain());

	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		updateActions(getSelectionActions());
	}

	private ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			updateActions(editPartActionIDs);
		}
	};

	/**
	 * Returns the selection listener.
	 * 
	 * @return the <code>ISelectionListener</code>
	 */
	protected ISelectionListener getSelectionListener() {
		return selectionListener;
	}
	public PasserelleModelMultiPageEditor getParent() {

		return parent;

	}

	public PasserelleModelEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public Logger getLogger() {
		return logger;
	}

	public String getContributorId() {
		return "com.isencia.passerelle.workbench.model.editor.ui.editors.modelEditor";
	}

	protected void addStackAction(StackAction action) {
		getActionRegistry().registerAction(action);
		stackActionIDs.add(action.getId());
	}

	protected void addEditPartAction(SelectionAction action) {
		getActionRegistry().registerAction(action);
		editPartActionIDs.add(action.getId());
	}

	protected void addSelectionAction(SelectionAction action) {
		getSelectionActions().add(action);
	}

	protected void addEditorAction(EditorPartAction action) {
		getActionRegistry().registerAction(action);
		editorActionIDs.add(action.getId());
	}

	protected void addAction(IAction action) {
		getActionRegistry().registerAction(action);
	}

	protected void closeEditor(boolean save) {
		getSite().getPage().closeEditor(PasserelleModelEditor.this, save);
	}

	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getGraphicalViewer();

		PasserellRootEditPart root = new PasserellRootEditPart();

		List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);

		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);
		getSite().getKeyBindingService().registerAction(zoomIn);
		getSite().getKeyBindingService().registerAction(zoomOut);

		viewer.setRootEditPart(root);

		viewer.setEditPartFactory(createEditPartFactory());
		ContextMenuProvider provider = new PasserelleContextMenuProvider(viewer, getActionRegistry());
		viewer.setContextMenu(provider);
		getSite()
				.registerContextMenu(
						"com.isencia.passerelle.workbench.model.editor.ui.editor.contextmenu", //$NON-NLS-1$
						provider, viewer);
		GraphicalViewerKeyHandler graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(viewer);
		graphicalViewerKeyHandler.setParent(getCommonKeyHandler());

		graphicalViewerKeyHandler.put(KeyStroke.getPressed('X', SWT.CTRL, 0),
				                      getActionRegistry().getAction(ActionFactory.CUT.getId()));
		viewer.setKeyHandler(graphicalViewerKeyHandler);

		// Zoom with the mouse wheel
		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL),
				MouseWheelZoomHandler.SINGLETON);

		// Actions
		IAction showRulers = new ToggleRulerVisibilityAction(
				getGraphicalViewer());
		getActionRegistry().registerAction(showRulers);

		IAction snapAction = new ToggleSnapToGeometryAction(
				getGraphicalViewer());
		getActionRegistry().registerAction(snapAction);

		IAction showGrid = new ToggleGridAction(getGraphicalViewer());
		getActionRegistry().registerAction(showGrid);

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				handleActivationChanged(event);
			}
		};
		getGraphicalControl().addListener(SWT.Activate, listener);
		getGraphicalControl().addListener(SWT.Deactivate, listener);
	}

	protected EditPartFactory editPartFactory;

	protected EditPartFactory createEditPartFactory() {
		return editPartFactory = new EditPartFactory(getParent(), container);
	}

	protected CustomPalettePage createPalettePage() {
		return new CustomPalettePage(getPaletteViewerProvider()) {
			public void init(IPageSite pageSite) {
			}
		};
	}

	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {

			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				// viewer.setCustomizer(new LogicPaletteCustomizer());
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
			}

			protected void hookPaletteViewer(PaletteViewer viewer) {
				super.hookPaletteViewer(viewer);
			}
		};
	}

	public void dispose() {
		final IFile file = EclipseUtils.getIFile(getEditorInput());
		if (file!=null) file.getWorkspace()
				.removeResourceChangeListener(resourceListener);
//		for (AbstractBaseEditPart part : editPartFactory.getParts()) {
//			for (Image image : part.getImages())
//				image.dispose();
//		}
		// model.removeAllEntities();
		// model.removeAllPorts();
		// model.removeAllRelations();
		// model = null;

		super.dispose();
	}

	public void doSave(final IProgressMonitor progressMonitor) {
		editorSaving = true;
		getCommandStack().markSaveLocation();

		editorSaving = false;
	}

	public void doSaveAs() {
		performSaveAs();
	}

	public Object getAdapter(Class type) {
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(
					ZoomManager.class.toString());

		if (IPropertySheetPage.class == type) {
			return new TabbedPropertySheetPage(this);
		}

		return super.getAdapter(type);
	}


	protected Control getGraphicalControl() {
		return rulerComp;
	}

	/**
	 * Returns the KeyHandler with common bindings for both the Outline and
	 * Graphical Views. For example, delete is a common action.
	 */
	protected KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			// sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
			// getActionRegistry().getAction(
			// ActionFactory.DYNAMIC_HELP.getId()));
			// sharedKeyHandler
			// .put(KeyStroke.getPressed(SWT.DEL, 127, 0),
			// getActionRegistry().getAction(
			// ActionFactory.DELETE.getId()));
			sharedKeyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
					getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
			sharedKeyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT,
					0), getActionRegistry().getAction(
					GEFActionConstants.ZOOM_OUT));
		}
		return sharedKeyHandler;
	}

	public CompositeActor getDiagram() {
		return model;
	}

	private PaletteRoot palletBuilder;

	protected PaletteRoot getPaletteRoot() {
		if (palletBuilder == null) {
			try {
				palletBuilder = PaletteBuilder.getInstance().createPalette(this);
			} catch (Exception e) {
				logger.error("Cannot create platte builder", e);
			}
		}
		return palletBuilder;
	}

	public void gotoMarker(IMarker marker) {
	}

	protected void handleActivationChanged(Event event) {
		// getRefreshCommand().setModel(getDiagram());
		// getRefreshCommand().execute();
	}

	protected void initializeGraphicalViewer() {
		
		super.initializeGraphicalViewer();
		getGraphicalViewer().setContents(getDiagram());
		getGraphicalViewer().addDropTargetListener(
				new PasserelleTemplateTransferDropTargetListener(
						getGraphicalViewer()));
		
		if (getGraphicalViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED)==null) {
			getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, true);
		}
	}

	protected void createActions() {
		
		super.createActions();
		
		ActionRegistry registry = getActionRegistry();
		IAction action;

		// action = new CopyTemplateAction(this);
		// registry.registerAction(action);

		action = new MatchWidthAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new MatchHeightAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		// action = new LogicPasteTemplateAction(this);
		// registry.registerAction(action);
		// getSelectionActions().add(action.getId());
		//

		action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.LEFT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.RIGHT);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.TOP);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.BOTTOM);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.CENTER);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new AlignmentAction((IWorkbenchPart) this,
				PositionConstants.MIDDLE);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		CopyNodeAction copyAction = new CopyNodeAction(this);
		registry.registerAction(copyAction);
		getSelectionActions().add(copyAction.getId());
		CutNodeAction cutAction = new CutNodeAction(this);
		registry.registerAction(cutAction);
		getSelectionActions().add(cutAction.getId());
		PasteNodeAction pasteAction = setPasteNodeAction();
		registry.registerAction(pasteAction);
		getSelectionActions().add(pasteAction.getId());
		CloseEditorAction closeEditorAction = new CloseEditorAction(this,getParent());
		registry.registerAction(closeEditorAction);
		getSelectionActions().add(closeEditorAction.getId());
		
		CreateSubModelAction emptySubModelAction = new CreateSubModelAction(this);
		emptySubModelAction.setText("Create new submodel");
		registry.registerAction(emptySubModelAction);
		getSelectionActions().add(emptySubModelAction.getId());
		
		CreateSubModelAction subModelAction = new CreateSubModelAction(this,getParent());
		subModelAction.setText("Create submodel from this workflow");
		registry.registerAction(subModelAction);
		getSelectionActions().add(subModelAction.getId());
		
//    CommitFlowAction commitFlow = new CommitFlowAction(this, getParent());
//    commitFlow.setText("Commit flow");
//    registry.registerAction(commitFlow);
//    getSelectionActions().add(commitFlow.getId());
//    UpdateFlowAction updateFlow = new UpdateFlowAction(this, getParent());
//    updateFlow.setText("Update flow");
//    registry.registerAction(updateFlow);
//    getSelectionActions().add(updateFlow.getId());
//		
		final IToolBarManager man = getEditorSite().getActionBars().getToolBarManager();
		final Separator sep = new Separator(getClass().getName() + ".openActions");
		if (man.find(sep.getId()) == null) man.add(sep);
		
		registry.registerAction(new OpenFileAction(this, OpenFileAction.ID1));
		if (man.find(OpenFileAction.ID1) == null) man.add(registry.getAction(OpenFileAction.ID1));
		getSelectionActions().add(OpenFileAction.ID1);
		registry.registerAction(new OpenFileAction(this, OpenFileAction.ID2));
		if (man.find(OpenFileAction.ID2) == null) man.add(registry.getAction(OpenFileAction.ID2));
		getSelectionActions().add(OpenFileAction.ID2);

		RenameAction renameAction = new RenameAction(this);
		registry.registerAction(renameAction);
		getSelectionActions().add(renameAction.getId());
		
		ViewAttributesAction viewAttributes = new ViewAttributesAction(this);
		registry.registerAction(viewAttributes);
		getSelectionActions().add(viewAttributes.getId());
		
		HelpAction helpAction = new HelpAction(this, getParent());
		registry.registerAction(helpAction);
		getSelectionActions().add(helpAction.getId());
		DynamicHelpAction dynamicHelpAction = new DynamicHelpAction(this, getParent());
		registry.registerAction(dynamicHelpAction);
		getSelectionActions().add(dynamicHelpAction.getId());

		BreakpointAction breakAction = new BreakpointAction(this, getParent());
		registry.registerAction(breakAction);
		getSelectionActions().add(breakAction.getId());
		
		ScreenshotAction screenshotAction = new ScreenshotAction(this);
		screenshotAction.setText("Create screenshot from this workflow");
		registry.registerAction(screenshotAction);
		getSelectionActions().add(ScreenshotAction.ID);

		RouterFactory.createRouterActions(getEditorSite().getActionBars());
		RouterFactory.createConnectionActions(getEditorSite().getActionBars());
		
	}

	protected PasteNodeAction setPasteNodeAction() {
		return new PasteNodeAction(this, getDiagram());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditor#createGraphicalViewer(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected void createGraphicalViewer(Composite parent) {

		rulerComp = new RulerComposite(parent, SWT.NONE);
		super.createGraphicalViewer(rulerComp);

		ScrollingGraphicalViewer graphicalViewer = (ScrollingGraphicalViewer) getGraphicalViewer();
		rulerComp.setGraphicalViewer(graphicalViewer);

		GraphicalViewerKeyHandler graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(
				graphicalViewer);
		KeyHandler keyHandler = new KeyHandler();

		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));

		keyHandler.put(KeyStroke.getPressed(SWT.F2, 0), getActionRegistry()
				.getAction(ActionFactory.HELP_CONTENTS.getId()));
		keyHandler.put(KeyStroke.getPressed(SWT.F3, 0), getActionRegistry()
				.getAction(ActionFactory.RENAME.getId()));
		graphicalViewerKeyHandler.setParent(keyHandler);
		graphicalViewer.setKeyHandler(graphicalViewerKeyHandler);

		getGraphicalViewer().addDropTargetListener(
				new FileTransferDropTargetListener(getGraphicalViewer()));

		EditDomain editDomain = getGraphicalViewer().getEditDomain();
		customizePalette(editDomain);
		getGraphicalViewer().addDropTargetListener(
				new FileTransferDropTargetListener(getGraphicalViewer()));


		// We can get the editor action associated with highlighting actors
		//TODO FIXME - After DAWB 0.8.2 move the selection feature to this
		// plugin and keep everything in passerelle! This is a soft reference to
		// dawb but there should be none at all in passerelle.
		try {
			final ScopedPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(),"org.dawb.workbench.ui");
			final boolean isSel = store.getBoolean("org.dawb.actor.highlight.choice");
			final ActionContributionItem action = (ActionContributionItem)getEditorSite().getActionBars().getToolBarManager().find("org.edna.workbench.editors.highlightWorkflowAction");
			action.getAction().setChecked(isSel);
		} catch (Throwable ne) {
			logger.error("Cannot read dawb preferences because this is not dawb!");
		}
	}

	// TODO implement a way to modify the paletteviewer
	private void customizePalette(EditDomain editDomain) {
		if (editDomain != null) {
			PaletteViewer paletteViewer = editDomain.getPaletteViewer();
			WorkbenchUtility.addMouseListenerToPaletteViewer(paletteViewer);
			DropTarget dt = new DropTarget(paletteViewer.getControl(),
					DND.DROP_MOVE);

			dt.setTransfer(new Transfer[] { TemplateTransfer.getInstance() });
			dt.addDropListener(new DropFavouriteListener(paletteViewer));
			paletteViewer.setCustomizer(new PasserellePaletteCustomizer(
					paletteViewer));

			ContextMenuProvider provider = new PasserellePaletteContextMenuProvider(
					paletteViewer, parent);
			paletteViewer.setContextMenu(provider);

		}
	}

	/**
	 * By default, this method returns a FlyoutPreferences object that stores
	 * the flyout settings in the GEF plugin. Sub-classes may override.
	 * 
	 * @return the FlyoutPreferences object used to save the flyout palette's
	 *         preferences
	 */
	protected FlyoutPreferences getPalettePreferences() {
		// We want the palette to look like RCP Developer
		final FlyoutPreferences prefs = super.getPalettePreferences();
		prefs.setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
		prefs.setDockLocation(PositionConstants.WEST);
		prefs.setPaletteWidth(180); // Some Workbench have long actor names
		
		return prefs;
	}


	protected FigureCanvas getEditor() {
		return (FigureCanvas) getGraphicalViewer().getControl();
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	protected boolean performSaveAs() {
		getCommandStack().markSaveLocation();
		return true;
	}

	public void setInput(IEditorInput input) {
		superSetInput(input);
	}

	public void setDiagram(CompositeActor diagram) {
		model = diagram;
	}

	protected void superSetInput(IEditorInput input) {
		// The workspace never changes for an editor. So, removing and re-adding
		// the
		// resourceListener is not necessary. But it is being done here for the
		// sake
		// of proper implementation. Plus, the resourceListener needs to be
		// added
		// to the workspace the first time around.
		if (getEditorInput() != null) {
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.getWorkspace().removeResourceChangeListener(resourceListener);
		}

		super.setInput(input);

		if (getEditorInput() != null) {
			IFile file = EclipseUtils.getIFile(input);
			if (file!=null) file.getWorkspace().addResourceChangeListener(resourceListener);
			setPartName(input.getName());
		}
	}

	// This class listens to changes to the file system in the workspace, and
	// makes changes accordingly.
	// 1) An open, saved file gets deleted -> close the editor
	// 2) An open file gets renamed or moved -> change the editor's input
	// accordingly
	class ResourceTracker implements IResourceChangeListener,
			IResourceDeltaVisitor {
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				if (delta != null)
					delta.accept(this);
			} catch (CoreException exception) {
				// What should be done here?
			}
		}

		public boolean visit(IResourceDelta delta) {
			if (delta == null
					|| !delta.getResource().equals(
							((IFileEditorInput) getEditorInput()).getFile()))
				return true;

			if (delta.getKind() == IResourceDelta.REMOVED) {
				Display display = getSite().getShell().getDisplay();
				if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) { // if
					// the
					// file
					// was
					// deleted
					// NOTE: The case where an open, unsaved file is deleted is
					// being handled by the
					// PartListener added to the Workbench in the initialize()
					// method.
					display.asyncExec(new Runnable() {
						public void run() {
							if (!isDirty())
								closeEditor(false);
						}
					});
				} else { // else if it was moved or renamed
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getMovedToPath());
					display.asyncExec(new Runnable() {
						public void run() {
							superSetInput(new FileEditorInput(newFile));
						}
					});
				}
			} else if (delta.getKind() == IResourceDelta.CHANGED) {
				if (!editorSaving) {
					// the file was overwritten somehow (could have been
					// replaced by another
					// version in the respository)
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getFullPath());
					Display display = getSite().getShell().getDisplay();
					display.asyncExec(new Runnable() {
						public void run() {
							setInput(new FileEditorInput(newFile));
							getCommandStack().flush();
						}
					});
				}
			}
			return false;
		}
	}

	public CompositeActor getContainer() {
		if (container != null)
			return container;
		return model;
	}

	private RefreshCommand getRefreshCommand() {
		if (RefreshCommand == null) {
			return RefreshCommand = new RefreshCommand();
		}
		return RefreshCommand;
	}
	
	
	@Override
	public SelectionSynchronizer getSelectionSynchronizer() {
		return super.getSelectionSynchronizer();
	}

	@Override
	public GraphicalViewer getGraphicalViewer() {
		return super.getGraphicalViewer();
	}
	
	@Override
	public DefaultEditDomain getEditDomain() {
		return super.getEditDomain();
	}

	@Override
	public ActionRegistry getActionRegistry() {
		return super.getActionRegistry();
	}

	public void refresh() {
		getGraphicalViewer().setContents(getDiagram());
	}

	/**
	 * 
	 * @param actorName
	 * @param isSelected
	 * @param colorCode - One of the SWT color codes, for instance SWT.COLOR_RED.
	 * not used if isSelected = false
	 */
	public void setActorSelected(final String actorName, final boolean isSelected, final int colorCode) {
		
		if (actorName==null) {
			clearActorSelections();
			return;
		}

		final ComponentEntity sel = ModelUtils.findEntityByName(getContainer(), actorName);
		GraphicalViewer gv = getGraphicalViewer();
		final Map<?, ?> reg = gv.getEditPartRegistry();
		final AbstractGraphicalEditPart part = (AbstractGraphicalEditPart)reg.get(sel);
		if (part!=null) {
			if (isSelected) {
			    part.setSelected(EditPart.SELECTED);
			    final Color selectionColor = Display.getCurrent().getSystemColor(colorCode);
			    final LineBorder border    = new SpecialLineBorder(selectionColor, 1);
			    part.getFigure().setBorder(border);
			} else {
				part.setSelected(EditPart.SELECTED_NONE);
			    part.getFigure().setBorder(null);
			}
		}
	}
	

	public void setPortSelected(String actorName, String portName, boolean isSelected, int colorCode) {
		
		if (actorName==null) {
			return;
		}

		final ComponentEntity sel = ModelUtils.findEntityByName(getContainer(), actorName);
		GraphicalViewer gv = getGraphicalViewer();
		final Map<?, ?> reg = gv.getEditPartRegistry();
		final AbstractGraphicalEditPart part = (AbstractGraphicalEditPart)reg.get(sel);
		if (part!=null && part.getFigure() instanceof ActorFigure) {
			ActorFigure actorFig = (ActorFigure)part.getFigure();
			actorFig.setPortColor(portName, isSelected, colorCode);
			actorFig.repaint();
		}
	}

	
	private static class SpecialLineBorder extends LineBorder {

		public SpecialLineBorder(Color selectionColor, int i) {
			super(selectionColor, i);
		}
		
	}

	public void clearActorSelections() {
		
		GraphicalViewer gv  = getGraphicalViewer();
		final Map<?, ?> reg = gv.getEditPartRegistry();
		for (Object key : reg.keySet()) {
			final Object ob = reg.get(key);
			if (ob instanceof AbstractGraphicalEditPart) {
				AbstractGraphicalEditPart part = (AbstractGraphicalEditPart)ob;
				if (part.getFigure()!=null) {
					if (part.getFigure().getBorder() instanceof SpecialLineBorder) {
						part.getFigure().setBorder(null);
					}
				}
			}
		}
	}

	public ZoomManager getZoomManager() {
		ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart)(getGraphicalViewer().getRootEditPart());
		if(root.getZoomManager()!=null) {
		  root.getZoomManager().setZoomLevels(new double[]{.5,.6,.7,.8,.9, 1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3, 4});
		}
		return root.getZoomManager();
	}

	public IFigure getWorkflowFigure() {
		return getEditor().getContents();
	}

	// protected void updateActions(List actionIds) {
	// super.updateActions(actionIds);
	// int activePage = getParent().getActivePage();
	// ActionRegistry registry = getActionRegistry();
	// if (activePage != -1) {
	// PasserelleModelEditor editor = (PasserelleModelEditor) getParent()
	// .getEditor(activePage);
	// registry = editor.getActionRegistry();
	// }
	// Iterator iter = actionIds.iterator();
	//
	// while (iter.hasNext()) {
	// IAction action = registry.getAction(iter.next());
	// if (action instanceof UpdateAction)
	// ((UpdateAction) action).update();
	// }
	// }
}
