/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.isencia.passerelle.workbench.model.opm;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.NamedObj;

import com.isencia.passerelle.editor.common.model.Link;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Link</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints.phd.opm.model.impl.OPMLinkImpl#getOpd <em>
 * Opd</em>}</li>
 * <li>{@link com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints.phd.opm.model.impl.OPMLinkImpl#getSource
 * <em>Source </em>}</li>
 * <li>{@link com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints.phd.opm.model.impl.OPMLinkImpl#getTarget
 * <em>Target </em>}</li>
 * <li>
 * {@link com.isencia.passerelle.workbench.model.opm.LinkWithBendPoints.phd.opm.model.impl.OPMLinkImpl#getBendpoints
 * <em> Bendpoints</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class LinkWithBendPoints extends EObjectImpl implements OPMLink, Link {
  public static final int OPM_LINK__BENDPOINTS = 3;
  
  private Object _head;

  private Object _tail;

  private ComponentRelation _relation;

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LinkWithBendPoints other = (LinkWithBendPoints) obj;

    if (_head == null && _tail == null) {
      if (other._head != null || other._tail != null) {
        return false;
      }
    } else {
      if (_head == null || _tail == null) {
        if (_head == null) {
          if (!_tail.equals(other._head) && !_tail.equals(other._tail)) {
            return false;
          }
        } else {
          if (!_head.equals(other._head) && !_head.equals(other._tail)) {
            return false;
          }
        }
      } else {
        if (!(_head.equals(other._head) && _tail.equals(other._tail)) && !(_tail.equals(other._head) && _head.equals(other._tail))) {
          return false;
        }
      }
      return true;
    }

    return true;
  }

  public String getTitle() {
    if (_head instanceof NamedObj && _tail instanceof NamedObj) {
      return ((NamedObj) _head).getName() + "_" + ((NamedObj) _tail).getName();
    }
    if (_relation == null)
      return "";
    return _relation.getName();
  }

  public LinkWithBendPoints() {
    super();
  }

  public LinkWithBendPoints(Object head, Object tail, ComponentRelation relation) {
    super();
    _head = head;
    _tail = tail;
    _relation = relation;
  }

  public Object getHead() {
    return _head;
  }

  public ComponentRelation getRelation() {
    return _relation;
  }

  public Object getTail() {
    return _tail;
  }

  public void setHead(Object head) {
    _head = head;
  }

  public void setRelation(ComponentRelation relation) {
    _relation = relation;
  }

  public void setTail(Object tail) {
    _tail = tail;
  }

  public String toString() {

    return "Link" + ((NamedObj) _head).getFullName() + "_" + ((NamedObj) _tail).getFullName() + "_" + ((NamedObj) _relation).getFullName();
  }



  /**
   * The cached value of the '{@link #getBendpoints() <em>Bendpoints</em>}' attribute list. <!-- begin-user-doc --> <!--
   * end-user-doc -->
   * 
   * @see #getBendpoints()
   * @generated
   * @ordered
   */
  protected EList<Point> bendpoints;
  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public EList<Point> getBendpoints() {

     if (bendpoints == null){
       bendpoints = new EDataTypeUniqueEList<Point>(Point.class, this, OPM_LINK__BENDPOINTS);
     }
     return bendpoints;
  }



} // OPMLinkImpl
