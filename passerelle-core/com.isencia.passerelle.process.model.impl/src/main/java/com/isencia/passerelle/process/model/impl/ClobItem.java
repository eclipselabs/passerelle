/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import com.isencia.passerelle.process.model.Identifiable;

/**
 * @author "puidir"
 *
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_CLOBITEM")
public class ClobItem implements Identifiable, Serializable, Cloneable {
	private static final long serialVersionUID = 7731827652936465986L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_clobitem")
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
	private Date creationTS;

	@Version
	private Integer version;
	
	@Column(name = "CLOBDATA", nullable = false, updatable = false)
	@Lob
	private String value;
	
  public static final String _ID = "id";
  public static final String _VALUE = "value";

	public ClobItem() {	
	}
	
	public ClobItem(String value) {
		this.creationTS = new Date();
		this.value = value;
	}
	
	@Override
	public ClobItem clone() throws CloneNotSupportedException {
		return((ClobItem)super.clone());
	}

	public Long getId() {
		return id;
	}

	public Date getCreationTS() {
		return creationTS;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
}
