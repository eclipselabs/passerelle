package com.isencia.passerelle.hmi.generic;

import java.awt.TextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import com.isencia.passerelle.hmi.PopupUtil;

public class PasserelleIDEMain {

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
            final int argSize = args.length;
            if (argSize == 1) {
                if(args[0].endsWith(".moml")){
                    new PasserelleGUIBuilder(args[0], true, true, true);
                }
                else {
                    new PasserelleGUIBuilder(Boolean.valueOf(args[0]), true, true);
                }
            } else if (argSize == 2) {
                new PasserelleGUIBuilder( args[0], Boolean.valueOf(args[1]),true, true);
            } else {
                // build GUI with model graph
                // System.out.println("no model ");
                new PasserelleGUIBuilder(true, true, true);
            }
        } catch (final Throwable t) {
            t.printStackTrace();
            System.err.println("ERROR: Sequence file not found:" + args[0]);
            PopupUtil.showError(new TextArea(), "Not found: " + args[0]);
        }
    }
}
