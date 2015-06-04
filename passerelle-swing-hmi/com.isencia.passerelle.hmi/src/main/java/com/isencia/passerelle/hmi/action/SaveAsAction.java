package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.hmi.util.SwingUtils;
import com.isencia.passerelle.model.Flow;
import diva.gui.ExtensionFileFilter;

@SuppressWarnings("serial")
public class SaveAsAction extends AbstractAction {

  private final static Logger logger = LoggerFactory.getLogger(SaveAsAction.class);
  private String selectedFile;
  private String selectedFilePath;

  public SaveAsAction(final HMIBase hmi) {
    super(hmi, HMIMessages.getString(HMIMessages.MENU_SAVEAS), new ImageIcon(HMIBase.class.getResource("resources/saveas.gif")));

    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVEAS, this);

    // This text is not directly used by any Swing component;
    // however, this text could be used in a help system
    putValue(Action.LONG_DESCRIPTION, "Save model parameters");

    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

  public void actionPerformed(final ActionEvent e) {
    try {
      this.save(getHMI().getCurrentModel(), e);
    } catch (IOException e1) {
      logger.error("Error saving model", e1);
      PopupUtil.showError(getHMI().getDialogHookComponent(), "error.file.save", e1.getMessage());
    }
  }

  /**
   * @param model
   * @param e
   * @return true if the model has effectively been saved, false if not
   *         (typically since the user canceled it)
   * @throws IOException
   */
  public boolean save(final Flow model, final ActionEvent e) throws IOException {
    if (logger.isTraceEnabled()) {
      logger.trace("File SaveAs action - entry"); //$NON-NLS-1$
    }
    boolean saved = false;
    JFileChooser fileChooser = null;

    try {
			if (getHMI().getModelURL() != null) {
        fileChooser = new JFileChooser(getHMI().getModelURL().getPath());
			} else if (HMIBase.MODELS_URL_STRING != null) {
				fileChooser = new JFileChooser(new URL(HMIBase.MODELS_URL_STRING).getFile());
      } else {
        fileChooser = new JFileChooser();
      }
    } catch (final MalformedURLException e1) {
      logger.error("Error saving model", e1);
      PopupUtil.showError(getHMI().getDialogHookComponent(), "error.file.save", e1.getMessage());
    }

		// get the list which show the list of files in current directory.
		final JList list = SwingUtils.getDescendantOfType(JList.class, fileChooser, "Enabled", true);
		// get the popup menu which is accecible by right click.
		final JPopupMenu popup = list.getComponentPopupMenu();

		// add delete action to the menu, we give map of all open sequence in
		// parameter to forbid to delete an open sequence
		final DeleteFileAction deleteAction = new DeleteFileAction(HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE), fileChooser, getHMI());
		popup.add(new JMenuItem(deleteAction));

		// Shortcut
		fileChooser.registerKeyboardAction(deleteAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

    fileChooser.addChoosableFileFilter(new ExtensionFileFilter(new String[] { "moml" }, HMIBase.HMI_APPLICATIONNAME+" model files"));
    final int returnVal = fileChooser.showSaveDialog(getHMI().getDialogHookComponent());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      // if the filename is ending by ".moml" we don't need to add
      // it...
      String dest = null;
      if (fileChooser.getSelectedFile().toString().endsWith(".moml")) {
        dest = fileChooser.getSelectedFile().toString();
      } else {
        dest = fileChooser.getSelectedFile().toString() + ".moml";
      }
      final File destinationFile = new File(dest);
      selectedFile = destinationFile.getName();

      if (destinationFile.exists()) {
        throw new IOException("File already exists " + selectedFile);
      } else {
        try {
          selectedFilePath = destinationFile.getCanonicalPath();
        } catch (final IOException e2) {
          logger.error("Error saving model", e2);
          PopupUtil.showError(getHMI().getDialogHookComponent(), "error.file.save", e2.getMessage());
        }

        try {
          final HMIBase hmi = getHMI();
          hmi.saveModelAs(model, destinationFile.toURI());
          hmi.setModelURL(destinationFile.toURI().toURL());
          // load saved file
          hmi.loadModel(hmi.getModelURL(), null);
          saved = true;
        } catch (final Exception e1) {
          logger.error("Error saving model", e1);
          PopupUtil.showError(getHMI().getDialogHookComponent(), "error.file.save", e1.getMessage());
        }
      }
    } else {
      // PopupUtil.showWarning(new TextArea(), "Nothing saved!!!");
    }
    if (logger.isTraceEnabled()) {
      logger.trace("File SaveAs action - exit - saved it? : " + saved); //$NON-NLS-1$
    }

    return saved;
  }

  public String getSelectedFile() {
    return selectedFile;
  }

  public String getSelectedFilePath() {
    return selectedFilePath.replace('\\', '/');
  }

}
