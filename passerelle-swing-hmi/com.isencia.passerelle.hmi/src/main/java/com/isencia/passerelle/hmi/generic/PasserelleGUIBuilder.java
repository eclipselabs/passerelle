package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.ClassicDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.PropertiesUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;
import org.apache.log4j.Logger;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.gui.PasserelleEditorPaneFactory;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.util.GenericHMIUserPref;

/**
 * Build the panels for Passerelle GUI.
 * 
 * @author ABEILLE
 * 
 */
public class PasserelleGUIBuilder {

	private GenericHMI genericHMI;

	private JPanel beanPanel;

	private JFrame mainFrame;

	private View parametersView;
	private View logView;
	private DockingWindow mainWindow;

	private RootWindow rootWindow;

	private boolean parametersVisible = true;

	private boolean logVisible = true;

	private boolean toolbarVisible = true;

	private boolean standalone;
	private final boolean showModelGraph;	
	private final boolean showModelForms;

	private View graphView;

	public PasserelleGUIBuilder(final boolean showModelGraph, final boolean standalone) {
	        this.showModelForms = true;
		this.showModelGraph = showModelGraph;
		this.standalone = standalone;
		try {
			buildGenericHMI(showModelForms,showModelGraph);
			if (standalone) {
				displayFrame();
			}
		} catch (final Throwable t) {
			System.err.println("ERROR: Impossible to create MainGenericHMI");
			StringWriter errWriter = new StringWriter();
			PrintWriter errPrtWriter = new PrintWriter(errWriter ,true);
			t.printStackTrace(errPrtWriter);
			PopupUtil.showError(new TextArea(), "Please contact ICA:\n"+errWriter.toString());
			System.exit(1);
		}
	}

  public PasserelleGUIBuilder(final boolean showModelForms, final boolean showModelGraph, final boolean standalone) {
    this.showModelForms = showModelForms;
    this.showModelGraph = showModelGraph;
    this.standalone = standalone;
    try {
      buildGenericHMI(showModelForms,showModelGraph);
      if (standalone) {
        displayFrame();
      }
    } catch (final Throwable t) {
      System.err.println("ERROR: Impossible to create MainGenericHMI");
      StringWriter errWriter = new StringWriter();
      PrintWriter errPrtWriter = new PrintWriter(errWriter ,true);
      t.printStackTrace(errPrtWriter);
      PopupUtil.showError(new TextArea(), "Please contact ICA:\n"+errWriter.toString());
      System.exit(1);
    }
  }

	public PasserelleGUIBuilder(final URL modelToLoad, final boolean showModelGraph) {
	  this.showModelForms = true;
		this.showModelGraph = showModelGraph;
		loadAndDisplayModel(modelToLoad);
	}

	public PasserelleGUIBuilder(final String modelToLoad,  final boolean showModelForms, final boolean showModelGraph, final boolean standalone) throws MalformedURLException {
	        this.showModelForms = showModelForms;
		this.showModelGraph = showModelGraph;
		this.standalone = standalone;
		URL url = null;
		if (modelToLoad.startsWith("file") || modelToLoad.startsWith("http")) {
			url = new URL(modelToLoad);
		} else {
			if (modelToLoad.startsWith("/")) {
				url = new URL("file:" + modelToLoad);
			} else {
				url = new URL("file:/" + modelToLoad);
			}
		}
		loadAndDisplayModel(url);
	}

	private void loadAndDisplayModel(final URL modelToLoad) {
		try {
			buildGenericHMI(showModelForms,showModelGraph);
		} catch (final Throwable t) {
			System.err.println("ERROR: can not configure HMI");
			t.printStackTrace();
			PopupUtil.showError(new TextArea(), "Can not configure HMI", t.getLocalizedMessage());
		}
		try {
			displayFrame();
			genericHMI.loadModel(modelToLoad, null);
			// state transition must be done only after the frame has been
			// displayed
			StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
		} catch (final IOException e1) {
			System.err.println("ERROR: Sequence file not found:" + modelToLoad);
			e1.printStackTrace();
			PopupUtil.showError(new TextArea(), "error.file.open", e1.getLocalizedMessage());
		} catch (final Throwable t) {
			System.err.println("ERROR: Unable to display model :" + modelToLoad);
			t.printStackTrace();
			PopupUtil.showError(new TextArea(), "error.file.open", t.getLocalizedMessage());
		} 
	}

	private void buildGenericHMI(final boolean showModelForms,final boolean showModelGraph) throws PasserelleException, IOException, NameDuplicationException, IllegalActionException {
		genericHMI = new GenericHMI(showModelForms,showModelGraph);
		genericHMI.setEditorPaneFactory(new PasserelleEditorPaneFactory());
		genericHMI.init();
	}

	public View getParametersView() {
		if (parametersView == null) {
			parametersView = new View("Parameters", new ImageIcon(Toolkit.getDefaultToolkit().getImage(
					getClass().getResource("/com/isencia/passerelle/hmi/resources/settings.gif"))), genericHMI.getParameterScrollPane());
		}
		return parametersView;
	}

	public View getLogView() {
		if (logView == null) {
			logView = new View("Log", new ImageIcon(Toolkit.getDefaultToolkit().getImage(
					getClass().getResource("/com/isencia/passerelle/hmi/resources/console.gif"))), genericHMI.getTracePanel());
		}
		return logView;
	}

	public View getGraphView() {
		if (graphView == null) {
			final JScrollPane modelGraphPanel = genericHMI.getModelGraphScrollPane();
			graphView = new View("Graph", new ImageIcon(Toolkit.getDefaultToolkit().getImage(
					getClass().getResource("/com/isencia/passerelle/hmi/resources/ModelDataIcon.gif"))), modelGraphPanel);
		}
		return graphView;
	}

	public RootWindow getMainRootWindow() {
		if (rootWindow == null) {
			final ViewMap viewMap = new ViewMap();

			try {
				final Runnable run = new Runnable() {
					public void run() {
						try {
							int i = 0;
							if (showModelGraph) {
								viewMap.addView(i++, getGraphView());
							}
							if(showModelForms) {
							  viewMap.addView(i++, getParametersView());
							}
							viewMap.addView(i++, getLogView());

							rootWindow = DockingUtil.createRootWindow(viewMap, true);
							prepareRootWindow();
							if (showModelGraph) {
								// modelGraphPanel.getWindowProperties()
								// .addSuperObject(
								// rootWindow
								// // .getWindowProperties());
							  if(showModelForms) {
							    mainWindow = new SplitWindow(true, 0.75f, graphView, getParametersView());
							  } else {
							    mainWindow = graphView;
							  }
								// mainWindow.setSelectedTab(0);
								rootWindow.setWindow(new SplitWindow(false, 0.75f, mainWindow, getLogView()));
							} else if (showModelForms) {
								rootWindow.setWindow(new SplitWindow(false, 0.5f, getParametersView(), getLogView()));
							} else {
                rootWindow.setWindow(getLogView());
							}

							// loadWindowLayoutPreferences(rootWindow, null);
						} catch (final Exception e) {
							Logger.getLogger(getClass()).error("Error while creating docking components", e);
						}
					}
				};
				if (Thread.currentThread().getName().startsWith("AWT-Event")) {
					// GBS runs everything in the AWT EventDispatchThread
					// Javadoc invokeAndWait: It should'nt be called from the
					// EventDispatchThread.
					run.run();
				} else {
					// Default Java apps are launched from Main Thread
					// We have to do this since Infonode is not thread safe
					SwingUtilities.invokeAndWait(run);
				}

			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rootWindow;
	}

	private void prepareRootWindow() {
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		rootWindow.getRootWindowProperties().getDockingWindowProperties().setUndockEnabled(false);
		rootWindow.getRootWindowProperties().getDockingWindowProperties().setCloseEnabled(false);
		final DockingWindowsTheme theme = new ClassicDockingTheme();
		// Apply theme
		rootWindow.getRootWindowProperties().addSuperObject(theme.getRootWindowProperties());

		final RootWindowProperties titleBarStyleProperties = PropertiesUtil.createTitleBarStyleRootWindowProperties();
		// Enable title bar style
		rootWindow.getRootWindowProperties().addSuperObject(titleBarStyleProperties);
	}

	public GenericHMI getGenericHMI() throws IOException, IllegalActionException, NameDuplicationException, PasserelleException {
		if (genericHMI == null) {
			buildGenericHMI(true,true);
		}
		return genericHMI;
	}

	public JPanel getBeanPanel() {
		// if (beanPanel == null) {
		beanPanel = new JPanel(new BorderLayout());
		if (toolbarVisible) {
			beanPanel.add(BorderLayout.BEFORE_FIRST_LINE, genericHMI.createDefaultToolbar());
		}
		if (parametersVisible && logVisible) {
			beanPanel.add(BorderLayout.CENTER, getMainRootWindow());

		} else if (parametersVisible) {
			beanPanel.add(BorderLayout.CENTER, genericHMI.getParameterScrollPane());
		} else if (logVisible) {
			beanPanel.add(BorderLayout.CENTER, genericHMI.getTracePanel());
		}
		// }
		return beanPanel;
	}

	public JFrame getMainFrame() {
		if (mainFrame == null) {
			mainFrame = new JFrame(HMIBase.HMI_APPLICATIONNAME);
			mainFrame.setContentPane(getBeanPanel());
			mainFrame.setIconImage(Toolkit.getDefaultToolkit()
					.getImage(PasserelleIDEMain.class.getResource("/com/isencia/passerelle/hmi/resources/runidew.gif")));
			mainFrame.setJMenuBar(genericHMI.getMenuBar());
//			if (standalone) {
				mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//			} else {
//				mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//			}
		}
		return mainFrame;
	}

	private void displayFrame() {
		final JFrame frame = getMainFrame();
		sizeAndPlaceFrame(frame);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				boolean allowClose = genericHMI.checkExitApplication();
				if(allowClose) {
					saveWindowLayoutPreferencesAndExit(rootWindow);
					super.windowClosed(e);
					mainFrame.dispose();
					if(standalone) {
						System.exit(0);
					}
				}
			}
		});
        frame.setVisible(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	private void sizeAndPlaceFrame(final JFrame frame) {
        final Rectangle rec = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        frame.setSize(rec.width / 2, rec.height - 100);
        frame.setLocation((rec.width - frame.getSize().width) / 2, (rec.height - frame.getSize().height) / 2);
	}

	private void saveWindowLayoutPreferencesAndExit(final RootWindow rootWindow) {
		// FIXME: java.io.IOException: Serialization of unknown view!
		// try {
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// final ObjectOutputStream out = new ObjectOutputStream(bos);
		// rootWindow.write(out, false);
		// out.close();
		// final byte[] layout = bos.toByteArray();
		// GenericHMIUserPref.putByteArrayPref(GenericHMIUserPref.LAYOUT,
		// layout);
		// bos.close();
		// bos = null;
		// System.exit(0);
		// } catch (final Exception e) {
		// e.printStackTrace();
		// JOptionPane.showMessageDialog(rootWindow,
		// "ScreenManager.saveWindowLayoutPreferences() :"
		// + " Unexpected Error (see traces)",
		// "Generic HMI - Error", JOptionPane.ERROR_MESSAGE);
		// }
	}

	public void loadWindowLayoutPreferences(final RootWindow rootWindow, final byte[] defaultLayout) {
		final byte[] prefs = GenericHMIUserPref.getByteArrayPref(GenericHMIUserPref.LAYOUT, defaultLayout);
		try {
			if (prefs != null) {
				rootWindow.read(new ObjectInputStream(new ByteArrayInputStream(prefs)));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isLogVisible() {
		return logVisible;
	}

	public void setLogVisible(final boolean logVisible) {
		this.logVisible = logVisible;
	}

	public boolean isParametersVisible() {
		return parametersVisible;
	}

	public void setParametersVisible(final boolean parametersVisible) {
		this.parametersVisible = parametersVisible;
	}

	public boolean isToolbarVisible() {
		return toolbarVisible;
	}

	public void setToolbarVisible(final boolean toolbarVisible) {
		this.toolbarVisible = toolbarVisible;
	}
}
