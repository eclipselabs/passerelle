/* Copyright 2014 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.isencia.passerelle.actor.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.style.ParameterEditorStyle;
import ptolemy.actor.parameters.IntRangeParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.ErrorHandler;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.actor.gui.binding.ParameterToWidgetBinder;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.util.ptolemy.DateTimeParameter;
import com.isencia.passerelle.util.ptolemy.ParameterGroup;
import com.isencia.util.swing.calendar.DateTimeSelector;
import com.isencia.util.swing.components.FinderAccessory;

// ////////////////////////////////////////////////////////////////////////
// // PasserelleQuery
/**
 * This class is a query dialog box with various entries for setting the values of Ptolemy II attributes that implement
 * the Settable interface and have visibility FULL. One or more entries are associated with an attribute so that if the
 * entry is changed, the attribute value is updated, and if the attribute value changes, the entry is updated. To change
 * an attribute, this class queues a change request with a particular object called the <i>change handler</i>. The
 * change handler is specified as a constructor argument.
 * <p>
 * It is important to note that it may take some time before the value of a attribute is actually changed, since it is
 * up to the change handler to decide when change requests are processed. The change handler will typically delegate
 * change requests to the Manager, although this is not necessarily the case.
 * <p>
 * To use this class, add an entry to the query using addStyledEntry().
 * 
 * @author Brian K. Vogel and Edward A. Lee
 * @version $Id: PasserelleQuery.java,v 1.8 2006/04/21 15:55:58 erwin Exp $
 * @since Ptolemy II 0.4
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Yellow (neuendor)
 */
public class PasserelleQuery extends ptolemy.actor.gui.PtolemyQuery implements IPasserelleQuery, IPasserelleComponent {

  private static final String NEW_NAME_FIELDLABEL = "New name";
  private static final long serialVersionUID = 1L;
  private final static Logger logger = LoggerFactory.getLogger(PasserelleQuery.class);

  // EDL : a callback strategy to be able to customize labels for parameters
  public static interface QueryLabelProvider {
    String getLabelFor(Settable settable);
  }

  private QueryLabelProvider labelProvider;
  private boolean nameChanged;

  /**
   * Construct a panel with no queries in it and with the specified change handler. When an entry changes, a change
   * request is queued with the given change handler. The change handler should normally be a composite actor that
   * deeply contains all attributes that are attached to query entries. Otherwise, the change requests might get queued
   * with a handler that has nothing to do with the attributes. The handler is also used to report errors.
   * 
   * @param handler
   *          The change handler.
   */
  public PasserelleQuery(NamedObj handler) {
    this(handler, null);
  }

  /**
   * 
   * @param handler
   * @param labelProvider
   */
  public PasserelleQuery(NamedObj handler, QueryLabelProvider labelProvider) {
    this(handler, labelProvider, true);
  }

  /**
   * Construct a panel with no queries in it and with the specified change handler. When an entry changes, a change
   * request is queued with the given change handler. The change handler should normally be a composite actor that
   * deeply contains all attributes that are attached to query entries. Otherwise, the change requests might get queued
   * with a handler that has nothing to do with the attributes. The handler is also used to report errors.
   * 
   * @param handler
   *          The change handler.
   * @param labelProvider
   * @param allowRename
   */
  public PasserelleQuery(NamedObj handler, QueryLabelProvider labelProvider, boolean allowRename) {
    super(handler);
    this.labelProvider = labelProvider;
    _handler = handler;
    if (_handler != null) {
      if (allowRename)
        addLine(NEW_NAME_FIELDLABEL, NEW_NAME_FIELDLABEL, handler.getName());
      // NOTE: Since we register as a listener to the handler,
      // there is no need to also register as a listner with
      // each change request. EAL 9/15/02.
      _handler.addChangeListener(this);

      if (Actor.class.isInstance(_handler)) {
        configureParameters((Actor) _handler);
      }
    }
  }

  /**
   * We don't put this as a method on Actor, as this kind of parameter configuration is only useful in the context of
   * the UI parameter pop-ups...
   */
  private void configureParameters(Actor actor) {
    List<Parameter> parameters = actor.attributeList(Parameter.class);
    if (actor.getOptionsFactory() != null) {
      for (Iterator<Parameter> iter = parameters.iterator(); iter.hasNext();) {
        actor.getOptionsFactory().setOptionsForParameter(iter.next());
      }
    }
  }

  // /////////////////////////////////////////////////////////////////
  // // public methods ////

  @Override
  public void changed(String name) {
    if (NEW_NAME_FIELDLABEL.equals(name)) {
      nameChanged = true;
      // applyNameChange();
    }
    super.changed(name);
  }

  /**
   * block updates to prevent feedback loops in the HMI remark that this means that open cfg panels will not adjust
   * automatically when a parameter value changes in an "unsollicited" way, i.e. by some external effect unrelated to
   * the cfg panel in question.
   */
  @Override
  public void valueChanged(Settable attribute) {
    // super.valueChanged(attribute);
    // System.out.println("value changed for "+attribute.getFullName());
  }

  public void setEnabled(boolean enabled) {
    Iterator entriesItr = _entries.keySet().iterator();
    while (entriesItr.hasNext()) {
      String name = (String) entriesItr.next();
      setEnabled(name, enabled);
    }
  }

  /**
   * Add a new entry to this query that represents the given attribute. The name of the entry will be set to the name of
   * the attribute, and the attribute will be attached to the entry, so that if the attribute is updated, then the entry
   * is updated. If the attribute contains an instance of ParameterEditorStyle, then defer to the style to create the
   * entry, otherwise just create a default entry. The style used in a default entry depends on the class of the
   * attribute and on its declared type, but defaults to a one-line entry if there is no obviously better style. Only
   * the first style that is found is used to create an entry.
   * 
   * @param attribute
   *          The attribute to create an entry for.
   */
  public void addStyledEntry(Settable attribute) {
    // Note: it would be nice to give
    // multiple styles to specify to create more than one
    // entry for a particular parameter. However, the style configurer
    // doesn't support it and we don't have a good way of representing
    // it in this class.
    // Look for a ParameterEditorStyle.
    boolean foundStyle = false;
    if (attribute instanceof NamedObj) {
      Iterator styles = ((NamedObj) attribute).attributeList(ParameterEditorStyle.class).iterator();
      while (styles.hasNext() && !foundStyle) {
        ParameterEditorStyle style = (ParameterEditorStyle) styles.next();
        try {
          style.addEntry(this);
          foundStyle = true;
        } catch (IllegalActionException ex) {
          // Ignore failures here, and just present
          // the default dialog.
        }
      }
    }
    String name = attribute.getName();
    String label = labelProvider != null ? labelProvider.getLabelFor(attribute) : name;
    if (!foundStyle) {
      // NOTE: Infer the style.
      // This is a regrettable approach, but it keeps
      // dependence on UI issues out of actor definitions.
      // Also, the style code is duplicated here and in the
      // style attributes. However, it won't work to create
      // a style attribute here, because we don't necessarily
      // have write access to the workspace.
      try {
        if (attribute.getVisibility() == Settable.NOT_EDITABLE) {
          String defaultValue = attribute.getExpression();
          addDisplay(name, label, defaultValue);
          attachParameter(attribute, name);
          foundStyle = true;
        } else if (attribute instanceof IntRangeParameter) {
          int current = ((IntRangeParameter) attribute).getCurrentValue();
          int min = ((IntRangeParameter) attribute).getMinValue();
          int max = ((IntRangeParameter) attribute).getMaxValue();
          addSlider(name, label, current, min, max);
          attachParameter(attribute, name);
          foundStyle = true;
        } else if (attribute instanceof ColorAttribute) {
          addColorChooser(name, label, attribute.getExpression());
          attachParameter(attribute, name);
          foundStyle = true;
        } else if (attribute instanceof FileParameter) {
          File directory = null;
          FileFilter fileFilter = null;
          URI base = null;
          if (directory == null) {
            // Specify the directory in which to start browsing
            // to be the location where the model is defined,
            // if that is known.
            URI modelURI = URIAttribute.getModelURI((NamedObj) attribute);
            if (modelURI != null) {
              if (modelURI.getScheme().equals("file")) {
                File modelFile = new File(modelURI);
                directory = modelFile.getParentFile();
              }
            }
          }
          if (directory != null) {
            base = directory.toURI();
          }
          // Specify the directory in which to start browsing
          // to be the one contained in the FileParameter, if it is
          // not empty.
          File currentFile = ((FileParameter) attribute).asFile();
          if (currentFile != null) {
            directory = currentFile;
          }
          if (attribute instanceof com.isencia.passerelle.util.ptolemy.FileParameter) {
            // this is a Passerelle extension that is able to
            // maintain a FileFilter
            com.isencia.passerelle.util.ptolemy.FileParameter passerelleFileParameter = (com.isencia.passerelle.util.ptolemy.FileParameter) attribute;
            fileFilter = passerelleFileParameter.getFilter();
          }
          if (fileFilter != null) {
            addFileChooser(name, label, attribute.getExpression(), base, directory, fileFilter, preferredBackgroundColor(attribute),
                preferredForegroundColor(attribute));
          } else {
            // Check to see whether the attribute being configured
            // specifies whether files or directories should be
            // listed.
            // By default, only files are selectable.
            boolean allowFiles = true;
            boolean allowDirectories = false;
            // attribute is always a NamedObj
            Parameter marker = (Parameter) ((NamedObj) attribute).getAttribute("allowFiles", Parameter.class);
            if (marker != null) {
              Token value = marker.getToken();
              if (value instanceof BooleanToken) {
                allowFiles = ((BooleanToken) value).booleanValue();
              }
            }
            marker = (Parameter) ((NamedObj) attribute).getAttribute("allowDirectories", Parameter.class);
            if (marker != null) {
              Token value = marker.getToken();
              if (value instanceof BooleanToken) {
                allowDirectories = ((BooleanToken) value).booleanValue();
              }
            }
            // FIXME: What to do when neither files nor directories
            // are allowed?
            if (!allowFiles && !allowDirectories) {
              // The given attribute will not have a query in the
              // dialog.
              return;
            }
            addFileChooser(name, label, attribute.getExpression(), base, directory, allowFiles, allowDirectories, preferredBackgroundColor(attribute),
                preferredForegroundColor(attribute));
          }
          attachParameter(attribute, name);
          foundStyle = true;
        } else if (attribute instanceof DateTimeParameter) {
          addDatePicker(name, label, ((DateTimeParameter) attribute).getDateValue(), Color.ORANGE, preferredForegroundColor(attribute));
          attachParameter(attribute, name);
          foundStyle = true;
        } else if (attribute instanceof ParameterGroup) {
          addParameterGroupButton(name, label, (ParameterGroup) attribute);
          foundStyle = true;
        } else if (attribute instanceof Parameter && ((Parameter) attribute).getChoices() != null) {
          Parameter castAttribute = (Parameter) attribute;
          // NOTE: Make this always editable since Parameter
          // supports a form of expressions for value propagation.
          addChoice(name, label, castAttribute.getChoices(), castAttribute.getExpression(), true, preferredBackgroundColor(attribute),
              preferredForegroundColor(attribute));
          attachParameter(attribute, name);
          foundStyle = true;
        } else if (attribute instanceof Variable) {
          Type declaredType = ((Variable) attribute).getDeclaredType();
          Token current = ((Variable) attribute).getToken();
          if (declaredType == BaseType.BOOLEAN) {
            // NOTE: If the expression is something other than
            // "true" or "false", then this parameter is set
            // to an expression that evaluates to to a boolean,
            // and the default Line style should be used.
            if (attribute.getExpression().equals("true") || attribute.getExpression().equals("false")) {
              addCheckBox(name, label, ((BooleanToken) current).booleanValue());
              attachParameter(attribute, name);
              foundStyle = true;
            }
          }
        }
        // FIXME: Other attribute classes? TextStyle?
      } catch (IllegalActionException ex) {
        // Ignore and create a line entry.
      }
    }
    String defaultValue = attribute.getExpression();
    if (defaultValue == null)
      defaultValue = "";
    if (!(foundStyle)) {
      addLine(name, label, defaultValue, preferredBackgroundColor(attribute), preferredForegroundColor(attribute));
      // The style itself does this, so we don't need to do it again.
      attachParameter(attribute, name);
    }
  }

  public void addFileChooser(String name, String label, String defaultName, URI base, File startingDirectory, FileFilter fileFilter, Color background,
      Color foreground) {

    JLabel lbl = new JLabel(label + ": ");
    lbl.setBackground(_background);
    QueryFileChooser fileChooser = new QueryFileChooser(name, defaultName, base, startingDirectory, fileFilter, background, foreground);
    _addPair(name, lbl, fileChooser, fileChooser);
  }

  /**
   * @param name
   * @param label
   * @param paramGroup
   */
  private void addParameterGroupButton(String name, String label, ParameterGroup paramGroup) {
    JLabel lbl = new JLabel(label + ": ");
    lbl.setBackground(_background);
    ParameterGroupButton button = new ParameterGroupButton(name, label, paramGroup);
    _addPair(name, lbl, button, button);
  }

  /**
   * @param name
   * @param label
   * @param startDate
   * @param background
   * @param foreground
   */
  private void addDatePicker(String name, String label, Date startDate, Color background, Color foreground) {
    JLabel lbl = new JLabel(label + ": ");
    lbl.setBackground(_background);
    QueryDatePicker datePicker = new QueryDatePicker(name, label, startDate, background, foreground);
    _addPair(name, lbl, datePicker, datePicker);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ptolemy.gui.Query#getStringValue(java.lang.String)
   */
  public String getStringValue(String name) throws NoSuchElementException, IllegalArgumentException {
    Object result = _entries.get(name);
    if (result == null) {
      throw new NoSuchElementException("No item named \"" + name + " \" in the query box.");
    }
    if (result instanceof QueryDatePicker) {
      String res = ((QueryDatePicker) result).getDateTimeAsString();
      if (logger.isDebugEnabled())
        logger.debug(((QueryDatePicker) result).getName() + " has value " + res);
      return res;
    } else if (result instanceof ParameterGroupButton) {
      return "";
    } else if (result instanceof QueryFileChooser) {
      return ((QueryFileChooser) result).getSelectedFileName();
    } else {
      try {
        return super.getStringValue(name);
      } catch (IllegalArgumentException e) {
        // This can happen when the entry is a password field
        // for some reason the Ptolemy guys think they need to build
        // a hyper-secure UI...
        // Although they will naturally store the password
        // in clear text in the .moml file anyway ;-)
        char[] value = super.getCharArrayValue(name);
        return new String(value);
      }
    }
  }

  public void setStringValue(String name, String value) {
    this.set(name, value);
  }

  /**
   * This method is made final, to limit the visibility of the Ptolemy API, use the setStringValue method instead
   */
  @Override
  public final void set(String name, String value) throws NoSuchElementException, IllegalArgumentException {
    try {
      super.set(name, value);
    } catch (IllegalArgumentException e) {
      // eat it, it's almost always caused by our overwritten/added param
      // cfg panels (file chooser, colour picker etc)
      // that are unknown to Ptolemy's Query class.
      // TODO find out a neater way to extend Ptolemy query without
      // needing to duplicate their inner classes etc.
    }
  }

  /**
   * Apply name change at this moment, similar to the original Ptolemy RenameConfigurer behaviour.
   */
  @Override
  public void windowClosed(Window window, String button) {
    super.windowClosed(window, button);
    if (nameChanged && !"Cancel".equals(button)) {
      applyNameChange();
    }
  }

  private void applyNameChange() {
    String newName = getStringValue(NEW_NAME_FIELDLABEL);

    NamedObj parent = _handler.getContainer();
    String oldName = _handler.getName();

    StringBuffer moml = new StringBuffer("<");
    String elementName = _handler.getElementName();
    moml.append(elementName);
    moml.append(" name=\"");
    moml.append(oldName);
    moml.append("\">");
    if (!oldName.equals(newName)) {
      moml.append("<rename name=\"");
      moml.append(newName);
      moml.append("\"/>");
    }
    moml.append("</");
    moml.append(elementName);
    moml.append(">");

    MoMLChangeRequest request = new RenameRequest(this, // originator
        parent, // context
        moml.toString(), oldName, newName);

    request.addChangeListener(this);
    request.setUndoable(true);
    parent.requestChange(request);
    nameChanged = false;
  }

  @Override
  public void changeExecuted(ChangeRequest change) {
    super.changeExecuted(change);
  }

  /**
   * Notify the listener that a change attempted by the change handler has resulted in an exception. This method brings
   * up a new dialog to prompt the user for a corrected entry. If the user hits the cancel button, then the attribute is
   * reverted to its original value.
   * 
   * @param change
   *          The change that was attempted.
   * @param exception
   *          The exception that resulted.
   */
  public void changeFailed(final ChangeRequest change, Exception exception) {
    // Ignore if this was not the originator, or if the error has already
    // been reported, or if the change request is null.
    if (change == null || change.getSource() != this) {
      return;
    }
    // Restore the parser error handler.
    if (_savedErrorHandler != null) {
      MoMLParser.setErrorHandler(_savedErrorHandler);
    }

    if ((change != null) && !change.isErrorReported() && change instanceof RenameRequest) {
      change.setErrorReported(true);
      MessageHandler.error("Rename failed: ", exception);
    } else {
      // If this is already a dialog reporting an error, and is
      // still visible, then just update the message. Otherwise,
      // create a new dialog to prompt the user for a corrected input.
      if (_isOpenErrorWindow) {
        setMessage(exception.getMessage() + "\n\nPlease enter a new value (or cancel to revert):");
      } else {
        if (change.isErrorReported()) {
          // Error has already been reported.
          return;
        }
        change.setErrorReported(true);
        _query = new PasserelleQuery(_handler, labelProvider);
        _query.setTextWidth(getTextWidth());
        _query._isOpenErrorWindow = true;
        String description = change.getDescription();
        _query.setMessage(exception.getMessage() + "\n\nPlease enter a new value:");
        /*
         * NOTE: The error message used to be more verbose, as follows. But this is intimidating to users.
         * _query.setMessage("Change failed:\n" + description + "\n" + exception.getMessage() +
         * "\n\nPlease enter a new value:");
         */
        // Need to extract the name of the entry from the request.
        // Default value is the description itself.
        // NOTE: This is very fragile... depends on the particular
        // form of the MoML change request.
        String tmpEntryName = description;
        int patternStart = description.lastIndexOf("<property name=\"");
        if (patternStart >= 0) {
          int nextQuote = description.indexOf("\"", patternStart + 16);
          if (nextQuote > patternStart + 15) {
            tmpEntryName = description.substring(patternStart + 16, nextQuote);
          }
        }
        final String entryName = tmpEntryName;
        final Settable attribute = (Settable) _attributes.get(entryName);
        // NOTE: Do this in the event thread, since this might be
        // invoked
        // in whatever thread is processing mutations.
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (attribute != null) {
              _query.addStyledEntry(attribute);
            } else {
              throw new InternalErrorException("Expected attribute attached to entry name: " + entryName);
            }
            _dialog = new ComponentDialog(JOptionPane.getFrameForComponent(PasserelleQuery.this), "Error", _query, null);
            // The above returns only when the modal
            // dialog is closing. The following will
            // force a new dialog to be created if the
            // value is not valid.
            _query._isOpenErrorWindow = false;
            if (_dialog.buttonPressed().equals("Cancel")) {
              if (_revertValue.containsKey(entryName)) {
                String revertValue = (String) _revertValue.get(entryName);
                // NOTE: Do not use setAndNotify() here because
                // that checks whether the string entry has
                // changed, and we want to force revert even
                // if it appears to not have changed.
                set(((NamedObj) attribute).getName(), revertValue);
                changed(entryName);
              }
            } else {
              // Force evaluation to check validity of
              // the entry. NOTE: Normally, we would
              // not need to force evaluation because if
              // the value has changed, then listeners
              // are automatically notified. However,
              // if the value has not changed, then they
              // are not notified. Since the original
              // value was invalid, it is not acceptable
              // to skip notification in this case. So
              // we force it.
              try {
                attribute.validate();
              } catch (IllegalActionException ex) {
                change.setErrorReported(false);
                changeFailed(change, ex);
              }
            }
          }
        });
      }
    }
  }

  class ParameterGroupButton extends Box {
    private EditorFactory dlgFactory = null;

    private final ParameterGroup parameterGroup;

    /**
     * @param name
     * @param label
     * @param paramGroup
     */
    public ParameterGroupButton(String name, String label, ParameterGroup paramGroup) {
      super(BoxLayout.X_AXIS);
      parameterGroup = paramGroup;
      try {
        dlgFactory = new PasserelleEditorFactory(paramGroup, "_editorFactory");
      } catch (NameDuplicationException e) {
        dlgFactory = (EditorFactory) paramGroup.getAttribute("_editorFactory");
      } catch (Exception e) {
        logger.error("Error initializing ParameterGroupButton", e);
      }
      JButton button = new JButton("...");
      button.setPreferredSize(new Dimension(15, 20));
      add(button);
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          dlgFactory.createEditor(parameterGroup, null);
        }
      });
    }
  }

  /**
   * Panel containing an entry box and file chooser.
   */
  class QueryFileChooser extends Box implements ActionListener {
    public QueryFileChooser(String name, String defaultName, URI base, File startingDirectory, FileFilter fileFilter) {
      this(name, defaultName, base, startingDirectory, fileFilter, Color.white, Color.black);
    }

    public QueryFileChooser(String name, String defaultName, URI base, File startingDirectory, FileFilter fileFilter, Color background, Color foreground) {
      super(BoxLayout.X_AXIS);
      _fileFilter = fileFilter;
      _base = base;
      _startingDirectory = startingDirectory;
      _entryBox = new JTextField(defaultName, getTextWidth());
      _entryBox.setBackground(background);
      _entryBox.setForeground(foreground);
      button = new JButton("Browse");
      button.addActionListener(this);
      add(_entryBox);
      add(button);
      // Add the listener last so that there is no notification
      // of the first value.
      _entryBox.addActionListener(new QueryActionListener(name));

      // Add a listener for loss of focus. When the entry gains
      // and then loses focus, listeners are notified of an update,
      // but only if the value has changed since the last notification.
      // FIXME: Unfortunately, Java calls this listener some random
      // time after the window has been closed. It is not even a
      // a queued event when the window is closed. Thus, we have
      // a subtle bug where if you enter a value in a line, do not
      // hit return, and then click on the X to close the window,
      // the value is restored to the original, and then sometime
      // later, the focus is lost and the entered value becomes
      // the value of the parameter. I don't know of any workaround.
      _entryBox.addFocusListener(new QueryFocusListener(name));

      _name = name;
    }

    @Override
    public void setEnabled(boolean enabled) {
      super.setEnabled(enabled);
      _entryBox.setEnabled(enabled);
      button.setEnabled(enabled);
    }

    public void actionPerformed(ActionEvent e) {
      // NOTE: If the last argument is null, then choose a default dir.
      JFileChooser fileChooser = new JFileChooser(_startingDirectory);
      if (_fileFilter != null) {
        fileChooser.setFileFilter(_fileFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);
      } else {
        fileChooser.setAccessory(new FinderAccessory(fileChooser));
      }

      fileChooser.setApproveButtonText("Select");
      // FIXME: The following doesn't have any effect.
      fileChooser.setApproveButtonMnemonic('S');
      int returnValue = fileChooser.showOpenDialog(PasserelleQuery.this);
      if (returnValue == JFileChooser.APPROVE_OPTION) {
        if (_base == null) {
          // Absolute file name.
          try {
            _entryBox.setText(fileChooser.getSelectedFile().getCanonicalPath());
          } catch (IOException ex) {
            // If we can't get a path, then just use the name.
            _entryBox.setText(fileChooser.getSelectedFile().getName());
          }
        } else {
          // Relative file name.
          File selectedFile = fileChooser.getSelectedFile();
          // FIXME: There is a bug here under Windows XP
          // at least... Sometimes, the drive ID (like c:)
          // is lower case, and sometimes it's upper case.
          // When we open a MoML file, it's upper case.
          // When we do "save as", it's lower case.
          // This despite the fact that both use the same
          // file browser to determine the file name.
          // Beats me... Consequence is that if you save as,
          // then the following relativize call doesn't work
          // until you close and reopen the file.
          try {
            selectedFile = selectedFile.getCanonicalFile();
          } catch (IOException ex) {
            // Ignore, since we can't do much about it anyway.
          }
          URI relativeURI = _base.relativize(selectedFile.toURI());
          _entryBox.setText(relativeURI.toString());
        }
        _notifyListeners(_name);
      }
    }

    public String getSelectedFileName() {
      return _entryBox.getText();
    }

    public void setFileName(String name) {
      _entryBox.setText(name);
    }

    private URI _base;

    private JTextField _entryBox;

    private String _name;

    private File _startingDirectory;
    private FileFilter _fileFilter;

    private JButton button;
  }

  /**
   * Panel containing a Date Picker.
   */
  class QueryDatePicker extends DateTimeSelector {
    // private String name;
    private boolean allowChangeNotifications = true;

    /**
     * @param name
     * @param label
     * @param startDate
     * @param background
     * @param foreground
     */
    public QueryDatePicker(String name, String label, Date startDate, Color background, Color foreground) {
      allowChangeNotifications = false;
      init();
      // this.name=name;
      setName(name);
      if (startDate == null) {
        startDate = new Date();
      }
      setDate(startDate);
      allowChangeNotifications = true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
      super.propertyChange(evt);
      if (logger.isDebugEnabled())
        logger.debug(getName() + " : property change evt " + evt);
      if (allowChangeNotifications && (evt.getPropertyName().equals("closeMe") || evt.getPropertyName().equals("time"))) {
        PasserelleQuery.this._notifyListeners(getName());
      }
    }
  }

  public static class RenameRequest extends MoMLChangeRequest {
    private String oldName;
    private String newName;

    public RenameRequest(Object originator, NamedObj context, String request, String oldName, String newName) {
      super(originator, context, request);
      this.oldName = oldName;
      this.newName = newName;
    }

    public String getOldName() {
      return oldName;
    }

    public String getNewName() {
      return newName;
    }
  }

  public boolean hasAutoSync() {
    return true;
  }

  public void closed(String button) {

  }

  public IPasserelleComponent getPasserelleComponent() {
    return this;
  }

  // /////////////////////////////////////////////////////////////////
  // // private variables ////
  // Another dialog used to prompt for corrections to errors.
  private ComponentDialog _dialog;

  // The handler that was specified in the constructors.
  private NamedObj _handler;

  // Indicator that this is an open dialog reporting an erroneous entry.
  private boolean _isOpenErrorWindow = false;

  // Maps an entry name to the attribute that is attached to it.
  // need to access the _attributes map of PtolemyQuery base class,
  // not an overridden one here...
  // private Map _attributes = new HashMap();

  // Background color for string mode edit boxes.
  // private static Color _NOT_OVERRIDDEN_FOREGROUND_COLOR
  // = new Color(200, 10, 10, 255);
  // A query box for dealing with an erroneous entry.
  private PasserelleQuery _query = null;

  // Maps an entry name to the most recent error-free value.
  private Map _revertValue = new HashMap();

  // Saved error handler to restore after change.
  private ErrorHandler _savedErrorHandler = null;

  // Background color for string mode edit boxes.
  private static Color _STRING_MODE_BACKGROUND_COLOR = new Color(230, 255, 255, 255);

  public void addListener(IPasserelleComponentCloseListener closeListener) {
  }

  public Set<ParameterToWidgetBinder> getParameterBindings() {
    return Collections.EMPTY_SET;
  }

}
