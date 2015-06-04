/* An action for getting documentation.

 Copyright (c) 2006-2008 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package com.isencia.passerelle.actor.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.DocApplicationSpecializer;
import ptolemy.vergil.actor.DocEffigy;
import ptolemy.vergil.actor.DocManager;
import ptolemy.vergil.actor.DocTableau;
import ptolemy.vergil.basic.DocAttribute;
import ptolemy.vergil.basic.KeplerDocumentationAttribute;
import ptolemy.vergil.toolbox.FigureAction;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// GetDocumentationAction

/**
 * This is an action that accesses the documentation for a Ptolemy object
 * associated with a figure. Note that this base class does not put this action
 * in a menu, since some derived classes will not want it. But by having it
 * here, it is available to all derived classes.
 * 
 * This class provides an action for removing instance-specific documentation.
 * 
 * @author Edward A. Lee
 * @version $Id: GetDocumentationAction.java,v 1.41.4.1 2008/03/25 22:24:28 cxh
 *          Exp $
 * @since Ptolemy II 5.2
 * @Pt.ProposedRating Red (eal)
 * @Pt.AcceptedRating Red (johnr)
 */
public class GetDocumentationAction extends FigureAction {

	/**
	 * Construct an instance and give a preference for whether the
	 * KeplerDocumentationAttribute or the docAttribute should be displayed if
	 * both exist.
	 * 
	 * @param docPreference
	 *            0 for docAttribute, 1 for KeplerDocumentationAttribute
	 */
	public GetDocumentationAction(int docPreference) {
		super("Get Documentation");
		this.docPreference = docPreference;
		putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	/** Construct an instance of this action. */
	public GetDocumentationAction() {
		super("Get Documentation");

		putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Perform the action by opening documentation for the target. In the
	 * default situation, the documentation is in doc.codeDoc. 
	 */
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);

		if (_configuration == null) {
			MessageHandler.error("Cannot get documentation without a configuration.");
		}

		NamedObj target = getTarget();
		if (target == null) {
			// Ignore and return.
			return;
		}

		showDocumentation(target);
	}

	/**
	 * Show the documentation for a NamedObj. This does the same thing as the
	 * actionPerformed but without the action handler
	 * 
	 * @param target
	 *            The NamedObj that will have its documentation shown.
	 */
	public void showDocumentation(NamedObj target) {
		if (_configuration == null) {
			MessageHandler.error("Cannot get documentation without a configuration.");
		}

		// If the object contains
		// an attribute named of class DocAttribute or if there
		// is a doc file for the object in the standard place,
		// then use the DocViewer class to display the documentation.
		// For backward compatibility, if neither of these is found,
		// then we open the Javadoc file, if it is found.
		List docAttributes = target.attributeList(DocAttribute.class);
		// check for the KeplerDocumentation attribute
		KeplerDocumentationAttribute keplerDocumentationAttribute = (KeplerDocumentationAttribute) target.getAttribute("KeplerDocumentation");
		int docAttributeSize = docAttributes.size();

		if (docAttributes.size() != 0 && keplerDocumentationAttribute != null) {
			// if there is both a docAttribute and a
			// KeplerDocumentationAttribute
			// use the preference passed in to the constructor
			if (docPreference == 0) {
				keplerDocumentationAttribute = null;
			} else if (docPreference == 1) {
				docAttributeSize = 0;
			}
		}

		if (keplerDocumentationAttribute != null) {
			// use the KeplerDocumentationAttribute
			DocAttribute docAttribute = keplerDocumentationAttribute.getDocAttribute(target);
			if (docAttribute != null) {
				showDocAttributeTableau(docAttribute, target);
			} else {
				throw new InternalErrorException("Error building Kepler documentation");
			}
		} else if (docAttributeSize != 0) {
			// Have a doc attribute. Use that.
			DocAttribute docAttribute = (DocAttribute) docAttributes.get(docAttributes.size() - 1);
			showDocAttributeTableau(docAttribute, target);
		} else {
			// No doc attribute. Try for a doc file.
			String className = target.getClass().getName();
			Effigy context = _configuration.findEffigy(target);
			NamedObj container = target.getContainer();
			while (context == null && container != null) {
				context = Configuration.findEffigy(container);
				container = container.getContainer();
			}
			if (context == null) {
				context = new PtolemyEffigy(_configuration.workspace());
				((PtolemyEffigy)context).setModel(target);
				CompositeEntity directory = _configuration.getDirectory();
				try {
					context.setName(directory.uniqueName(target.getName()));
					context.setContainer(directory);
					context.identifier.setExpression(_effigyIdentifier(context, target));
				} catch (Exception e) {
					MessageHandler.error("Cannot find documentation for " + className, e);
				}
			}
			getDocumentation(_configuration, className, context);
		}
	}

	/**
	 * Get the documentation for a particular class.
	 * <p>
	 * If the configuration has a parameter _docApplicationSpecializer and that
	 * parameter names a class that that implements the
	 * DocApplicationSpecializer interface, then we call docClassNameToURL().
	 * 
	 * <p>
	 * If the documentation is not found, pop up a dialog and ask the user if
	 * they would like to build the documentation, use the website documentation
	 * or cancel. The location of the website documentation is set by the
	 * _remoteDocumentationURLBase attribute in the configuration. That
	 * attribute, if present, should be a parameter that whose value is a string
	 * that represents the URL where the documentation may be found. If the
	 * _remoteDocumentationURLBase attribute is not set, then the location of
	 * the website documentation defaults to
	 * <code>http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII/<i>Major.Version</i></code>
	 * , where <code><i>Major.Version</i></code> is the value returned by
	 * {@link ptolemy.kernel.attributes.VersionAttribute#majorCurrentVersion()}.
	 * 
	 * @param configuration
	 *            The configuration.
	 * @param className
	 *            The dot separated fully qualified name of the class.
	 * @param context
	 *            The context.
	 */
	public void getDocumentation(Configuration configuration, String className, Effigy context) {
		try {

			// Look for the PtDoc .xml file or the javadoc.
			// Don't look for the source or the index.
			URL toRead = DocManager.docClassNameToURL(configuration, className, true, true, false, false);
			if (toRead != null) {
				_lastClassName = null;
				configuration.openModel(null, toRead, toRead.toExternalForm());
			} else {
				Parameter docApplicationSpecializerParameter = (Parameter) configuration.getAttribute("_docApplicationSpecializer", Parameter.class);
				if (docApplicationSpecializerParameter != null) {
					// if there is a docApplicationSpecializer, let it handle
					// the
					// error instead of just throwing the exception
					String docApplicationSpecializerClassName = docApplicationSpecializerParameter.getExpression();
					Class docApplicationSpecializerClass = Class.forName(docApplicationSpecializerClassName);
					final DocApplicationSpecializer docApplicationSpecializer = (DocApplicationSpecializer) docApplicationSpecializerClass.newInstance();
					docApplicationSpecializer.handleDocumentationNotFound(className, context);
				} else {

					throw new Exception("Could not get find documentation for "
							+ className
							+ "."
							+ (DocManager.getRemoteDocumentationURLBase() != null ? " Also tried looking on \"" + DocManager.getRemoteDocumentationURLBase()
									+ "\"." : ""));
				}
			}
		} catch (Exception ex) {
			try {
				// Pop up a query an prompt the user
				String message = "The documentation was not found.";
				Object[] options = new Object[] { "Ok" };
				JOptionPane.showOptionDialog(getFrame(), message, "Documentation not found", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
						options, options[0]);
			} catch (Exception ex2) {
				MessageHandler.error("Cannot find documentation for " + className, ex2);
			}
		}
	}

	/**
	 * Set the configuration. This is used to open files (such as
	 * documentation). The configuration is is important because it keeps track
	 * of which files are already open and ensures that there is only one editor
	 * operating on the file at any one time.
	 * 
	 * @param configuration
	 *            The configuration.
	 */
	public void setConfiguration(Configuration configuration) {
		_configuration = configuration;
	}

	/**
	 * Set the effigy to be used if the effigy is not evident from the model
	 * being edited. This is used if you are showing the documentation from code
	 * that is not in a model.
	 * 
	 * @param effigy
	 *            the effigy to set.
	 */
	public void setEffigy(Effigy effigy) {
		_effigy = effigy;
	}

	// /////////////////////////////////////////////////////////////////
	// // private methods ////

    /** Return an identifier for the specified effigy based on its
     *  container (if any) and its name.
     *  @return An identifier for the effigy.
     */
    private String _effigyIdentifier(Effigy effigy, NamedObj entity) {
        // Set the identifier of the effigy to be that
        // of the parent with the model name appended.
        Effigy parentEffigy=null;
		try {
			parentEffigy = (Effigy)effigy.getContainer();
		} catch (ClassCastException e) {
			// means it's another kind of container, e.g. a Directory
		}
        if (parentEffigy == null) {
            return effigy.getFullName();
        }
        // Note that we add a # the first time, and
        // then add . after that.  So
        // file:/c:/foo.xml#bar.bif is ok, but
        // file:/c:/foo.xml#bar#bif is not
        // If the title does not contain a legitimate
        // way to reference the submodel, then the user
        // is likely to look at the title and use the wrong
        // value if they xml edit files by hand. (cxh-4/02)
        String entityName = parentEffigy.identifier.getExpression();
        String separator = "#";
        if (entityName.indexOf("#") > 0) {
            separator = ".";
        }
        return (entityName + separator + entity.getName());
    }

	/**
	 * Allow optional use of multiple documentation windows when the
	 * _multipleDocumentationAllowed attribute is found in the Configuration.
	 */
	private static boolean isMultipleDocumentationAllowed() {
		// FIXME: This is necessary for Kepler, but not for Ptolemy?
		// Why?
		boolean retVal = false;
		List configsList = Configuration.configurations();
		Configuration config = null;
		Object object = null;
		for (Iterator it = configsList.iterator(); it.hasNext();) {
			config = (Configuration) it.next();
			if (config != null) {
				break;
			}
		}
		if (config == null) {
			throw new KernelRuntimeException("Could not find " + "configuration, list of configurations was " + configsList.size()
					+ " elements, all were null.");
		}
		// Look up the attribute (if it exists)
		StringAttribute multipleDocumentationAllowed = (StringAttribute) config.getAttribute("_multipleDocumentationAllowed");
		if (multipleDocumentationAllowed != null) {
			retVal = Boolean.parseBoolean(multipleDocumentationAllowed.getExpression());
		}
		return retVal;
	}

	/**
	 * Find and show the tableau for a given DocAttribute.
	 * 
	 * @param docAttribute
	 *            the attribute to show
	 * @param taget
	 *            the target of the documentation viewing
	 */
	private void showDocAttributeTableau(DocAttribute docAttribute, NamedObj target) {
		// Need to create an effigy and tableau.
		ComponentEntity effigy = null;
		Effigy context = Configuration.findEffigy(target);
		if (_effigy == null) {
			if (context == null) {
				context = Configuration.findEffigy(target.getContainer());
				if (context == null) {
					MessageHandler.error("Cannot find an effigy for " + target.getFullName());
				}
				effigy = context.getEntity("DocEffigy");
			}
		} else {
			effigy = _effigy;
		}

		if (effigy == null) {
			try {
				effigy = new DocEffigy(context, "DocEffigy");
			} catch (KernelException exception) {
				throw new InternalErrorException(exception);
			}
		}
		if (!(effigy instanceof DocEffigy)) {
			MessageHandler.error("Found an effigy named DocEffigy that " + "is not an instance of DocEffigy!");
		}
		((DocEffigy) effigy).setDocAttribute(docAttribute);
		ComponentEntity tableau = ((Effigy) effigy).getEntity("DocTableau");
		if (tableau == null) {
			try {
				tableau = new DocTableau((DocEffigy) effigy, "DocTableau");

				((DocTableau) tableau).setTitle("Documentation for " + target.getFullName());
			} catch (KernelException exception) {
				throw new InternalErrorException(exception);
			}
		} else {
			if (isMultipleDocumentationAllowed()) {
				try {
					// FIXME: This is necessary for Kepler, but
					// not for Ptolemy? Why?

					// Create a new tableau with a unique name
					tableau = new DocTableau((DocEffigy) effigy, effigy.uniqueName("DocTableau"));
					((DocTableau) tableau).setTitle("Documentation for " + target.getFullName());
				} catch (KernelException exception) {
					MessageHandler.error("Failed to display documentation for " + "\" " + target.getFullName() + "\".", exception);
				}
			}
		}
		if (!(tableau instanceof DocTableau)) {
			MessageHandler.error("Found a tableau named DocTableau that " + "is not an instance of DocTableau!");
		}
		((DocTableau) tableau).show();
	}

	// /////////////////////////////////////////////////////////////////
	// // protected variables ////

	/** The configuration. */
	protected Configuration _configuration;

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	/**
	 * The name of the last class for which we looked. If the user looks again
	 * for the same class and gets an error and remoteDocumentationURLBase is
	 * set, we print a little more information.
	 */
	private static String _lastClassName = null;

	/**
	 * Defines a preference for whether to display kepler documentation or
	 * ptolemy documentation. This can be set in the constructor and it default
	 * to ptolemy. 0 is ptolemy, 1 is kepler.
	 */
	private int docPreference = 0;

	/**
	 * Defines the effigy to use if the effigy is not apparent from the model
	 */
	private Effigy _effigy = null;
}
