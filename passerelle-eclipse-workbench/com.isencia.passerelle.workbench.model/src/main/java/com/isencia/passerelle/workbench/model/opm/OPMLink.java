/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.isencia.passerelle.workbench.model.opm;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.isencia.passerelle.editor.common.model.Link;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Link</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link com.isencia.passerelle.workbench.model.opm.vainolo.phd.opm.model.OPMLink#getOpd <em>Opd</em>}</li>
 * <li>{@link com.isencia.passerelle.workbench.model.opm.vainolo.phd.opm.model.OPMLink#getSource <em>Source</em>}</li>
 * <li>{@link com.isencia.passerelle.workbench.model.opm.vainolo.phd.opm.model.OPMLink#getTarget <em>Target</em>}</li>
 * <li>{@link com.isencia.passerelle.workbench.model.opm.vainolo.phd.opm.model.OPMLink#getBendpoints <em>Bendpoints
 * </em>}</li>
 * </ul>
 * </p>
 * 
 * @see com.isencia.passerelle.workbench.model.opm.vainolo.phd.opm.model.OPMPackage#getOPMLink()
 * @model
 * @generated
 */
public interface OPMLink extends EObject,Link {

  EList<Point> getBendpoints();


} // OPMLink
