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
 * A duplicate of ClobItem, mapped to a separate table, to store very long messages for ContextEvents.
 *
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_CLOBMESSAGE")
public class ClobMessage implements Identifiable, Serializable, Cloneable {
  private static final long serialVersionUID = -5977827216575554188L;

  @Id
	@Column(name = "ID")
	@GeneratedValue(generator = "pas_clobmessage")
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

	public ClobMessage() {	
	}
	
	public ClobMessage(String value) {
		this.creationTS = new Date();
		this.value = value;
	}
	
	@Override
	public ClobMessage clone() throws CloneNotSupportedException {
		return((ClobMessage)super.clone());
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
