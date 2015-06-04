package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.model.Flow;

@SuppressWarnings("serial")
public class SaveAllAction extends AbstractAction {

  private final static Logger logger = LoggerFactory.getLogger(SaveAllAction.class);

  public SaveAllAction(final HMIBase hmi) {
    super(hmi, HMIMessages.getString(HMIMessages.MENU_SAVEALL), new ImageIcon(HMIBase.class.getResource("resources/saveall.gif")));

    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_SAVEALL, this);
    // This text is not directly used by any Swing component;
    // however, this text could be used in a help system
    putValue(Action.LONG_DESCRIPTION, "Save all models");

    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_V));

    // Set an accelerator key; this value is used by menu items
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
  }

  public void actionPerformed(final ActionEvent arg0) {
    if (logger.isTraceEnabled()) {
      logger.trace("File Save All action - entry");
    }
    final HMIBase hmi = getHMI();
    for (Entry<URI, Flow> loadedModelEntry : hmi.getLoadedModels().entrySet()) {
      URI modelURI = loadedModelEntry.getKey();
      if (hmi.isChangedModel(modelURI)) {
        Flow flowToSave = loadedModelEntry.getValue();
        try {
          hmi.saveModelAs(flowToSave, modelURI);
        } catch (Exception e) {
          logger.error("Error saving model", e);
          PopupUtil.showError(getHMI().getDialogHookComponent(), "error.file.save", flowToSave.getName());
        }
        System.out.println("Saved " + modelURI);
      }
    }

    if (logger.isTraceEnabled()) {
      logger.trace("File Save All action - exit");
    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

}
