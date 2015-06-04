/*
 * (c) Copyright 2011-, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.gui.graph.ModelGraphPanel;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.state.StateMachine;

@SuppressWarnings("serial")
public class ActorLibraryReloader extends AbstractAction {
	private final static Logger logger = LoggerFactory.getLogger(ActorLibraryReloader.class);

	public ActorLibraryReloader(final HMIBase base) {
		super(base, HMIMessages.getString(HMIMessages.MENU_ACTORLIBRARY_RELOAD), null);
		putValue(Action.SHORT_DESCRIPTION, HMIMessages.getString(HMIMessages.MENU_ACTORLIBRARY_RELOAD_TOOLTIP));
		StateMachine.getInstance().registerActionForState(StateMachine.READY, HMIMessages.MENU_ACTORLIBRARY_RELOAD, this);
		StateMachine.getInstance().registerActionForState(StateMachine.MODEL_OPEN, HMIMessages.MENU_ACTORLIBRARY_RELOAD, this);
	}

	public void actionPerformed(final ActionEvent e) {
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("actionPerformed() - entry"); //$NON-NLS-1$
		}

		try {
			ModelGraphPanel.invalidateUserLibrary(getHMI().getPtolemyConfiguration());
		} catch (Exception e1) {
			getLogger().error("Error reloading actor library",e1);
		}
		
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("actionPerformed() - exit"); //$NON-NLS-1$
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}