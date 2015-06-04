/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.trace;

import com.isencia.passerelle.ext.ExecutionTracer;

/**
 * A visible ExecutionTracer
 * 
 * @author erwin.de.ley@isencia.be
 */
public interface TraceVisualizer extends ExecutionTracer {

  void show();

  void hide();

  void setVisible(boolean visible);
}
