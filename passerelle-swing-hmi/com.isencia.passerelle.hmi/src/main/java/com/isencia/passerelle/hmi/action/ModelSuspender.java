/*
 * (c) Copyright 2001-2006, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;

@SuppressWarnings("serial")
public class ModelSuspender extends AbstractAction {
	private final static Logger logger = LoggerFactory.getLogger(ModelSuspender.class);

	public ModelSuspender(final HMIBase base) {
		super(base, HMIMessages.getString(HMIMessages.MENU_SUSPEND), new ImageIcon(HMIBase.class.getResource("resources/suspend.gif")));
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	public synchronized void actionPerformed(final ActionEvent e) {
		if (getLogger().isTraceEnabled()) {
			getLogger().trace("Model Suspend action - entry"); //$NON-NLS-1$
		}

		 getHMI().suspendModel();

		if (getLogger().isTraceEnabled()) {
			getLogger().trace("Model Suspend action - exit"); //$NON-NLS-1$
		}
	}
}