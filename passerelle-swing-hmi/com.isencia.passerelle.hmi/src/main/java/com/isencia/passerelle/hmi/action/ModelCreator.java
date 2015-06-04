package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.ModelUtils;
import com.isencia.passerelle.hmi.PopupUtil;
import com.isencia.passerelle.hmi.state.StateMachine;

@SuppressWarnings("serial")
public class ModelCreator extends AbstractAction {

  private final static Logger logger = LoggerFactory.getLogger(ModelCreator.class);

  public ModelCreator(final HMIBase hmi) {
    super(hmi, HMIMessages.getString(HMIMessages.MENU_NEW), new ImageIcon(HMIBase.class.getResource("resources/new.gif")));

    StateMachine.getInstance().registerActionForState(StateMachine.READY, HMIMessages.MENU_NEW, this);
    StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_NEW, this);
  }

  public void actionPerformed(final ActionEvent e) {
    if (logger.isTraceEnabled()) {
      logger.trace("New Model action - entry"); //$NON-NLS-1$
    }
    final HMIBase hmi = getHMI();
    URL modelURL = hmi.getModelURL();

    try {
      modelURL = this.getClass().getResource("/com/isencia/passerelle/hmi/resources/new.moml");
      if (modelURL != null) {
        // force user to save the new model
        boolean saved = hmi.getSaveAsAction().save(ModelUtils.loadModel(modelURL), e);
        if (saved) StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
      } else {
        PopupUtil.showError(getHMI().getDialogHookComponent(), "error.file.new");
      }
    } catch (final Exception e1) {
      logger.error("impossible to open new model", e1);
      PopupUtil.showError(getHMI().getDialogHookComponent(), "error.file.new", e1.getMessage());
    }
    if (logger.isTraceEnabled()) {
      logger.trace("New Model action - exit - current file : " + modelURL); //$NON-NLS-1$
    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
