package com.isencia.passerelle.hmi.generic;

import java.awt.TextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.isencia.passerelle.hmi.PopupUtil;

public class PasserelleEditorMain {

  public static void main(final String args[]) {
    // look & feel
    try {
      UIManager.put("swing.boldMetal", Boolean.FALSE);
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (final ClassNotFoundException e) {
      e.printStackTrace();
    } catch (final InstantiationException e) {
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      e.printStackTrace();
    } catch (final UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }

    try {
      // build GUI without model graph
      if (args.length != 0) {
        new PasserelleGUIBuilder(args[0], true,  false, true);
      } else {
        // build GUI with model graph
       // System.out.println("no model ");
        new PasserelleGUIBuilder(false, true);
      }
    } catch (final Throwable t) {
      t.printStackTrace();
      System.err.println("ERROR: Sequence file not found:" + args[0]);
      PopupUtil.showError(new TextArea(), "Not found: " + args[0]);
    }
  }
}
