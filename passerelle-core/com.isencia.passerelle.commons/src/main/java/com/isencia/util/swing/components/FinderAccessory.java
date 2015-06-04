/* Copyright 2011 - iSencia Belgium NV

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
package com.isencia.util.swing.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

public class FinderAccessory extends JPanel {
  /**
   * Label for this accessory.
   */
  static public final String ACCESSORY_NAME = "Custom filter";

  /**
   * Parent JFileChooser component
   */
  protected JFileChooser chooser = null;

  protected FormField valueField = null;

  private ExtensionFileFilter customFilter = null;

  /**
   * @param parent JFileChooser containing this accessory
   */
  public FinderAccessory(JFileChooser parent) {
    setBorder(new TitledBorder(ACCESSORY_NAME));
    setLayout(new BorderLayout());

    Box formBox = Box.createVerticalBox();
    valueField = new FormField();
    valueField.setEditable(true);
    JLabel extLbl = new JLabel("Selected extensions");
    extLbl.setToolTipText("Enter one or more file extensions, separated by spaces. E.g. [txt xml java]");
    formBox.add(new FormEntry(extLbl, valueField));
    add(formBox, BorderLayout.NORTH);

    valueField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pushExtensions();
      }
    });
    chooser = parent;
  }

  protected void pushExtensions() {
    String extText = valueField.getText();
    if (extText != null) {
      extText = extText.trim();
      if (extText.length() > 0) {
        String[] exts = extText.split("[ ,.]");
        if (customFilter != null) {
          chooser.removeChoosableFileFilter(customFilter);
          chooser.setFileFilter(null);
        }
        customFilter = new ExtensionFileFilter();
        for (String ext : exts) {
          if (ext != null && ext.length() > 0) customFilter.addExtension(ext);
        }
        customFilter.setDescription("Custom filter");
        chooser.addChoosableFileFilter(customFilter);
        chooser.setFileFilter(customFilter);
      }
    }
  }

  /**
   * Set parent's current directory to the parent folder of the specified file
   * and select the specified file. This method is invoked when the user double
   * clicks on an item in the results list.
   * 
   * @param f File to select in parent JFileChooser
   */
  public void goTo(File f) {
    if (f == null) return;
    if (!f.exists()) return;
    if (chooser == null) return;

    // Make sure that files and directories can be displayed
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    // Make sure that parent file chooser will show the type of file
    // specified
    javax.swing.filechooser.FileFilter filter = chooser.getFileFilter();
    if (filter != null) {
      if (!filter.accept(f)) {
        // The current filter will not display the specified file.
        // Set the file filter to the built-in accept-all filter (*.*)
        javax.swing.filechooser.FileFilter all = chooser.getAcceptAllFileFilter();
        chooser.setFileFilter(all);
      }
    }

    // Tell parent file chooser to display contents of parentFolder.
    // Prior to Java 1.2.2 setSelectedFile() did not set the current
    // directory the folder containing the file to be selected.
    File parentFolder = f.getParentFile();
    if (parentFolder != null) chooser.setCurrentDirectory(parentFolder);

    // Nullify the current selection, if any.
    // Why is this necessary?
    // Emperical evidence suggests that JFileChooser gets "sticky" (i.e. it
    // does not always relinquish the current selection). Nullifying the
    // current selection seems to yield better results.
    chooser.setSelectedFile(null);

    // Select the file
    chooser.setSelectedFile(f);

    // Refresh file chooser display.
    // Is this really necessary? Testing on a variety of systems with
    // Java 1.2.2 suggests that this helps. Sometimes it doesn't work,
    // but it doesn't do any harm.
    chooser.invalidate();
    chooser.repaint();
  }

  /**
   * A convenience implementation of FileFilter that filters out all files
   * except for those type extensions that it knows about. Extensions are of the
   * type ".foo", which is typically found on Windows and Unix boxes, but not on
   * Macintosh. Case is ignored. Extension - create a new filter that filters
   * out all files but gif and jpg image files: JFileChooser chooser = new
   * JFileChooser(); ExtensionFileFilter filter = new ExtensionFileFilter( new
   * String{"gif", "jpg"}, "JPEG & GIF Images")
   * chooser.addChoosableFileFilter(filter); chooser.showOpenDialog(this);
   * 
   * @version 1.7 04/23/99
   * @author Jeff Dinkins
   */
  static class ExtensionFileFilter extends FileFilter {

    private Map filters = new HashMap();
    private String description = null;
    private String fullDescription = null;
    private boolean useExtensionsInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all files are
     * accepted.
     * 
     * @see #addExtension(String)
     */
    public ExtensionFileFilter() {
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new ExtensionFileFilter("jpg");
     * 
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String extension) {
      this(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type. Example: new
     * ExtensionFileFilter("jpg", "JPEG Image Images"); Note that the "." before
     * the extension is not needed. If provided, it will be ignored.
     * 
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String extension, String description) {
      this(new String[] { extension }, description);
    }

    /**
     * Creates a file filter from the given string array. Example: new
     * ExtensionFileFilter(String {"gif", "jpg"}); Note that the "." before the
     * extension is not needed and will be ignored.
     * 
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String[] filters) {
      this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new ExtensionFileFilter(String {"gif", "jpg"},
     * "Gif and JPG Images"); Note that the "." before the extension is not
     * needed and will be ignored.
     * 
     * @see #addExtension(String)
     */
    public ExtensionFileFilter(String[] filters, String description) {
      this.filters = new Hashtable(filters.length);
      for (int i = 0; i < filters.length; i++) {
        // add filters one by one
        addExtension(filters[i]);
      }
      setDescription(description);
    }

    /**
     * Return true if this file should be shown in the directory pane, false if
     * it shouldn't. Files that begin with "." are ignored.
     * 
     * @see #getExtension(File)
     * @see FileFilter#accept(File)
     */
    public boolean accept(File f) {
      if (f != null) {
        if (f.isDirectory()) {
          return true;
        }
        String extension = getExtension(f);
        if (extension != null && filters.get(getExtension(f)) != null) {
          return true;
        }
        ;
      }
      return false;
    }

    /**
     * Return the extension portion of the file's name .
     */
    public String getExtension(File f) {
      if (f != null) {
        String filename = f.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
          return filename.substring(i + 1).toLowerCase();
        }
        ;
      }
      return null;
    }

    /**
     * Adds a filetype "dot" extension to filter against. For example: the
     * following code will create a filter that filters out all files except
     * those that end in ".jpg" and ".tif": ExtensionFileFilter filter = new
     * ExtensionFileFilter(); filter.addExtension("jpg");
     * filter.addExtension("tif"); Note that the "." before the extension is not
     * needed and will be ignored.
     */
    public void addExtension(String extension) {
      if (filters == null) {
        filters = new Hashtable(5);
      }
      filters.put(extension.toLowerCase(), this);
      fullDescription = null;
    }

    public void clearExtensions() {
      filters.clear();
    }

    /**
     * Returns the human readable description of this filter. For example:
     * "JPEG and GIF Image Files (*.jpg, *.gif)"
     * 
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     * @see FileFilter#getDescription()
     */
    public String getDescription() {
      if (fullDescription == null) {
        if (description == null || isExtensionListInDescription()) {
          if (description != null) {
            fullDescription = description;
          }
          fullDescription += " (";
          // build the description from the extension list
          Iterator extensions = filters.keySet().iterator();
          if (extensions != null) {
            fullDescription += "." + (String) extensions.next();
            while (extensions.hasNext()) {
              fullDescription += ", " + (String) extensions.next();
            }
          }
          fullDescription += ")";
        } else {
          fullDescription = description;
        }
      }
      return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For example:
     * filter.setDescription("Gif and JPG Images");
     * 
     * @see #setDescription(String)
     * @see #setExtensionListInDescription(boolean)
     * @see #isExtensionListInDescription()
     */
    public void setDescription(String description) {
      this.description = description;
      fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should show up in
     * the human readable description. Only relevant if a description was
     * provided in the constructor or using setDescription();
     * 
     * @see #getDescription()
     * @see #isExtensionListInDescription()
     */
    public void setExtensionListInDescription(boolean b) {
      useExtensionsInDescription = b;
      fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should show up in
     * the human readable description. Only relevant if a description was
     * provided in the constructor or using setDescription();
     * 
     * @see #getDescription()
     */
    public boolean isExtensionListInDescription() {
      return useExtensionsInDescription;
    }

    /**
     * Return a string description of this filter.
     * 
     * @see #getDescription()
     */
    public String toString() {
      return getDescription();
    }
  }

}
