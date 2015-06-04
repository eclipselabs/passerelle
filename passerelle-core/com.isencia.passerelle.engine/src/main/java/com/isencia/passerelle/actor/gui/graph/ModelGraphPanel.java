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
package com.isencia.passerelle.actor.gui.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.AbstractBasicGraphModel;
import ptolemy.vergil.tree.EntityTreeModel;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.PTreeMenuCreator;
import ptolemy.vergil.tree.VisibleTreeModel;

import com.isencia.passerelle.actor.gui.GetDocumentationAction;
import com.isencia.passerelle.actor.gui.LibraryManager;
import com.isencia.passerelle.actor.gui.graph.EditorGraphController.ViewFactory;
import com.isencia.passerelle.actor.gui.graph.userlib.AddUserLibraryFolderMenuItemFactory;
import com.isencia.passerelle.actor.gui.graph.userlib.DeleteFromLibraryMenuItemFactory;
import com.isencia.passerelle.actor.gui.graph.userlib.ImportClassInLibraryMenuItemFactory;
import com.isencia.passerelle.actor.gui.graph.userlib.RenameLibraryMenuItemFactory;
import com.isencia.passerelle.core.ControlPort;
import com.isencia.passerelle.core.ErrorPort;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.util.EnvironmentUtils;
import com.isencia.passerelle.validation.version.ActorVersionRegistry;

import diva.canvas.CanvasComponent;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.TerminalFigure;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.GraphController;
import diva.graph.GraphEvent;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;
import diva.graph.NodeRenderer;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JCanvasPanner;
import diva.util.java2d.Polygon2D;
import diva.util.java2d.ShapeUtilities;

/**
 * This class represents a merger of about 5 ptolemy gui classes:
 * <ul>
 * <li>ptolemy.gui.Top
 * <li>ptolemy.actor.gui.TableauFrame
 * <li>ptolemy.actor.gui.PtolemyFrame
 * <li>ptolemy.vergil.basic.BasicGraphframe
 * <li>ptolemy.vergil.basic.ExtendedGraphFrame
 * <li>ptolemy.vergil.actor.ActorGraphFrame
 * </ul>
 * 
 * In ptolemy these are used to define a JFrame containing a complete model
 * graph editor, including menus and toolbar.
 * 
 * For Passerelle, we don't want separate frames per model, and we don't want
 * the menu per model. But we're keeping the toolbar.
 * 
 * The resulting panel will be embedded in a IDE view component. In Netbeans,
 * this is our own com.isencia.passerelle.ide.graphedit.ModelGraphTopComponent.
 * 
 * This panel is constructed by our customized ActorGraphTableau. In order to
 * use this, one needs to define the correct tableau factory in the
 * Passerelle/Ptolemy config files, namely:<br>
 * <code>
 * &lt;property name="factory2" class="ptolemy.actor.gui.PtolemyTableauFactory"/><br>
 * &lt;property name="Graph Editor"
 *           class="com.isencia.passerelle.actor.gui.graph.ActorGraphTableau$Factory"/><br>
 * ...
 * </code>
 * 
 * @author erwin
 */
public class ModelGraphPanel extends JPanel implements ClipboardOwner, ChangeListener, Printable {

	final static Logger logger = LoggerFactory.getLogger(ModelGraphPanel.class);

	private NamedObj model = null;
	private Effigy effigy = null;
	private EditorGraphController controller = null;

	// Copied from ptolemy.vergil.basic.BasicGraphFrame
	/** The cut action. */
	protected Action _cutAction;
	/** The copy action. */
	protected Action _copyAction;
	/** The paste action. */
	protected Action _pasteAction;
	/** extras to complete edit actions **/
	private UndoManager undoManager = new UndoManager();
	protected Action _undoAction;
	protected Action _redoAction;
	protected Action _selectAllAction;

	/** Undo/Redo support */
	UndoableEditSupport undoableEditSupport = new UndoableEditSupport(this);

	/** Action for zooming in. */
	private Action _zoomInAction = new ZoomInAction("Zoom In");

	/** Action for zoom reset. */
	private Action _zoomResetAction = new ZoomResetAction("Zoom Reset");

	/** Action for zoom fitting. */
	private Action _zoomFitAction = new ZoomFitAction("Zoom Fit");

	/** Action for zooming out. */
	private Action _zoomOutAction = new ZoomOutAction("Zoom Out");

	/** The instance of EditorDropTarget associated with this object. */
	protected EditorDropTarget _dropTarget;

	/** The panner. */
	protected JCanvasPanner _graphPanner;

	/** The instance of JGraph for this editor. */
	protected JGraph _jgraph;
	/** Default background color is a light grey. */
	protected static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

	/** The library display widget. */
	protected JTree _library;
	/** The library context menu creator. */
	protected PTreeMenuCreator _libraryContextMenuCreator;
	/** The library model. */
	protected EntityTreeModel _libraryModel;
	/** The library scroll pane. */
	protected JScrollPane _libraryScrollPane;
	/** The library display panel. */
	protected JPanel _palettePane;

	/** The split pane for library and editor. */
	protected JSplitPane _splitPane;

	/** The toolbar. */
	protected JToolBar _toolbar;

	/** The library. */
	protected CompositeEntity _topLibrary;
	// End-of-Copied

	// Passerelle-specific
	LibraryManager libraryManager;
	EntityLibrary usrLibrary;

	/**
	 * 
	 */
	public ModelGraphPanel(NamedObj model, PtolemyEffigy effigy) {
		super();
		this.model = model;
		this.effigy = effigy;
		this.model.setModelErrorHandler(new BasicModelErrorHandler());
		List attrList = this.model.attributeList(UndoStackAttribute.class);
		if (attrList.size() == 0) {
			// Create and attach a new instance
			try {
				new UndoStackAttribute(this.model, "_undoInfo");
			} catch (KernelException e) {
				throw new InternalErrorException(e);
			}
		}

		libraryManager = new LibraryManager(getConfiguration());

		// Code copied from ptolemy.gui.Top
		setLayout(new BorderLayout());

		// Code copied from ptolemy.vergil.basic.BasicGraphFrame
		model.addChangeListener(this);

		initGUI();
	}

	public void setEnabled(boolean b) {
		_jgraph.getGraphPane().getForegroundEventLayer().setEnabled(b);
		_jgraph.getGraphPane().getForegroundLayer().setEnabled(b);
		_jgraph.getGraphPane().getBackgroundEventLayer().setEnabled(b);
		Component[] toolbarButtons = _toolbar.getComponents();
		for (Component toolbarButton : toolbarButtons) {
			toolbarButton.setEnabled(b);
		}
		_dropTarget.setActive(b);

	}
	
	/**
	 * This method contains the largest part of the constructor of
	 * ptolemy.vergil.basic.BasicGraphFrame, where all UI components are
	 * assembled into a 3-pane graph editor:
	 * <ul>
	 * <li>the model graph itself
	 * <li>a tree view of the actor libraries
	 * <li>a panner view to select the view port on large models
	 * </ul>
	 * 
	 */
	private void initGUI() {
		GraphPane pane = _createGraphPane();
		pane.getForegroundLayer().setPickHalo(2);

		_jgraph = new JGraph(pane);
		// Make this the default context for modal messages.
		GraphicalMessageHandler.setContext(getDialogHookComponent());
		MessageHandler.setMessageHandler(new GraphicalMessageHandler());

		_dropTarget = new EditorDropTarget(_jgraph, this);

		ActionListener deletionListener = new ActionListener() {
			/**
			 * Delete any nodes or edges from the graph that are currently
			 * selected. In addition, delete any edges that are connected to any
			 * deleted nodes.
			 */
			public void actionPerformed(ActionEvent e) {
				delete();
			}
		};

		PanMouseListener pmListener = new PanMouseListener();
		_jgraph.addMouseListener(pmListener);
		_jgraph.addMouseMotionListener(pmListener);
		_jgraph.addMouseWheelListener(new MouseWheelListener() {
			
			public void mouseWheelMoved(MouseWheelEvent e) {
				int dir=e.getWheelRotation();
				if(dir>0)
					zoom(0.9);
				else
					zoom(1.1);
			}
		});
		
		_jgraph.registerKeyboardAction(deletionListener, "Delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		_jgraph.registerKeyboardAction(deletionListener, "BackSpace", KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		ShowDocumentationAction showDocAction = new ShowDocumentationAction();
		showDocAction.setConfiguration(getConfiguration());
		_jgraph.registerKeyboardAction(showDocAction, "Ctrl+D", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

		_jgraph.setRequestFocusEnabled(true);
		pane.getForegroundEventLayer().setConsuming(false);
		pane.getForegroundEventLayer().setEnabled(true);
		pane.getForegroundEventLayer().addLayerListener(new LayerAdapter() {
			/**
			 * Invoked when the mouse is pressed on a layer or figure.
			 */
			public void mousePressed(LayerEvent event) {
				Component component = event.getComponent();
				if (!component.hasFocus()) {
					component.requestFocus();
				}
			}
		});

		// We used to do this, but it would result in context menus
		// getting lost on the mac.

		// _jgraph.addMouseListener(new FocusMouseListener());
		_jgraph.setAlignmentX(1);
		_jgraph.setAlignmentY(1);
		// _jgraph.setBackground(BACKGROUND_COLOR);

		try {
			// The SizeAttribute property is used to specify the size
			// of the JGraph component. Unfortunately, with Swing's
			// mysterious and undocumented handling of component sizes,
			// there appears to be no way to control the size of the
			// JGraph from the size of the Frame, which is specified
			// by the WindowPropertiesAttribute.
			SizeAttribute size = (SizeAttribute) getModel().getAttribute("_vergilSize", SizeAttribute.class);
			if (size != null) {
				size.setSize(_jgraph);
			} else {
				// Set the default size.
				// Note that the location is of the frame, while the size
				// is of the scrollpane.
				_jgraph.setMinimumSize(new Dimension(200, 200));
				_jgraph.setPreferredSize(new Dimension(600, 400));
				_jgraph.setSize(600, 400);
			}

			// Set the zoom factor.
			Parameter zoom = (Parameter) getModel().getAttribute("_vergilZoomFactor", Parameter.class);
			if (zoom != null) {
				zoom(((DoubleToken) zoom.getToken()).doubleValue());
				// Make sure the visibility is only expert.
				zoom.setVisibility(Settable.EXPERT);
			}

			// Set the pan position.
			Parameter pan = (Parameter) getModel().getAttribute("_vergilCenter", Parameter.class);
			if (pan != null) {
				ArrayToken panToken = (ArrayToken) pan.getToken();
				Point2D center = new Point2D.Double(((DoubleToken) panToken.getElement(0)).doubleValue(), ((DoubleToken) panToken.getElement(1)).doubleValue());
				setCenter(center);
				// Make sure the visibility is only expert.
				pan.setVisibility(Settable.EXPERT);
			}

		} catch (Exception ex) {
			// Ignore problems here. Errors simply result in a default
			// size and location.
		}

		// Create the panner.
		_graphPanner = new JCanvasPanner(_jgraph);
		_graphPanner.setPreferredSize(new Dimension(200, 150));
		_graphPanner.setMaximumSize(new Dimension(200, 150));
		_graphPanner.setSize(200, 150);
		// NOTE: Border causes all kinds of problems!
		// _graphPanner.setBorder(BorderFactory.createEtchedBorder());

		// Create the library of actors, or use the one in the entity,
		// if there is one.
		// FIXME: How do we make changes to the library persistent?
		boolean gotLibrary = false;
		try {
			URL userLibURL = EnvironmentUtils.getUserLibraryURL();
			usrLibrary = openLibrary(getConfiguration(), userLibURL);
			LibraryAttribute libraryAttribute = (LibraryAttribute) getModel().getAttribute("_library", LibraryAttribute.class);
			if (libraryAttribute != null) {
				// The model contains a library.
				_topLibrary = libraryAttribute.getLibrary();
				gotLibrary = true;
			}
		} catch (Exception ex) {
			try {
				MessageHandler.warning("Invalid library in the model.", ex);
			} catch (CancelException e) {
			}
		}
		if (!gotLibrary) {
			// Neither the model nor the argument have specified a library.
			// See if there is a default library in the configuration.
			_topLibrary = _createDefaultLibrary(getModel().workspace());
		}
		// We take the actor library as the source for actor versions as they
		// are available in the current "design tool/environment", 
		// for which this ModelGraphPanel is being constructed.
		// If version validation is "switched on", Passerelle's Swing HMI will
		// validate actor versions when opening existing/old models.
		ActorVersionRegistry.getInstance().registerActorVersionsFromLibrary(_topLibrary);
		
		_libraryModel = new VisibleTreeModel(_topLibrary);
		_library = new PTree(_libraryModel);
		_library.setRootVisible(false);
		_library.setBackground(BACKGROUND_COLOR);

		// If you want to expand the top-level libraries, uncomment this.
		/*
		 * Object[] path = new Object[2]; path[0] = topLibrary; Iterator
		 * libraries = topLibrary.entityList().iterator(); while
		 * (libraries.hasNext()) { path[1] = libraries.next();
		 * _library.expandPath(new TreePath(path)); }
		 */

		_libraryContextMenuCreator = new PTreeMenuCreator();
		_libraryContextMenuCreator.addMenuItemFactory(new RenameLibraryMenuItemFactory(this));
		_libraryContextMenuCreator.addMenuItemFactory(new AddUserLibraryFolderMenuItemFactory(this));
		_libraryContextMenuCreator.addMenuItemFactory(new DeleteFromLibraryMenuItemFactory(this));
		_libraryContextMenuCreator.addMenuItemFactory(new ImportClassInLibraryMenuItemFactory(this));
		_library.addMouseListener(_libraryContextMenuCreator);

		_libraryScrollPane = new JScrollPane(_library);
		_libraryScrollPane.setMinimumSize(new Dimension(200, 200));
		_libraryScrollPane.setPreferredSize(new Dimension(200, 200));

		// create the palette on the left.
		_palettePane = new JPanel();
		_palettePane.setBorder(null);
		_palettePane.setLayout(new BoxLayout(_palettePane, BoxLayout.Y_AXIS));

		_palettePane.add(_libraryScrollPane, BorderLayout.CENTER);
		_palettePane.add(_graphPanner, BorderLayout.SOUTH);

		_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		_splitPane.setLeftComponent(_palettePane);
		_splitPane.setRightComponent(_jgraph);
		add(_splitPane, BorderLayout.CENTER);

		addUndoableEditListener(undoManager);
		_undoAction = new UndoAction();
		_redoAction = new RedoAction();
		_copyAction = new CopyAction();
		_pasteAction = new PasteAction();
		_cutAction = new CutAction();
		_selectAllAction = new SelectAllAction();

		_jgraph.registerKeyboardAction(_undoAction, "Undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
		_jgraph.registerKeyboardAction(_redoAction, "Redo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
		_jgraph.registerKeyboardAction(_copyAction, "Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
						JComponent.WHEN_IN_FOCUSED_WINDOW);
		_jgraph.registerKeyboardAction(_pasteAction, "Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		_jgraph.registerKeyboardAction(_cutAction, "Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
		_jgraph.registerKeyboardAction(_selectAllAction, "Ctrl+A", KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		_toolbar = new JToolBar();
		add(_toolbar, BorderLayout.NORTH);

		GUIUtilities.addToolBarButton(_toolbar, _zoomInAction);
		GUIUtilities.addToolBarButton(_toolbar, _zoomResetAction);
		GUIUtilities.addToolBarButton(_toolbar, _zoomFitAction);
		GUIUtilities.addToolBarButton(_toolbar, _zoomOutAction);
		_toolbar.addSeparator();
		GUIUtilities.addToolBarButton(_toolbar, _copyAction);
		GUIUtilities.addToolBarButton(_toolbar, _cutAction);
		GUIUtilities.addToolBarButton(_toolbar, _pasteAction);
		_toolbar.addSeparator();
		GUIUtilities.addToolBarButton(_toolbar, _undoAction);
		GUIUtilities.addToolBarButton(_toolbar, _redoAction);
		_toolbar.addSeparator();
		GUIUtilities.addToolBarButton(_toolbar, _selectAllAction);
		controller.addToMenuAndToolbar(null, _toolbar);
	}

	/**
	 * Create a new graph pane. Subclasses will override this to change the pane
	 * that is created. Note that this method is called in constructor, so
	 * derived classes must be careful to not reference local variables that may
	 * not have yet been created.
	 * 
	 * @return The pane that is created.
	 */
	protected GraphPane _createGraphPane() {
		logger.debug("create graph pane");
		// create the graph editor
		// These two things control the view of a ptolemy model.
		controller = new EditorGraphController(this);
		controller.setConfiguration(getConfiguration());
		controller.getEntityPortController().setNodeRenderer(new EntityPortRenderer());

		NamedObj passerelleModel = getModel();

		ActorGraphModel graphModel = new ActorGraphModel(passerelleModel);

		GraphPane pane = new GraphPane(controller, graphModel);
		return pane;
	}

	public Frame getDialogHookComponent() {
		if(_jgraph==null) return null;
		
		final JFrame parentFrame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, _jgraph);
		return parentFrame;
	}

	public void setAnimationDelay(long msDelay) {
		controller.setAnimationDelay(msDelay);
	}

	public void event(DebugEvent event) {
		controller.event(event);
	}

	/**
	 * @return Returns the model.
	 */
	public NamedObj getModel() {
		return model;
	}

	/**
	 * @return Returns the effigy.
	 */
	public Effigy getEffigy() {
		return effigy;
	}

	/**
	 * Get the configuration at the top level of the hierarchy.
	 * 
	 * @return The configuration controlling this frame, or null if there isn't
	 *         one.
	 */
	public Configuration getConfiguration() {
		NamedObj toplevel = getEffigy().toplevel();
		if (toplevel instanceof Configuration) {
			return (Configuration) toplevel;
		} else {
			logger.warn("No configuration found as top level of model");
			return null;
		}
	}

	/**
	 * Facade method. The IDE layer has access to this panel implementation, but
	 * should not be bothered with implementation details, such as the presence
	 * of controllers etc...
	 * 
	 * @param viewFactory
	 */
	public void registerViewFactory(EditorGraphController.ViewFactory viewFactory) {
		controller.registerViewFactory(viewFactory);
	}

	public ViewFactory getViewFactory() {
		return controller.getViewFactory();
	}

	/**
	 * Do nothing.
	 */
	public void lostOwnership(Clipboard clipboard, Transferable transferable) {
	}

	/**
	 * React to the fact that a change has been successfully executed. In
	 * Ptolemy, the frame is responsible for showing a dialog, when closing the
	 * window, asking the user to save modifications or not.
	 * 
	 * Here, we just need to ensure that the panner view is also updated.
	 * 
	 * @param change
	 *            The change that has been executed.
	 */
	public void changeExecuted(ChangeRequest change) {
		if (_graphPanner != null) {
			_graphPanner.repaint();
		}
		if (model instanceof EntityLibrary) {
			if (change instanceof MoMLChangeRequest || change instanceof UndoChangeRequest || change instanceof RedoChangeRequest) {
				// chances are that the library was changed
				libraryManager.saveChangedEntityLibrary((EntityLibrary) model);
			}
		}
	}

	/**
	 * React to the fact that a change has triggered an error by doing nothing
	 * (the effigy is also listening and will report the error).
	 * 
	 * @param change
	 *            The change that was attempted.
	 * @param exception
	 *            The exception that resulted.
	 */
	public void changeFailed(ChangeRequest change, Exception exception) {
		// Do not report if it has already been reported.
		if (change == null) {
			logger.error("Change failed", exception);
		} else if (!change.isErrorReported()) {
			change.setErrorReported(true);
			logger.error("Change failed", exception);
		}
	}

	/**
	 * Get the currently selected objects from this document, if any, and place
	 * them on the clipboard in MoML format.
	 */
	public void copy() {
		Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		GraphPane graphPane = _jgraph.getGraphPane();
		GraphController controller = (GraphController) graphPane.getGraphController();
		SelectionModel model = controller.getSelectionModel();
		GraphModel graphModel = controller.getGraphModel();
		Object selection[] = model.getSelectionAsArray();
		// A set, because some objects may represent the same
		// ptolemy object.
		HashSet namedObjSet = new HashSet();
		HashSet nodeSet = new HashSet();
		// First get all the nodes.
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] instanceof Figure) {
				Object userObject = ((Figure) selection[i]).getUserObject();
				if (graphModel.isNode(userObject)) {
					nodeSet.add(userObject);
					NamedObj actual = (NamedObj) graphModel.getSemanticObject(userObject);
					namedObjSet.add(actual);
				}
			}
		}
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] instanceof Figure) {
				Object userObject = ((Figure) selection[i]).getUserObject();
				if (graphModel.isEdge(userObject)) {
					// Check to see if the head and tail are both being
					// copied. Only if so, do we actually take the edge.
					Object head = graphModel.getHead(userObject);
					Object tail = graphModel.getTail(userObject);
					boolean headOK = nodeSet.contains(head);
					boolean tailOK = nodeSet.contains(tail);
					Iterator objects = nodeSet.iterator();
					while (!(headOK && tailOK) && objects.hasNext()) {
						Object object = objects.next();
						if (!headOK && GraphUtilities.isContainedNode(head, object, graphModel)) {
							headOK = true;
						}
						if (!tailOK && GraphUtilities.isContainedNode(tail, object, graphModel)) {
							tailOK = true;
						}
					}
					if (headOK && tailOK) {
						NamedObj actual = (NamedObj) graphModel.getSemanticObject(userObject);
						namedObjSet.add(actual);
					}
				}
			}
		}
		StringWriter buffer = new StringWriter();
		try {
			Iterator elements = namedObjSet.iterator();
			while (elements.hasNext()) {
				NamedObj element = (NamedObj) elements.next();
				// first level to avoid obnoxiousness with
				// toplevel translations.
				element.exportMoML(buffer, 0);
			}
			NamedObj container = (NamedObj) graphModel.getRoot();
			if (container instanceof CompositeEntity) {
				buffer.write(((CompositeEntity) container).exportLinks(1, namedObjSet));
			}

			// The code below does not use a PtolemyTransferable,
			// to work around
			// a bug in the JDK that should be fixed as of jdk1.3.1. The bug
			// is that cut and paste through the system clipboard to native
			// applications doesn't work unless you use string selection.
			clipboard.setContents(new StringSelection(buffer.toString()), this);
		} catch (Exception ex) {
			MessageHandler.error("Copy failed", ex);
		}

	}

	/**
	 * Remove the currently selected objects from this document, if any, and
	 * place them on the clipboard.
	 */
	public void cut() {
		copy();
		delete();
	}

	/**
	 * Delete the currently selected objects from this document.
	 */
	public void delete() {
		postUndoableEdit("delete");

		// Note that we previously a delete was handled at the model level.
		// Now a delete is handled by generating MoML to carry out the delete
		// and handing that MoML to the parser

		GraphPane graphPane = _jgraph.getGraphPane();
		GraphController controller = (GraphController) graphPane.getGraphController();
		SelectionModel model = controller.getSelectionModel();

		AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) controller.getGraphModel();
		Object selection[] = model.getSelectionAsArray();

		// First collect selected objects into the userObjects array
		// and deselect them.
		Object userObjects[] = new Object[selection.length];
		for (int i = 0; i < selection.length; i++) {
			userObjects[i] = ((Figure) selection[i]).getUserObject();
			model.removeSelection(selection[i]);
		}

		// Create a set to hold those elements whose deletion
		// does not go through MoML. This is only links that
		// are not connected to another port or a relation.
		HashSet edgeSet = new HashSet();

		// Generate the MoML to carry out the deletion
		StringBuffer moml = new StringBuffer("<group>\n");

		// Delete edges then nodes, since deleting relations may
		// result in deleting links to that relation.
		for (int i = 0; i < selection.length; i++) {
			Object userObject = userObjects[i];
			if (graphModel.isEdge(userObject)) {
				NamedObj actual = (NamedObj) graphModel.getSemanticObject(userObject);
				// If there is no semantic object, then this edge is
				// not fully connected, so we can't go through MoML.
				if (actual == null) {
					edgeSet.add(userObject);
				} else {
					moml.append(graphModel.getDeleteEdgeMoML(userObject));
				}
			}
		}
		for (int i = 0; i < selection.length; i++) {
			Object userObject = userObjects[i];
			if (graphModel.isNode(userObject)) {
				moml.append(graphModel.getDeleteNodeMoML(userObject));
			}
		}

		moml.append("</group>\n");

		// Have both MoML to perform deletion and set of objects whose
		// deletion does not go through MoML. This set of objects
		// should be very small and so far consists of only links that are not
		// connected to a relation
		try {
			// First manually delete any objects whose deletion does not go
			// through MoML and so are not undoable
			// Note that we turn off event dispatching so that each individual
			// removal does not trigger graph redrawing.
			graphModel.setDispatchEnabled(false);
			Iterator edges = edgeSet.iterator();
			while (edges.hasNext()) {
				Object nextEdge = edges.next();
				if (graphModel.isEdge(nextEdge)) {
					graphModel.disconnectEdge(this, nextEdge);
				}
			}
		} finally {
			graphModel.setDispatchEnabled(true);
		}

		// Next process the deletion MoML. This should be the large majority
		// of most deletions.
		try {
			// Finally create and request the change
			NamedObj container = graphModel.getPtolemyModel();
			MoMLChangeRequest change = new MoMLChangeRequest(this, container, moml.toString());
			change.setUndoable(true);
			container.requestChange(change);
		} catch (Exception ex) {
			MessageHandler.error("Delete failed, changeRequest was:" + moml, ex);
		}
		graphModel.dispatchGraphEvent(new GraphEvent(this, GraphEvent.STRUCTURE_CHANGED, graphModel.getRoot()));

	}

	/**
	 * Assuming the contents of the clipboard is MoML code, paste it into the
	 * current model by issuing a change request.
	 */
	public void paste() {
		// EDL : this builds the link to generic Swing undo/redo provisions
		// and will also get Netbeans undo/redo working...
		postUndoableEdit("paste");

		Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = clipboard.getContents(this);
		GraphPane graphPane = _jgraph.getGraphPane();
		GraphController controller = (GraphController) graphPane.getGraphController();
		GraphModel model = controller.getGraphModel();
		if (transferable == null)
			return;
		try {
			NamedObj container = (NamedObj) model.getRoot();
			StringBuffer moml = new StringBuffer();
			// The pasted version will have the names generated by the
			// uniqueName() method of the container, to ensure that they
			// do not collide with objects already in the container.
			String pastedSegment = (String) transferable.getTransferData(DataFlavor.stringFlavor);
			// stupid method to slightly move the pasted elements, compared to their "originals"
			pastedSegment = translateFiguresInSegment(pastedSegment,20,20);
			moml.append("<group name=\"auto\">\n");
			moml.append(pastedSegment);
			moml.append("</group>\n");

			MoMLChangeRequest change = new MoMLChangeRequest(this, container, moml.toString());
			change.setUndoable(true);
			container.requestChange(change);
		} catch (Exception ex) {
			MessageHandler.error("Paste failed", ex);
		}
	}

	private String translateFiguresInSegment(String momlSegment, int... increments) {
		String movedSegment = momlSegment;
		int i=0;
		while(i>=0) {
			i = movedSegment.indexOf("_location", i);
			if(i>=0) {
				int jRoundBrace = movedSegment.indexOf("{", i);
				int jSquareBracket = movedSegment.indexOf("[", i);
				if(jRoundBrace<0 && jSquareBracket<0) {
					break;
				}
				if(jRoundBrace<0)
					i = jSquareBracket;
				else if(jSquareBracket<0)
					i = jRoundBrace;
				else
					i = Math.min(jRoundBrace, jSquareBracket);
				jRoundBrace = movedSegment.indexOf("}",i);
				jSquareBracket = movedSegment.indexOf("]",i);
				if(jRoundBrace<0 && jSquareBracket<0) {
					break;
				}
				int j = 0;
				if(jRoundBrace<0)
					j = jSquareBracket;
				else if(jSquareBracket<0)
					j = jRoundBrace;
				else
					j = Math.min(jRoundBrace, jSquareBracket);
				String locationSegment = movedSegment.substring(i+1,j);
				String[] locationCoordinates = locationSegment.split(",");
				int[] newCoordinates = new int[locationCoordinates.length];
				for (int c=0;c<locationCoordinates.length;++c) {
					String locationCoordinate = locationCoordinates[c].trim();
					newCoordinates[c] = Math.round(Float.parseFloat(locationCoordinate))+increments[c];
				}
				movedSegment=movedSegment.substring(0,i+1) 
						+ newCoordinates[0] + "," + newCoordinates[1]
						+ movedSegment.substring(j);
				i=j;
			}
		}
		return movedSegment;
	}

	public void postUndoableEdit(String editMsg) {
		undoableEditSupport.postEdit(new UndoableEdit(editMsg));
	}

	/**
	 * Redo the last undone change on the model
	 */
	public void redo() {
		GraphPane graphPane = _jgraph.getGraphPane();
		GraphController controller = (GraphController) graphPane.getGraphController();
		GraphModel model = controller.getGraphModel();
		try {
			NamedObj toplevel = (NamedObj) model.getRoot();
			RedoChangeRequest change = new RedoChangeRequest(this, toplevel);
			toplevel.requestChange(change);
		} catch (Exception ex) {
			MessageHandler.error("Redo failed", ex);
		}
	}

	/**
	 * Undo the last undoable change on the model
	 */
	public void undo() {
		GraphPane graphPane = _jgraph.getGraphPane();
		GraphController controller = (GraphController) graphPane.getGraphController();
		GraphModel model = controller.getGraphModel();
		try {
			NamedObj toplevel = (NamedObj) model.getRoot();
			UndoChangeRequest change = new UndoChangeRequest(this, toplevel);
			toplevel.requestChange(change);
		} catch (Exception ex) {
			MessageHandler.error("Undo failed", ex);
		}
	}

	/**
	 * Zoom in or out to magnify by the specified factor, from the current
	 * magnification.
	 * 
	 * @param factor
	 *            The magnification factor (relative to 1.0).
	 */
	public void zoom(double factor) {
		JCanvas canvas = _jgraph.getGraphPane().getCanvas();
		AffineTransform current = canvas.getCanvasPane().getTransformContext().getTransform();
		// Save the center, so we remember what we were looking at.
		Point2D center = getCenter();
		current.scale(factor, factor);
		canvas.getCanvasPane().setTransform(current);
		// Reset the center.
		setCenter(center);
		if (_graphPanner != null) {
			_graphPanner.repaint();
		}
	}

	/**
	 * Zoom to fit the current figures.
	 */
	public void zoomFit() {
		GraphPane pane = _jgraph.getGraphPane();
		Rectangle2D bounds = pane.getForegroundLayer().getLayerBounds();
		if (bounds.isEmpty()) {
			// Empty diagram.
			return;
		}
		Rectangle2D viewSize = getVisibleRectangle();
		AffineTransform newTransform = CanvasUtilities.computeFitTransform(bounds, viewSize);
		JCanvas canvas = pane.getCanvas();
		canvas.getCanvasPane().setTransform(newTransform);
		if (_graphPanner != null) {
			_graphPanner.repaint();
		}
	}

	/**
	 * Set zoom to the nominal.
	 */
	public void zoomReset() {
		JCanvas canvas = _jgraph.getGraphPane().getCanvas();
		AffineTransform current = canvas.getCanvasPane().getTransformContext().getTransform();
		current.setToIdentity();
		canvas.getCanvasPane().setTransform(current);
		if (_graphPanner != null) {
			_graphPanner.repaint();
		}
	}

	/**
	 * 
	 * @param listener
	 */
	public void addUndoableEditListener(UndoableEditListener listener) {
		undoableEditSupport.addUndoableEditListener(listener);
	}

	/**
	 * 
	 * @param listener
	 */
	public void removeUndoableEditListener(UndoableEditListener listener) {
		undoableEditSupport.removeUndoableEditListener(listener);
	}

	/**
	 * Return the center location of the visible part of the pane.
	 * 
	 * @return The center of the visible part.
	 */
	public Point2D getCenter() {
		Rectangle2D rect = getVisibleCanvasRectangle();
		return new Point2D.Double(rect.getCenterX(), rect.getCenterY());
	}

	/**
	 * Set the center location of the visible part of the pane. This will cause
	 * the panner to center on the specified location with the current zoom
	 * factor.
	 * 
	 * @param center
	 *            The center of the visible part.
	 */
	public void setCenter(Point2D center) {
		Rectangle2D visibleRect = getVisibleCanvasRectangle();
		AffineTransform newTransform = _jgraph.getCanvasPane().getTransformContext().getTransform();

		newTransform.translate(visibleRect.getCenterX() - center.getX(), visibleRect.getCenterY() - center.getY());

		_jgraph.getCanvasPane().setTransform(newTransform);
	}

	/**
	 * Return the rectangle representing the visible part of the pane,
	 * transformed into canvas coordinates. This is the range of locations that
	 * are visible, given the current pan and zoom.
	 * 
	 * @return The rectangle representing the visible part.
	 */
	public Rectangle2D getVisibleCanvasRectangle() {
		AffineTransform current = _jgraph.getCanvasPane().getTransformContext().getTransform();
		AffineTransform inverse;
		try {
			inverse = current.createInverse();
		} catch (NoninvertibleTransformException e) {
			throw new RuntimeException(e.toString());
		}
		Rectangle2D visibleRect = getVisibleRectangle();

		return ShapeUtilities.transformBounds(visibleRect, inverse);
	}

	/**
	 * Return the rectangle representing the visible part of the pane, in pixel
	 * coordinates on the screen.
	 * 
	 * @return A rectangle whose upper left corner is at (0, 0) and whose size
	 *         is the size of the canvas component.
	 */
	public Rectangle2D getVisibleRectangle() {
		Dimension size = _jgraph.getSize();
		return new Rectangle2D.Double(0, 0, size.getWidth(), size.getHeight());
	}

	/**
	 * Return the JGraph instance that this view uses to represent the ptolemy
	 * model.
	 * 
	 * @return the JGraph.
	 * @see #setJGraph(JGraph)
	 */
	public JGraph getJGraph() {
		return _jgraph;
	}

	/**
	 * Print the visible portion of the graph to a printer, which is represented
	 * by the specified graphics object.
	 * 
	 * @param graphics
	 *            The context into which the page is drawn.
	 * @param format
	 *            The size and orientation of the page being drawn.
	 * @param index
	 *            The zero based index of the page to be drawn.
	 * @return PAGE_EXISTS if the page is rendered successfully, or NO_SUCH_PAGE
	 *         if pageIndex specifies a non-existent page.
	 * @exception PrinterException
	 *                If the print job is terminated.
	 */
	public int print(Graphics graphics, PageFormat format, int index) throws PrinterException {
		if (getJGraph() != null) {
			Rectangle2D view = getVisibleRectangle();
			return getJGraph().print(graphics, format, index, view);
		} else {
			return NO_SUCH_PAGE;
		}
	}

	public static void invalidateUserLibrary(Configuration configuration) throws Exception {
		final CompositeEntity libraryContainer = (CompositeEntity) configuration.getEntity("actor library");
		if (libraryContainer != null) {
			final ModelDirectory directory = (ModelDirectory) configuration.getEntity(Configuration._DIRECTORY_NAME);
			if (directory != null) {
				// If we have a jar URL, convert spaces to %20
				URL fileURL = JNLPUtilities.canonicalizeJarURL(EnvironmentUtils.getUserLibraryURL());
				MoMLParser.purgeModelRecord(fileURL);
				MoMLParser.purgeAllModelRecords();
			}
		}
	}
	
	/**
	 * 
	 * @param configuration
	 * @param usrLibURL
	 * @param forceRefresh if true, the library is reloaded and its view representation is refreshed if it was already present.
	 * if false, an existing model/view is maintained for the library.
	 * @return
	 * @throws Exception
	 */
	public static EntityLibrary openLibrary(Configuration configuration, URL usrLibURL) throws Exception {
		final CompositeEntity libraryContainer = (CompositeEntity) configuration.getEntity("actor library");
		if (libraryContainer == null) {
			return null;
		}

		final ModelDirectory directory = (ModelDirectory) configuration.getEntity(Configuration._DIRECTORY_NAME);
		if (directory == null) {
			return null;
		}

		// If we have a jar URL, convert spaces to %20
		URL fileURL = JNLPUtilities.canonicalizeJarURL(usrLibURL);

		String identifier = fileURL.toExternalForm();

		// Check to see whether the library is already open.
		Effigy libraryEffigy = directory.getEffigy(identifier);
		if (libraryEffigy == null) {
			// No previous libraryEffigy exists that is identified by this URL.
			// Parse the user library into the workspace of the actor library.
			MoMLParser parser = new MoMLParser(libraryContainer.workspace());
			// Set the ErrorHandler so that if we have compatibility problems
			// between devel and production versions, we can skip that element.
			// EDL : this is now done in the HMI init
			// MoMLParser.setErrorHandler(new VergilErrorHandler());
			parser.parse(fileURL, fileURL);

			// Now create the effigy with no tableau.
			final PtolemyEffigy finalLibraryEffigy = new PtolemyEffigy(directory.workspace());
			finalLibraryEffigy.setSystemEffigy(true);

			final EntityLibrary userLibrary = (EntityLibrary) parser.getToplevel();

			// // Correct old library name.
			// if (userLibrary.getName().equals("user library")) {
			userLibrary.setName(LibraryManager.USER_LIBRARY_NAME);
			// }

			finalLibraryEffigy.setName(directory.uniqueName(userLibrary.getName()));

			ChangeRequest request = new ChangeRequest(configuration, usrLibURL.toString()) {
				protected void _execute() throws Exception {
					userLibrary.setContainer(libraryContainer);
					finalLibraryEffigy.setContainer(directory);
				}
			};

			libraryContainer.requestChange(request);
			request.waitForCompletion();

			finalLibraryEffigy.setModel(userLibrary);

			// Identify the URL from which the model was read
			// by inserting an attribute into both the model
			// and the effigy.
			URIAttribute uri = new URIAttribute(userLibrary, "_uri");
			uri.setURL(fileURL);

			// This is used by TableauFrame in its
			// _save() method.
			finalLibraryEffigy.uri.setURL(fileURL);
			finalLibraryEffigy.identifier.setExpression(identifier);

			return userLibrary;
		} else {
			return (EntityLibrary) ((PtolemyEffigy) libraryEffigy).getModel();
		}
	}

	/**
	 * Create the default library to use if an entity has no LibraryAttribute.
	 * Note that this is called in the constructor and therefore overrides in
	 * subclasses should not refer to any members that may not have been
	 * initialized. If no library is found in the configuration, then an empty
	 * one is created in the specified workspace.
	 * 
	 * @param workspace
	 *            The workspace in which to create the library, if one needs to
	 *            be created.
	 * @return The new library, or null if there is no configuration.
	 */
	protected CompositeEntity _createDefaultLibrary(Workspace workspace) {
		Configuration configuration = getConfiguration();
		if (configuration != null) {
			CompositeEntity result = (CompositeEntity) configuration.getEntity("actor library");
			if (result == null) {
				// Create an empty library by default.
				result = new CompositeEntity(workspace);
				try {
					result.setName("topLibrary");
					// Put a marker in so that this is
					// recognized as a library.
					new Attribute(result, "_libraryMarker");
				} catch (Exception ex) {
					throw new InternalErrorException("Library configuration failed: " + ex);
				}
			}
			return result;
		} else {
			return null;
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // private methods ////

	/**
	 * A customized port renderer that is able to adjust port colours depending
	 * on their type.
	 * 
	 * @author erwin
	 */
	public class EntityPortRenderer implements NodeRenderer {
		public Figure render(Object n) {
			final Port port = (Port) n;
			Polygon2D.Double polygon = new Polygon2D.Double();
			polygon.moveTo(-3, 3);
			polygon.lineTo(3, 0);
			polygon.lineTo(-3, -3);
			polygon.closePath();

			Color fill;
			if (port instanceof ErrorPort) {
				fill = Color.red;
			} else if (port instanceof ControlPort) {
				fill = Color.blue;
			} else if (port instanceof IOPort && ((IOPort) port).isMultiport()) {
				fill = Color.white;
			} else {
				fill = Color.black;
			}
			Figure figure = new BasicFigure(polygon, fill, (float) 1.5) {

				// Override this because we want to show the type.
				// It doesn't work to set it once because the type
				// has not been resolved, and anyway, it may
				// change.
				public String getToolTipText() {
					String tipText = port.getName();
					StringAttribute _explAttr = (StringAttribute) (port.getAttribute("_explanation"));
					if (_explAttr != null) {
						tipText = _explAttr.getExpression();
					} else if (port instanceof Typeable) {
						try {
							tipText = tipText + ", type:" + ((Typeable) port).getType();
						} catch (IllegalActionException ex) {
						}
					}
					return tipText;
				}
			};

			figure.setToolTipText(port.getName());

			// Wrap the figure in a TerminalFigure to set the direction that
			// connectors exit the port. Note that this direction is the
			// same direction that is used to layout the port in the
			// Entity Controller.

			int direction;
			if (!(port instanceof IOPort)) {
				direction = SwingUtilities.SOUTH;
			} else if (((IOPort) port).isInput() && ((IOPort) port).isOutput()) {
				direction = SwingUtilities.SOUTH;
			} else if (((IOPort) port).isInput()) {
				direction = SwingUtilities.WEST;
			} else if (((IOPort) port).isOutput()) {
				direction = SwingUtilities.EAST;
			} else {
				// should never happen
				direction = SwingUtilities.SOUTH;
			}
			double normal = CanvasUtilities.getNormal(direction);

			Site tsite = new PerimeterSite(figure, 0);
			tsite.setNormal(normal);
			tsite = new FixedNormalSite(tsite);
			figure = new TerminalFigure(figure, tsite);
			return figure;
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // ZoomInAction

	// An action to zoom in.
	public class ZoomInAction extends AbstractAction {
		public ZoomInAction(String description) {
			super(description);
			// Load the image by using the absolute path to the gif.
			// Using a relative location should work, but it does not.
			// Use the resource locator of the class.
			// For more information, see
			// jdk1.3/docs/guide/resources/resources.html
			URL img = getClass().getResource("/ptolemy/vergil/basic/img/zoomin.gif");
			if (img != null) {
				ImageIcon icon = new ImageIcon(img);
				putValue(GUIUtilities.LARGE_ICON, icon);
			}
			putValue("tooltip", description + " (Ctrl+Shift+=)");
			// NOTE: The following assumes that the + key is the same
			// as the = key. Unfortunately, the VK_PLUS key event doesn't
			// work, so we have to do it this way.
			putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
					| Event.SHIFT_MASK));
			// putValue(GUIUtilities.MNEMONIC_KEY,
			// new Integer(KeyEvent.VK_Z));
		}

		public void actionPerformed(ActionEvent e) {
			zoom(1.25);
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // ZoomResetAction

	// An action to reset zoom.
	public class ZoomResetAction extends AbstractAction {
		public ZoomResetAction(String description) {
			super(description);
			// Load the image by using the absolute path to the gif.
			// Using a relative location should work, but it does not.
			// Use the resource locator of the class.
			// For more information, see
			// jdk1.3/docs/guide/resources/resources.html
			URL img = getClass().getResource("/ptolemy/vergil/basic/img/zoomreset.gif");
			if (img != null) {
				ImageIcon icon = new ImageIcon(img);
				putValue(GUIUtilities.LARGE_ICON, icon);
			}
			putValue("tooltip", description + " (Ctrl+=)");
			putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			// putValue(GUIUtilities.MNEMONIC_KEY,
			// new Integer(KeyEvent.VK_M));
		}

		public void actionPerformed(ActionEvent e) {
			zoomReset();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // ZoomFitAction

	// An action to zoom fit.
	public class ZoomFitAction extends AbstractAction {
		public ZoomFitAction(String description) {
			super(description);
			// Load the image by using the absolute path to the gif.
			// Using a relative location should work, but it does not.
			// Use the resource locator of the class.
			// For more information, see
			// jdk1.3/docs/guide/resources/resources.html
			URL img = getClass().getResource("/ptolemy/vergil/basic/img/zoomfit.gif");
			if (img != null) {
				ImageIcon icon = new ImageIcon(img);
				putValue(GUIUtilities.LARGE_ICON, icon);
			}
			putValue("tooltip", description + " (Ctrl+Shift+-)");
			putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
					| Event.SHIFT_MASK));
			// putValue(GUIUtilities.MNEMONIC_KEY,
			// new Integer(KeyEvent.VK_F));
		}

		public void actionPerformed(ActionEvent e) {
			zoomFit();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // ZoomOutAction

	// An action to zoom out.
	public class ZoomOutAction extends AbstractAction {
		public ZoomOutAction(String description) {
			super(description);
			// Load the image by using the absolute path to the gif.
			// Using a relative location should work, but it does not.
			// Use the resource locator of the class.
			// For more information, see
			// jdk1.3/docs/guide/resources/resources.html
			URL img = getClass().getResource("/ptolemy/vergil/basic/img/zoomout.gif");
			if (img != null) {
				ImageIcon icon = new ImageIcon(img);
				putValue(GUIUtilities.LARGE_ICON, icon);
			}
			putValue("tooltip", description + " (Ctrl+-)");
			putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			// putValue(GUIUtilities.MNEMONIC_KEY,
			// new Integer(KeyEvent.VK_U));
		}

		public void actionPerformed(ActionEvent e) {
			zoom(1.0 / 1.25);
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // CopyAction

	/** Action to copy the current selection. */
	private class CopyAction extends AbstractAction {
		/** Create a new action to copy the current selection. */
		public CopyAction() {
			super("Copy");
			putValue("tooltip", "Copy the current selection onto the clipboard. (Ctrl+c)");
			putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));
			final URL img = getClass().getResource("copy.png");
            if (img != null) {
               final ImageIcon icon = new ImageIcon(img);
               putValue(GUIUtilities.LARGE_ICON, icon);
            }
		}

		/** Copy the current selection. */
		public void actionPerformed(ActionEvent e) {
			copy();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // CutAction

	/** Action to copy and delete the current selection. */
	private class CutAction extends AbstractAction {
		/** Create a new action to copy and delete the current selection. */
		public CutAction() {
			super("Cut");
			putValue("tooltip", "Cut the current selection onto the clipboard. (Ctrl+x)");
			putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
			final URL img = getClass().getResource("cut.png");
            if (img != null) {
               final ImageIcon icon = new ImageIcon(img);
               putValue(GUIUtilities.LARGE_ICON, icon);
            }
		}

		/** Copy and delete the current selection. */
		public void actionPerformed(ActionEvent e) {
			cut();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // PasteAction

	/** Paste the current contents of the clipboard into the current model. */
	private class PasteAction extends AbstractAction {
		/**
		 * Create a new action to paste the current contents of the clipboard
		 * into the current model.
		 */
		public PasteAction() {
			super("Paste");
			putValue("tooltip", "Paste the contents of the clipboard. (Ctrl+v)");
			putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
			final URL img = getClass().getResource("paste.png");
            if (img != null) {
               final ImageIcon icon = new ImageIcon(img);
               putValue(GUIUtilities.LARGE_ICON, icon);
            }
		}

		/**
		 * Paste the current contents of the clipboard into the current model.
		 */
		public void actionPerformed(ActionEvent e) {
			paste();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // RedoAction

	/**
	 * Redo the last undone MoML change on the current current model.
	 */
	private class RedoAction extends AbstractAction {
		/**
		 * Create a new action to paste the current contents of the clipboard
		 * into the current model.
		 */
		public RedoAction() {
			super("Redo");
			putValue("tooltip", "Redo the last change undone. (Ctrl+y)");
			putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(diva.gui.GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
			final URL img = getClass().getResource("redo.png");
            if (img != null) {
               final ImageIcon icon = new ImageIcon(img);
               putValue(GUIUtilities.LARGE_ICON, icon);
            }
		}

		/**
		 * Redo the last undone MoML change on the current current model.
		 * 
		 * @param e
		 *            The event for the action.
		 */
		public void actionPerformed(ActionEvent e) {
			redo();
		}
	}

	// /////////////////////////////////////////////////////////////////
	// // UndoAction

	/**
	 * Undo the last undoable MoML change on the current current model.
	 */
	private class UndoAction extends AbstractAction {
		/**
		 * Create a new action to paste the current contents of the clipboard
		 * into the current model.
		 */
		public UndoAction() {
			super("Undo");
			putValue("tooltip", "Undo the last change. (Ctrl+z)");
			putValue(diva.gui.GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(diva.gui.GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_U));
			final URL img = getClass().getResource("undo.png");
            if (img != null) {
               final ImageIcon icon = new ImageIcon(img);
               putValue(GUIUtilities.LARGE_ICON, icon);
            }
		}

		/**
		 * Undo the last undoable MoML change on the current current model.
		 * 
		 * @param e
		 *            The event for the action.
		 */
		public void actionPerformed(ActionEvent e) {
			undo();
		}
	}

	public class SelectAllAction extends AbstractAction {
		public SelectAllAction() {
			super("select all");
			final URL img = getClass().getResource("selectall.png");
			if (img != null) {
				final ImageIcon icon = new ImageIcon(img);
				putValue(GUIUtilities.LARGE_ICON, icon);
			}
			putValue("tooltip", "select all (Ctrl+A)");
		}

		public void actionPerformed(ActionEvent e) {
			controller.selectAll();
		}
	}

	public class ShowDocumentationAction extends GetDocumentationAction {
		public void actionPerformed(ActionEvent e) {
			// Figure targetFigure =
			// controller.getSelectionModel().getLastSelection();
			CanvasComponent targetFigure = controller.getGraphPane().getForegroundLayer().getCurrentFigure();
			NamedObj target = null;
			while ((target == null) && (targetFigure != null)) {
				Object object = targetFigure;

				if (object instanceof Figure) {
					object = ((Figure) targetFigure).getUserObject();
				}

				target = (NamedObj) controller.getGraphModel().getSemanticObject(object);
				targetFigure = targetFigure.getParent();
			}

			try {
				showDocumentation(target, controller._getEffigy(target, _configuration));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		@Override
		public Frame getFrame() {
			return ModelGraphPanel.this.getDialogHookComponent();
		}

		/**
		 * Show the documentation for a NamedObj. This does the same thing as
		 * the actionPerformed but without the action handler
		 * 
		 * @param target
		 *            The NamedObj that will have its documentation shown.
		 */
		public void showDocumentation(NamedObj target, Effigy context) {
			if (_configuration == null) {
				MessageHandler.error("Cannot get documentation without a configuration.");
			}

			// // If the object contains
			// // an attribute named of class DocAttribute or if there
			// // is a doc file for the object in the standard place,
			// // then use the DocViewer class to display the documentation.
			// // For backward compatibility, if neither of these is found,
			// // then we open the Javadoc file, if it is found.
			// List docAttributes = target.attributeList(DocAttribute.class);
			// int docAttributeSize = docAttributes.size();
			//
			// if (docAttributeSize != 0) {
			// // Have a doc attribute. Use that.
			// DocAttribute docAttribute = (DocAttribute)
			// docAttributes.get(docAttributes.size() - 1);
			// showDocAttributeTableau(docAttribute, target);
			// } else {
			// No doc attribute. Try for a doc file.
			String className = target.getClass().getName();
			getDocumentation(_configuration, className, context);
			// }
		}

	}
    // This listener is attached to this panner and is responsible for
    // panning the target in response to a mouse click on the panner.
    private class PanMouseListener extends MouseAdapter implements
            MouseMotionListener {
    	private int startX;
    	private int startY;
    	
        public void mousePressed(MouseEvent evt) {
            startX=evt.getX();
            startY=evt.getY();
        }

        public void mouseMoved(MouseEvent evt) {
        }

        public void mouseDragged(MouseEvent evt) {
            if ((_jgraph != null)
//                    && ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0)
                    && ((evt.getModifiers() & InputEvent.BUTTON2_MASK) != 0)
                    ) {
                setPosition(evt.getX(), evt.getY());
                startX=evt.getX();startY=evt.getY();
            }
        }
        
        public void setPosition(int x, int y) {
        	AffineTransform newTransform = _jgraph.getCanvasPane().getTransformContext().getTransform();
        	Point2D newCenter = new Point2D.Double(x, y);
        	newTransform.translate( x-startX,y-startY);

    		_jgraph.getCanvasPane().setTransform(newTransform);

        }
    }


	public class UndoableEdit extends AbstractUndoableEdit {

		private String changeMsg = "";

		public UndoableEdit(String msg) {
			changeMsg = msg;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.AbstractUndoableEdit#redo()
		 */
		public void redo() throws CannotRedoException {
			super.redo();
			ModelGraphPanel.this.redo();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.AbstractUndoableEdit#undo()
		 */
		public void undo() throws CannotUndoException {
			super.undo();
			ModelGraphPanel.this.undo();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.AbstractUndoableEdit#getPresentationName()
		 */
		public String getPresentationName() {
			return changeMsg;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.AbstractUndoableEdit#getRedoPresentationName()
		 */
		public String getRedoPresentationName() {
			// Default implementation adds I18N-ed "Redo" before name,
			// but NetBeans menu items already contain that...
			// So we drop it.
			return " " + getPresentationName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.undo.AbstractUndoableEdit#getUndoPresentationName()
		 */
		public String getUndoPresentationName() {
			// Default implementation adds I18N-ed "Undo" before name,
			// but NetBeans menu items already contain that...
			// So we drop it.
			return " " + getPresentationName();
		}

	}

	public LibraryManager getLibraryManager() {
		return libraryManager;
	}

	public EntityLibrary getUserLibrary() {
		return usrLibrary;
	}

}
