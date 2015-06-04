/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.trace;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import javax.swing.JDialog;
import com.isencia.passerelle.util.Level;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.NamedObj;

/**
 * @todo Class Comment
 * @author erwin.de.ley@isencia.be
 */
public class TraceDialog extends JDialog implements TraceVisualizer {

  private TracePanel tracePanel;

  /**
   * @throws HeadlessException
   */
  public TraceDialog(Frame owner) throws HeadlessException {
    super(owner, "Trace Messages", false);

    tracePanel = new TracePanel();
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(tracePanel, BorderLayout.CENTER);
    setSize(400, 600);
    setLocationRelativeTo(owner);
    /*
     * // buttons JPanel buttonPanel = new JPanel(); buttonPanel.setLayout(new
     * FlowLayout()); JButton closeButton = new JButton("Close");
     * closeButton.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { dispose(); } });
     * buttonPanel.add(closeButton);
     * getContentPane().add(buttonPanel,BorderLayout.SOUTH);
     */
  }

  public void trace(Actor source, String message) {
    tracePanel.addTraceMessage(getFullNameButWithoutModelName((NamedObj)source), message);
  }

  public void trace(Director source, String message) {
        tracePanel.addTraceMessage(source!=null?source.getName():"main", message);
  }

  public TracePanel getTracePanel() {
    return tracePanel;
  }

  public void setTracePanel(TracePanel tracePanel) {
    this.tracePanel = tracePanel;
  }

	public static String getFullNameButWithoutModelName(NamedObj source) {
		if(source==null)
			return "unknown";
		
    // the first string is the name of the model
		String fullName = source.getFullName();
    int i = fullName.indexOf(".", 1);
    if (i > 0) {
      // there's always an extra '.' in front of the model name...
      // and a trailing '.' just behind it...
      fullName = fullName.substring(i + 1);
    }

    return fullName;
  }

  public void trace(Actor source, String message, Level level) {
    trace(source, message);
  }

  public void trace(Director source, String message, Level level) {
    trace(source, message);
  }
}
