package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map.Entry;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.StateMachine;
import com.isencia.passerelle.model.Flow;

@SuppressWarnings("serial")
public class CloseAllAction extends AbstractAction {

  private final static Logger logger = LoggerFactory.getLogger(CloseAllAction.class);

  public CloseAllAction(final HMIBase hmi) {
    super(hmi, HMIMessages.getString(HMIMessages.MENU_CLOSEALL), new ImageIcon(HMIBase.class.getResource("resources/close.png")));

    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_CLOSEALL, this);
    // This text is not directly used by any Swing component;
    // however, this text could be used in a help system
    putValue(Action.LONG_DESCRIPTION, "Close all models");

    // putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_V));

    // Set an accelerator key; this value is used by menu items
    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
  }

  public void actionPerformed(final ActionEvent arg0) {
    if (logger.isTraceEnabled()) {
      logger.trace("File Close All action - entry");
    }
    final HMIBase hmi = getHMI();
    for (Entry<URI, Flow> loadedModelEntry : hmi.getLoadedModels().entrySet()) {
		URI modelURI = loadedModelEntry.getKey();
		try {
    	  	hmi.close(modelURI.toURL());
		} catch (MalformedURLException ex) {
			// We can safely ignore this since the modelURL is in compliance with RFC2396
		}
    }

    if (logger.isTraceEnabled()) {
      logger.trace("File Close All action - exit");
    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }

}
