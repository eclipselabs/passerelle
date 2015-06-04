package com.isencia.passerelle.hmi.generic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.isencia.passerelle.hmi.state.StateMachine;

@SuppressWarnings("serial")
public final class GenericHMIBean extends JPanel {

  private PasserelleGUIBuilder mainGenericHMI;
  private boolean momlLoaded;
  private JPanel mainPanel;

  public GenericHMIBean() {
    super(new BorderLayout());
    initialize();
    momlLoaded = false;
  }

  public void initialize() {

    try {
      mainGenericHMI = new PasserelleGUIBuilder(false, false);
      mainPanel = mainGenericHMI.getBeanPanel();
      this.add(BorderLayout.CENTER, mainPanel);
    } catch (final Exception e) {
      e.printStackTrace();
      System.out.println(" error creating MainGenericHMI");
    }
  }

  public void setToolbarVisible(final boolean toolbarVisible) {
    mainGenericHMI.setToolbarVisible(toolbarVisible);
    this.removeAll();
    mainPanel = mainGenericHMI.getBeanPanel();
    this.add(BorderLayout.CENTER, mainPanel);
    this.revalidate();
  }

  public void setParametersVisible(final boolean parametersVisible) {
    mainGenericHMI.setParametersVisible(parametersVisible);
    this.removeAll();
    mainPanel = mainGenericHMI.getBeanPanel();
    this.add(BorderLayout.CENTER, mainPanel);
    this.revalidate();
  }

  public void setLogVisible(final boolean logVisible) {
    mainGenericHMI.setLogVisible(logVisible);
    this.removeAll();
    mainPanel = mainGenericHMI.getBeanPanel();
    this.add("Center", mainPanel);
    this.revalidate();
  }

  public void setEnable(final boolean enable) {
    if (enable) {
      if (StateMachine.getInstance().getCurrentState().equals(StateMachine.READY) && momlLoaded) {
        StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
      }
    } else {
      if (StateMachine.getInstance().getCurrentState().equals(StateMachine.MODEL_OPEN)) {
        StateMachine.getInstance().transitionTo(StateMachine.READY);
      }
    }
  }

  public void launchModel() {
    if (momlLoaded) {
      try {
        mainGenericHMI.getGenericHMI().launchModel(null);
      } catch (final Exception e) {
        System.out.println("impossible to start model");
        e.printStackTrace();
      }
    }
  }

  public void start() {
  }

  public void stop() {
  }

  public void loadSequenceModel(final String sequencePath) {
    if (sequencePath != null && sequencePath.length() != 0) {

      try {
        StateMachine.getInstance().transitionTo(StateMachine.READY);
        mainGenericHMI.getGenericHMI().clearModelForms(null);
        if (sequencePath.startsWith("file") || sequencePath.startsWith("http")) {
          mainGenericHMI.getGenericHMI().loadModel(new URL(sequencePath), null);
        } else {
            URL fileName = new URL("file:" + sequencePath);
            if(fileName.toURI().getPath() == null) {
                fileName = new URL("file:/" + sequencePath);
            }
            mainGenericHMI.getGenericHMI().loadModel(fileName, null);
        }
        this.removeAll();
        mainPanel = mainGenericHMI.getBeanPanel();
        this.add(BorderLayout.CENTER, mainPanel);
        StateMachine.getInstance().transitionTo(StateMachine.MODEL_OPEN);
        this.revalidate();
        momlLoaded = true;
      } catch (final Exception e) {
        e.printStackTrace();
        this.removeAll();
        final JLabel modelNameLabel = new JLabel(sequencePath + " not found or is not a moml file", JLabel.CENTER);
        modelNameLabel.setForeground(Color.RED);
        this.add(modelNameLabel, BorderLayout.CENTER);
        this.revalidate();
        momlLoaded = false;
        System.out.println(sequencePath + " file not found or is not a moml file");
      }
    }
  }

  public static void main(final String[] args) {
    try {
      // UIManager
      // .setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");

      final JFrame mainFrame = new JFrame("IHM Passerelle Generic Bean");

      final GenericHMIBean genericBean = new GenericHMIBean();

      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      mainFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - 30) / 2);

      mainFrame.setVisible(true);
      mainFrame.setContentPane(genericBean);
      // mainFrame.validate();

      genericBean.setParametersVisible(false);
      genericBean.setLogVisible(true);
      genericBean.loadSequenceModel(args[0]);

      // genericBean.setEnable(true);
      // Thread.sleep(1000);
      // genericBean.setEnable(false);
      // Thread.sleep(1000);
      // genericBean.setEnable(true);
      //
      // genericBean.launchModel();
      // genericBean.setEnable(false);
      // Thread.sleep(1000);

    } catch (final Exception exception) {
      exception.printStackTrace();
    }
  }

}
