/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Action;
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
import com.isencia.util.swing.components.FinderAccessory;
import diva.gui.ExtensionFileFilter;

@SuppressWarnings("serial")
public class OpenAction extends AbstractAction {
  private final static Logger logger = LoggerFactory.getLogger(OpenAction.class);

  public OpenAction(final HMIBase base) {
    super(base, HMIMessages.getString(HMIMessages.MENU_OPEN), null);
    StateMachine.getInstance().registerActionForState(StateMachine.READY, HMIMessages.MENU_OPEN, this);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_OPEN, this);

    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_O));

    // Set an accelerator key; this value is used by menu items
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
  }

  public void actionPerformed(final ActionEvent e) {
    if (logger.isTraceEnabled()) {
      logger.trace("File Open action - entry"); //$NON-NLS-1$
    }
    URL modelURL = null;
    try {
      JFileChooser fileChooser;

			if (getHMI().getModelURL() != null) {
				fileChooser = new JFileChooser(getHMI().getModelURL().getPath());
			} else if (HMIBase.MODELS_URL_STRING != null) {
				fileChooser = new JFileChooser(new URL(HMIBase.MODELS_URL_STRING).getFile());
      } else {
        fileChooser = new JFileChooser();
      }

			// get the list which show the list of files in current directory.
			final JList list = SwingUtils.getDescendantOfType(JList.class, fileChooser, "Enabled", true);
			// get the popup menu which is accecible by right click.
			final JPopupMenu popup = list.getComponentPopupMenu();

			// add delete action to the menu, we give map of all open sequence
			// in parameter to forbid to delete an open sequence
			final DeleteFileAction deleteAction = new DeleteFileAction(HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE), fileChooser, getHMI());
			popup.add(new JMenuItem(deleteAction));

			// Shortcut
			fileChooser.registerKeyboardAction(deleteAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

      fileChooser.setAccessory(new FinderAccessory(fileChooser));
      fileChooser.addChoosableFileFilter(new ExtensionFileFilter(new String[] { "xml", "moml" }, HMIBase.HMI_APPLICATIONNAME+" model files"));
      final int returnVal = fileChooser.showOpenDialog(getHMI().getDialogHookComponent());
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        modelURL = fileChooser.getSelectedFile().toURL();
        getHMI().loadModel(modelURL, null);

        StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
      }
    } catch (final Exception e1) {
      logger.error("Error opening file", e1);
      PopupUtil.showError(getHMI().getDialogHookComponent(), HMIMessages.HMI_ERROR_FILE_OPEN, e1.getMessage());
    }
    if (logger.isTraceEnabled()) {
      logger.trace("File Open action - exit - current file : " + modelURL); //$NON-NLS-1$
    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

}