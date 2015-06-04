package com.isencia.passerelle.hmi.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.hmi.HMIBase;
import com.isencia.passerelle.hmi.HMIMessages;
import com.isencia.passerelle.hmi.util.SwingUtils;

public class DeleteFileAction extends AbstractAction {
  private static final long serialVersionUID = 2603323877590335139L;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFileAction.class);

  private JFileChooser chooser = null;
  /**
   * map of all open sequence
   */
  private final HMIBase hmiBase;

  public DeleteFileAction(final String desc, final JFileChooser aChooser, final HMIBase hmiBase) {
    super(desc);
    this.chooser = aChooser;
    this.hmiBase = hmiBase;
  }

  public void actionPerformed(final ActionEvent e) {
    final File file = chooser.getSelectedFile();
    if (file != null) {
      URI fileURI = file.toURI();
      if (!hmiBase.isDeleteAllowed(fileURI)) {
        JOptionPane.showMessageDialog(chooser, HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_IMPOSSIBLE),
            HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE), JOptionPane.ERROR_MESSAGE);
      } else {
        final int res = JOptionPane.showConfirmDialog(chooser, HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE) + " "
            + chooser.getSelectedFile().getAbsolutePath() + "?", HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_MESSAGE), JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
          try {
            boolean deleted = hmiBase.delete(file.toURI());
            if (deleted) {
              chooser.rescanCurrentDirectory(); // update list of files.
              // unselect deleted file.
              chooser.setSelectedFile(null);
            } else {
              JOptionPane.showMessageDialog(chooser, HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_IMPOSSIBLE),
                  HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE), JOptionPane.WARNING_MESSAGE);
            }

            // get all JTextField in chooser
            final List<JTextField> textFieldList = SwingUtils.getDescendantsOfType(JTextField.class, chooser, true);

            // get JTextField which contains the name of file
            // previously deleted and set text to "".
            boolean found = false;
            final Iterator<JTextField> itt = textFieldList.iterator();
            while (itt.hasNext() && !found) {
              final JTextField temp = itt.next();
              if (temp.getText().equals(file.getName())) {
                temp.setText("");
                found = true;
              }
            }
          } catch (IllegalStateException ex) {
            // this should not happen as we've checked if the delete was allowed at the start of the method
            // but one never knows...
            JOptionPane.showMessageDialog(chooser, HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_IMPOSSIBLE),
                HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE), JOptionPane.ERROR_MESSAGE);
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(chooser, HMIMessages.getString(HMIMessages.ERROR_GENERIC),
                HMIMessages.getString(HMIMessages.FILECHOOSER_DELETE_TITLE), JOptionPane.ERROR_MESSAGE);
            LOGGER.error(ErrorCode.ERROR + " - Delete of " + file + " failed unexpectedly", ex);
          }
        }
      }
    }
  }
}
