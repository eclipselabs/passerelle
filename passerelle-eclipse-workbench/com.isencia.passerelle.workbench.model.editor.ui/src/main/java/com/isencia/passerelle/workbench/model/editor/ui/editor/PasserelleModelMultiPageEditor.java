package com.isencia.passerelle.workbench.model.editor.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;
import com.isencia.passerelle.editor.common.model.Link;
import com.isencia.passerelle.editor.common.model.LinkHolder;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.util.CollectingMomlParsingErrorHandler;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.workbench.model.editor.ui.Activator;
import com.isencia.passerelle.workbench.model.editor.ui.editpart.OutlinePartFactory;
import com.isencia.passerelle.workbench.model.editor.ui.palette.PaletteBuilder;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorPalettePage;
import com.isencia.passerelle.workbench.model.editor.ui.views.ActorTreeViewerPage;
import com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints;
import com.isencia.passerelle.workbench.model.ui.IPasserelleEditor;
import com.isencia.passerelle.workbench.model.ui.IPasserelleMultiPageEditor;
import com.isencia.passerelle.workbench.model.ui.command.RefreshCommand;
import com.isencia.passerelle.workbench.model.ui.utils.EclipseUtils;
import com.isencia.passerelle.workbench.model.utils.ModelUtils;

/**
 * An example showing how to create a multi-page editor. This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class PasserelleModelMultiPageEditor extends MultiPageEditorPart implements LinkHolder, IPasserelleMultiPageEditor, IResourceChangeListener {
  boolean dirty = false;

  @Override
  public boolean isDirty() {
    if (dirty)
      return true;
    return super.isDirty();
  }

  public static final String ID = "com.isencia.passerelle.workbench.model.editor.ui.editors.modelEditor";

  private static Logger logger = LoggerFactory.getLogger(PasserelleModelMultiPageEditor.class);

  private CompositeActor model = new CompositeActor();
  protected boolean editorSaving = false;
  private RefreshCommand RefreshCommand;
  protected OutlinePage outlinePage;
  protected ActorTreeViewerPage actorTreeViewPage;

  public ActorTreeViewerPage getActorTreeViewPage() {
    return actorTreeViewPage;
  }

  public IPasserelleEditor getSelectedPage() {
    IEditorPart ed = getEditor(getActivePage());
    if (ed instanceof IPasserelleEditor)
      return (IPasserelleEditor) ed;
    return editor;
  }

  public DefaultEditDomain getDefaultEditDomain() {
    return new DefaultEditDomain(this);
  }

  private RefreshCommand getRefreshCommand() {
    if (RefreshCommand == null) {
      return RefreshCommand = new RefreshCommand();
    }
    return RefreshCommand;
  }

  public Object getAdapter(Class type) {
    if (type == IContentOutlinePage.class) {
      outlinePage = new OutlinePage(new TreeViewer());
      return outlinePage;
    }

    if (type == ActorPalettePage.class || type == Page.class) {
      actorTreeViewPage = new ActorTreeViewerPage(editor.getActionRegistry(), null);
      return actorTreeViewPage;
    }

    return super.getAdapter(type);
  }

  private ResourceTracker resourceListener = new ResourceTracker();

  public Logger getLogger() {
    return logger;
  }

  public CompositeActor getDiagram() {
    return model;
  }

  @Override
  public void removePage(int pageIndex) {

    super.removePage(pageIndex);
    pages.remove(pageIndex);
    setActivePage(getWorkflowPageIndex());
  }

  protected List pages = new ArrayList();

  public int getPageIndex(CompositeActor actor) {

    for (int i = 0; i < pages.size(); i++) {
      final Object part = pages.get(i);
      if (!(part instanceof PasserelleModelEditor))
        continue;

      if (((PasserelleModelEditor) part).getContainer() == actor) {
        return i;
      }
    }
    return -1;
  }

  public IEditorPart getEditor(int index) {
    return (index>=0)?super.getEditor(index):null;
  }

  public int addPage(TypedCompositeActor model, IEditorPart editor, IEditorInput input) throws PartInitException {
    int index = super.addPage(editor, input);
    pages.add(editor);
    return index;
  }

  /** The text editor used in page 0 or 1 */
  private PasserelleModelEditor editor;

  public PasserelleModelEditor getEditor() {
    return editor;
  }

  /** The font chosen in page 1. */
  private Font font;

  /** The text widget used in page 2. */
  private StyledText text;
  private TextEditor textEditor;
  private boolean parseError = false;

  /**
   * Creates a multi-page editor example.
   */
  public PasserelleModelMultiPageEditor() {
    super();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
  }

  /**
   * Creates the pages of the multi-page editor.
   */
  protected void createPages() {
    try {
      createWorkflowPage(getWorkflowPageIndex());
      createXmlPage(1);

    } catch (PartInitException e) {
      logger.error("Cannot open passerelle editor " + getEditorInput().getName(), e);
    }

    getSite().getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        try {
          EclipseUtils.getActivePage().showView(ActorAttributesView.ID);
          EclipseUtils.getActivePage().activate(PasserelleModelMultiPageEditor.this);
        } catch (Throwable ignored) {
          // Nowt
        }
      }
    });
  }

  /**
   * Creates page 0 of the multi-page editor, which contains a text editor.
   * 
   * @throws PartInitException
   */
  protected void createWorkflowPage(int pageIndex) throws PartInitException {

    editor = new PasserelleModelEditor(this, model);
    addPage(editor, getEditorInput());
    editor.setIndex(pageIndex);
    setPageText(pageIndex, "Edit");
    pages.add(editor);

  }

  protected void createXmlPage(final int pageIndex) throws PartInitException {

    /**
     * Important use StructuredTextEditor and set .moml as an xml file using the eclipse content type extension point.
     */
    try {
      this.textEditor = new StructuredTextEditor() {
        @Override
        protected void setContentDescription(String description) {
          // TODO Auto-generated method stub
          super.setContentDescription(description);
        }

        @Override
        public boolean validateEditorInputState() {
          // TODO Auto-generated method stub
          return true;
        }

        @Override
        public boolean isEditable() {
          return false;
        }
      };
      addPage(pageIndex, textEditor, getEditorInput());
      setPageText(pageIndex, "XML");
      pages.add(textEditor);
    } catch (Exception e) {

    }
  }

  @Override
  protected void setContentDescription(String description) {
    // TODO Auto-generated method stub
    super.setContentDescription(description);
  }

  void removePage(PasserelleModelEditor editor) {
    try {

      removePage(editor.getIndex());
      int index = addPage(editor, getEditorInput());
      setPageText(index, editor.getTitle());
    } catch (PartInitException e) {
      ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
    }
  }

  public void setText(int idex, String text) {
    setPageText(idex, text);
  }

  public void doSave(final IProgressMonitor monitor) {

    editorSaving = true;
    dirty = false;
    if (!this.parseError) {
      SafeRunner.run(new SafeRunnable() {
        public void run() throws Exception {
          CompositeActor diagram = getDiagram();
          StringWriter writer = new StringWriter();
          diagram.exportMoML(writer);

          final IFile file = EclipseUtils.getIFile(getEditorInput());
          if (!file.exists()) {
            file.create(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), true, monitor);
          } else {
            file.setContents(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")), true, false, monitor);
          }

          if (ModelUtils.getPasserelleProject().equals(file.getProject())) {
            if (diagram.isClassDefinition()) {
              Flow flow = (Flow) diagram;
              PaletteBuilder.getInstance().addSubModel(null,null,flow.getName());
              Activator.getDefault().getRepositoryService().createSubmodel(flow);

            }
          }
        }
      });

//      getEditor(0).doSave(monitor);
      for (Object page : pages) {
        if (page instanceof PasserelleModelEditor) {
          CompositeActor actor = ((PasserelleModelEditor) page).getContainer();
          int index = getPageIndex(actor);
          if (index != -1 && index != 0) {
            IEditorPart editor = getEditor(index);
            editor.doSave(monitor);
          }
        }
      }
    }
    editorSaving = false;

  }

  /**
   * Saves the multi-page editor's document as another file. Also updates the text for page 0's tab, and updates this
   * multi-page editor's input to correspond to the nested editor's.
   */
  public void doSaveAs() {
    performSaveAs();
  }

  /*
   * (non-Javadoc) Method declared on IEditorPart
   */
  public void gotoMarker(IMarker marker) {
    setActivePage(0);
    IDE.gotoMarker(editor, marker);
  }

  /**
   * The <code>MultiPageEditorExample</code> implementation of this method checks that the input is an instance of
   * <code>IFileEditorInput</code>.
   */
  public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
    super.init(site, editorInput);
  }

  /*
   * (non-Javadoc) Method declared on IEditorPart.
   */
  public boolean isSaveAsAllowed() {
    return true;
  }

  protected boolean performSaveAs() {

    SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow().getShell());
    dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
    dialog.open();
    IPath path = dialog.getResult();

    if (path == null)
      return false;

    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IFile file = workspace.getRoot().getFile(path);

    WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
      public void execute(final IProgressMonitor monitor) {

        try {
          CompositeActor diagram = getDiagram();
          StringWriter writer = new StringWriter();
          diagram.exportMoML(writer);

          final ByteArrayInputStream contents = new ByteArrayInputStream(writer.toString().getBytes());
          if (!file.exists()) {
            file.create(contents, true, monitor);
          } else {
            file.setContents(contents, true, true, monitor);
          }

        } catch (Exception e) {
          getLogger().error("Error saving model file : " + file.getName(), e);
        }
      }
    };
    try {
      new ProgressMonitorDialog(getSite().getWorkbenchWindow().getShell()).run(false, true, op);
    } catch (Exception e) {
      getLogger().error("Error showing progress monitor during saving of model file : " + file.getName(), e);
    }

	try {
		setInput(new FileEditorInput(file));
		
		for (Object page : pages) {
			if (page instanceof IReusableEditor) {
				((IReusableEditor)page).setInput(getEditorInput());
		}
	}
    } catch (Exception e) {
      getLogger().error("Error during re-read of saved model file : " + file.getName(), e);
    }
    return true;
  }

  /**
   * Calculates the contents of page 2 when the it is activated.
   */
  protected void pageChange(int newPageIndex) {

    getRefreshCommand().setModel(getDiagram());
    getRefreshCommand().execute();
    if (outlinePage != null)
      outlinePage.switchThumbnail(newPageIndex);

    // if (newPageIndex == 1) { // Text Editor
    // doSave(new NullProgressMonitor());
    // }

    super.pageChange(newPageIndex);
  }

  /**
   * Closes all project files on project close.
   */
  public void resourceChanged(final IResourceChangeEvent event) {
    if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
          for (int i = 0; i < pages.length; i++) {
            if (((FileEditorInput) editor.getEditorInput()).getFile().getProject().equals(event.getResource())) {
              IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
              pages[i].closeEditor(editorPart, true);
            }
          }
        }
      });
    }
  }

  /**
   * Sets the font related data to be applied to the text in page 2.
   */
  void setFont() {
    FontDialog fontDialog = new FontDialog(getSite().getShell());
    fontDialog.setFontList(text.getFont().getFontData());
    FontData fontData = fontDialog.open();
    if (fontData != null) {
      if (font != null)
        font.dispose();
      font = new Font(text.getDisplay(), fontData);
      text.setFont(font);
    }
  }

  private void setDiagram(CompositeActor diagram) {
    model = diagram;
    if (editor!=null) this.editor.setDiagram(diagram);
  }

  // public void createPartControl(Composite parent) {
  // super.createPartControl(parent);
  //
  // getEditorSite().getActionBars().setGlobalActionHandler(
  // IWorkbenchActionConstants.DELETE,
  // getActionRegistry().getAction(GEFActionConstants.DELETE));
  // }
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
      if (file != null)
        file.getWorkspace().addResourceChangeListener(resourceListener);
      setPartName(input.getName());
    }
  }

  protected void setInput(IEditorInput input) {

    this.parseError = false;
    superSetInput(input);

    String filePath = EclipseUtils.getFilePath(input);
    filePath = filePath.replace("%20", " ");
    InputStream is = null;
    try {
      is = new FileInputStream(filePath);

      FileReader reader = new FileReader(new File(filePath));

      final IFile ifile = EclipseUtils.getIFile(input);

      CollectingMomlParsingErrorHandler errorHandler = new CollectingMomlParsingErrorHandler();
      MoMLParser.setErrorHandler(errorHandler);
      Flow compositeActor = FlowManager.readMoml(reader);
      if (errorHandler != null && errorHandler.hasErrors()) {
        Iterator itr = errorHandler.iterator();
        StringBuilder errorMsgBldr = new StringBuilder("Error during reading/parsing of model file");
        while (itr.hasNext()) {
          CollectingMomlParsingErrorHandler.ErrorItem errorItem = (CollectingMomlParsingErrorHandler.ErrorItem) itr.next();
          errorMsgBldr.append(errorItem.context.getName() + ":" + errorItem.exception.getMessage());
        }
        this.dirty = true;
        logger.error(errorMsgBldr.toString());
        EclipseUtils.logError(new Exception(errorMsgBldr.toString()), "Error parsing model", IStatus.ERROR);
        EclipseUtils.displayErrorDialog("Error parsing model", errorMsgBldr.toString());

      }
      compositeActor.setSource(filePath);
      compositeActor.workspace().setName(ifile.getProject().getName());

      setDiagram(compositeActor);

    } catch (Exception e) {
      this.dirty = true;
      this.parseError = true;
      logger.error("Error during reading/parsing of model file", e);
      EclipseUtils.logError(e, "Error during reading/parsing of model file", IStatus.ERROR);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // Do Nothing
        }
      }

    }

  }

  class ResourceTracker implements IResourceChangeListener, IResourceDeltaVisitor {
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

      final IFile file = EclipseUtils.getIFile(getEditorInput());
      if (delta == null || !delta.getResource().equals(file))
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
          final IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getMovedToPath());
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
          final IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(delta.getFullPath());
          Display display = getSite().getShell().getDisplay();
          display.asyncExec(new Runnable() {
            public void run() {
              setInput(new FileEditorInput(newFile));
              // getCommandStack().flush();
            }
          });
        }
      }
      return false;
    }
  }

  protected void closeEditor(boolean save) {
    getSite().getPage().closeEditor(PasserelleModelMultiPageEditor.this, save);
  }

  private boolean isDisposed = false;
  public void dispose() {

	  MoMLParser.purgeAllModelRecords();

	  getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
	  partListener = null;

	  final IFile file = EclipseUtils.getIFile(getEditorInput());
	  if (file != null) {
		  file.getWorkspace().removeResourceChangeListener(resourceListener);
	  }

	  super.dispose();
	  this.isDisposed = true;
  }

  private IPartListener partListener = new IPartListener() {
    // If an open, unsaved file was deleted, query the user to either do a
    // "Save As"
    // or close the editor.
    public void partActivated(IWorkbenchPart part) {
      if (part != PasserelleModelMultiPageEditor.this)
        return;
      if (!EclipseUtils.getFile(getEditorInput()).exists()) {

      }
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
    }

    public void partDeactivated(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }
  };

  protected void setSite(IWorkbenchPartSite site) {
    super.setSite(site);
    getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
  }

  class OutlinePage extends ContentOutlinePage implements IAdaptable {

    private PageBook pageBook;
    private Control outline;
    private Canvas overview;
    private IAction showOutlineAction, showOverviewAction;
    static final int ID_OUTLINE = 0;
    static final int ID_OVERVIEW = 1;
    private Thumbnail thumbnail;
    private DisposeListener disposeListener;
    private int index;
    private LightweightSystem lws;
    private Map<PasserelleModelEditor, Thumbnail> thumbnails = new HashMap<PasserelleModelEditor, Thumbnail>();
    private PasserelleModelEditor editor;

    public OutlinePage(EditPartViewer viewer) {
      super(viewer);
    }

    public void init(IPageSite pageSite) {
      super.init(pageSite);
      editor = PasserelleModelMultiPageEditor.this.editor;

    }

    private OutlinePartFactory factory;

    public OutlinePartFactory getFactory() {
      return factory;
    }

    protected void configureOutlineViewer() {
      getViewer().setEditDomain(editor.getEditDomain());
      factory = new OutlinePartFactory(PasserelleModelMultiPageEditor.this);
      getViewer().setEditPartFactory(factory);
      ContextMenuProvider provider = new PasserelleContextMenuProvider(getViewer(), editor.getActionRegistry());
      getViewer().setContextMenu(provider);
      getSite().registerContextMenu("com.isencia.passerelle.workbench.model.editor.ui.editor.outline.contextmenu", //$NON-NLS-1$  
          provider, getSite().getSelectionProvider());
      getViewer().setKeyHandler(editor.getCommonKeyHandler());
      IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
      showOutlineAction = new Action() {
        public void run() {
          showPage(ID_OUTLINE);
        }
      };
      showOutlineAction.setImageDescriptor(Activator.getImageDescriptor("icons/outline.gif"));
      // showOutlineAction.setToolTipText(LogicMessages.LogicEditor_outline_show_outline);
      tbm.add(showOutlineAction);
      showOverviewAction = new Action() {
        public void run() {
          showPage(ID_OVERVIEW);
        }
      };
      showOverviewAction.setImageDescriptor(Activator.getImageDescriptor("icons/overview.gif")); //$NON-NLS-1$
      // showOverviewAction.setToolTipText(LogicMessages.LogicEditor_outline_show_overview);
      tbm.add(showOverviewAction);
      showPage(ID_OVERVIEW);
    }

    public void createControl(Composite parent) {
      pageBook = new PageBook(parent, SWT.NONE);
      outline = getViewer().createControl(pageBook);
      overview = new Canvas(pageBook, SWT.NONE);
      pageBook.showPage(outline);
      configureOutlineViewer();
      hookOutlineViewer();
      initializeOutlineViewer();
      // IActionBars bars = getSite().getActionBars();
      // ActionRegistry ar = getActionRegistry();
      // bars.setGlobalActionHandler(ActionFactory.COPY.getId(), ar
      // .getAction(ActionFactory.COPY.getId()));
      // bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), ar
      // .getAction(ActionFactory.PASTE.getId()));
    }

    public void dispose() {
      unhookOutlineViewer();
      if (thumbnail != null) {
        try {
          thumbnail.deactivate();
        } catch (IllegalArgumentException e) {

        }
        thumbnail = null;
      }
      super.dispose();
      // if (getFactory() != null) {
      // for (OutlineEditPart part : getFactory().getParts()) {
      // for (Image image : part.getModelImages()) {
      // image.dispose();
      // }
      // }
      // }
      outlinePage = null;
    }

    public Object getAdapter(Class type) {
      if (type == ZoomManager.class)
        return editor.getGraphicalViewer().getProperty(ZoomManager.class.toString());
      return null;
    }

    public Control getControl() {
      return pageBook;
    }

    protected void hookOutlineViewer() {
      editor.getSelectionSynchronizer().addViewer(getViewer());
    }

    protected void initializeOutlineViewer() {
      setContents(getDiagram());
    }

    public void switchThumbnail(int newPageIndex) {
      index = getActivePage();

      IEditorPart newEditor = getEditor(newPageIndex);
      if (newEditor instanceof PasserelleModelEditor)
        editor = (PasserelleModelEditor) newEditor;
      thumbnail = thumbnails.get(editor);

      if (editor == null)
        return;
      GraphicalViewer viewer = editor.getGraphicalViewer();
      if (lws == null) {
        lws = new LightweightSystem(overview);
      }
      if (thumbnail != null) {
        thumbnail.setVisible(true);
        lws.setContents(thumbnail);
      } else {

        thumbnail = createThumbnail(lws, viewer);
        thumbnails.put(editor, thumbnail);
      }

    }

    protected void initializeOverview() {
      editor = PasserelleModelMultiPageEditor.this.editor;

      thumbnail = thumbnails.get(editor);
      lws = new LightweightSystem(overview);

      GraphicalViewer viewer = editor.getGraphicalViewer();
      thumbnail = createThumbnail(lws, viewer);
      thumbnails.put(editor, thumbnail);

    }

    private Thumbnail createThumbnail(LightweightSystem lws, GraphicalViewer viewer) {
      if (editor != null)
        viewer = editor.getGraphicalViewer();
      RootEditPart rep = viewer.getRootEditPart();

      if (rep instanceof ScalableFreeformRootEditPart) {
        ScalableFreeformRootEditPart root = (ScalableFreeformRootEditPart) rep;
        thumbnail = new ScrollableThumbnail((Viewport) root.getFigure());
        thumbnail.setBorder(new MarginBorder(3));
        thumbnail.setSource(root.getLayer(LayerConstants.PRINTABLE_LAYERS));
        lws.setContents(thumbnail);

        disposeListener = new DisposeListener() {
          public void widgetDisposed(DisposeEvent e) {
            if (thumbnail != null) {
              thumbnail.deactivate();
              thumbnail = null;
            }
          }
        };
        editor.getEditor().addDisposeListener(disposeListener);

        return thumbnail;
      }
      return null;
    }

    public void setContents(Object contents) {
      getViewer().setContents(contents);
    }

    protected void showPage(int id) {
      if (id == ID_OUTLINE) {
        showOutlineAction.setChecked(true);
        showOverviewAction.setChecked(false);
        pageBook.showPage(outline);
        if (thumbnail != null)
          thumbnail.setVisible(false);
      } else if (id == ID_OVERVIEW) {
        if (thumbnail == null)
          initializeOverview();
        showOutlineAction.setChecked(false);
        showOverviewAction.setChecked(true);
        pageBook.showPage(overview);
        thumbnail.setVisible(true);
      }
    }

    protected void unhookOutlineViewer() {
      editor.getSelectionSynchronizer().removeViewer(getViewer());
      if (disposeListener != null && editor.getEditor() != null && !editor.getEditor().isDisposed())
        editor.getEditor().removeDisposeListener(disposeListener);
    }
  }

  // get the container of the page where the user is on this moment
  public CompositeActor getSelectedContainer() {
    IPasserelleEditor editor = getSelectedPage();
    if (editor != null && editor.getContainer() != null) {
      return editor.getContainer();
    }
    return getDiagram();
  }

  public void selectPage(CompositeActor actor) {
    int index = getPageIndex(actor);
    if (index != -1)
      setActivePage(index);
    else
      setActivePage(0);
  }

  public void refreshActions() {
    getEditorSite().getActionBars().getToolBarManager().update(true);
  }

  /**
   * Selects an actor in the editor by highlighting it.
   * 
   * @param actorName
   * @param isSelected
   * @param colorCode
   *          - one of the SWT color codes.
   */
  public void setActorSelected(String actorName, boolean isSelected, final int colorCode) {
    editor.setActorSelected(actorName, isSelected, colorCode);
  }

  public void setPortSelected(String actorName, String portName, boolean selected, int colorCode) {
	  editor.setPortSelected(actorName, portName, selected, colorCode);
  }

  public void clearActorSelections() {
    editor.clearActorSelections();
  }

  public CompositeActor getModel() {
    return model;
  }

  public void setModel(CompositeActor model) {
    this.model = model;
  }

  public boolean isParseError() {
    return parseError;
  }

  /**
   * Required by unit tests in dawb.
   */
  public void setActivePage(final int ipage) {
    super.setActivePage(ipage);
  }

  public void setPasserelleEditorActive() {
    setActivePage(0);
  }

  protected int getWorkflowPageIndex() {
    return 0;
  }

  Map<Object, Set<Link>> linkMap;
  Set<Link> links = new HashSet<Link>();

  public Set<Link> getLinks(Object o) {

    Set<Link> list = linkMap.get(o);
    if (list == null) {
      return Collections.EMPTY_SET;

    }
    return list;
  }

  public void registerLink(Link link) {
    Set<Link> links = linkMap.get(link.getHead());
    if (links == null) {
      links = new HashSet<Link>();
      linkMap.put(link.getHead(), links);
    }
    links.add(link);
    links = linkMap.get(link.getTail());
    if (links == null) {
      links = new HashSet<Link>();
      linkMap.put(link.getTail(), links);
    }
    links.add(link);
  }

  public void generateCompositeLinks(CompositeActor modelDiagram) {
    List<NamedObj> relations = modelDiagram.relationList();
    for (NamedObj modelObject : relations) {
      TypedIORelation relation = (TypedIORelation) modelObject;

      if (relation.getContainer() != null) {
        if (modelObject.attributeList(Vertex.class).isEmpty()) {
          if (!relation.linkedSourcePortList().isEmpty() && !relation.linkedDestinationPortList().isEmpty()) {
            generateLink(relation, relation.linkedSourcePortList().get(0), relation.linkedDestinationPortList().get(0));
          } else {
            try {
              relation.setContainer(null);
            } catch (Exception e) {
            }
          }

        } else {
          NamedObj vertex = (NamedObj) modelObject.attributeList(Vertex.class).get(0);
          List list = relation.linkedObjectsList();

          for (Object o : list) {
            LinkWithBendPoints generateLinkWithBendPoints = null;
            if (o instanceof TypedIORelation) {

              TypedIORelation o2 = (TypedIORelation) o;
              if (!o2.attributeList(Vertex.class).isEmpty()) {
                NamedObj otherVertex = (NamedObj) o2.attributeList(Vertex.class).get(0);
                if (otherVertex != null) {
                  double[] otherVertexLocation = ModelUtils.getLocation(otherVertex);
                  double[] vertexLocation = ModelUtils.getLocation(vertex);
                  if (vertexLocation[0] < otherVertexLocation[0]) {
                    generateLinkWithBendPoints = generateLink(relation, otherVertex, vertex);
                  } else {
                    generateLinkWithBendPoints = generateLink(relation, vertex, otherVertex);
                  }

                }
              }
            } else {
              if (relation.linkedSourcePortList().contains(o)) {
                generateLinkWithBendPoints = generateLink(relation, (NamedObj) o, vertex);
              } else {
                generateLinkWithBendPoints = generateLink(relation, vertex, (NamedObj) o);
              }
            }

          }

        }
      }
    }
  }
  public void generateLinks(CompositeActor modelDiagram) {
    linkMap = new HashMap<Object, Set<Link>>();
    if (links == null)
      links = new HashSet<Link>();

    Flow flow = (Flow) modelDiagram.toplevel();
    generateCompositeLinks(flow);
    for (Object entity : flow.entityList(CompositeActor.class)){
      CompositeActor compositeEntity = (CompositeActor)entity;
      generateCompositeLinks(compositeEntity);
    }
    
  }

  public void removeLink(Link link) {
    links.remove(link);
    Set<Link> headLinks = linkMap.get(link.getHead());
    if (headLinks != null) {
      headLinks.remove(link);
    }
    Set<Link> tailLinks = linkMap.get(link.getTail());
    if (tailLinks != null) {
      tailLinks.remove(link);
    }
  }

  public LinkWithBendPoints generateLink(ComponentRelation relation, Object source, Object target) {
    LinkWithBendPoints link = generate(relation, source, target);
    if (!links.contains(link)) {
      registerLink(link);
      links.add(link);
    } else {
      for (Link l : links) {
        if (l.equals(link)) {
          registerLink(l);
          return (LinkWithBendPoints) l;
        }
      }
    }
    return link;
  }

  public LinkWithBendPoints generate(ComponentRelation relation, Object source, Object target) {
    LinkWithBendPoints link = new LinkWithBendPoints();
    link.setHead(source);
    link.setTail(target);
    link.setRelation(relation);
    return link;
  }

  public boolean isDisposed() {
	  return isDisposed;
  }

}
