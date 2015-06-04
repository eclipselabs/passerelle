/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.ResultBlock;

/**
 * @author "puidir"
 * 
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_RESULTBLOCKATTRIBUTE")
public class ResultBlockAttributeImpl extends AttributeImpl implements Comparable<ResultBlockAttributeImpl> {
  private static final long serialVersionUID = 1L;

  public static final String SCOPE_RESULTBLOCK = "resultblock";

  @Id
  @Column(name = "ID")
  @GeneratedValue(generator = "pas_resultblockattribute")
  private Long id;

  // Remark: need to use the implementation class instead of the interface
  // here to ensure jpa implementations like EclipseLink will generate setter methods
  @ManyToOne
  @JoinColumn(name = "RESULTBLOCK_ID")
  private ResultBlockImpl resultBlock;

  public ResultBlockAttributeImpl() {
  }

  public ResultBlockAttributeImpl(ResultBlock resultBlock, String name, String value) {
    super(name, value);
    this.resultBlock = (ResultBlockImpl) resultBlock;
    if (this.resultBlock != null) {
      this.resultBlock.putAttribute(this);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.Identifiable#getId()
   */
  public Long getId() {
    return id;
  }

  public ResultBlockImpl getResultBlock() {
    return resultBlock;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(ResultBlockAttributeImpl rhs) {
    return new CompareToBuilder().append(id, rhs.id).append(version, rhs.version).toComparison();
  }

  public String getScope() {
    return SCOPE_RESULTBLOCK;
  }

  @SuppressWarnings("all")
  public int hashCode() {
    return new HashCodeBuilder(31, 71).append(id).append(getName()).append(getValueAsString()).toHashCode();
  }

  @Override
  public boolean equals(Object arg0) {
    if (!(arg0 instanceof ResultBlockAttributeImpl)) {
      return false;
    }
    ResultBlockAttributeImpl rhs = (ResultBlockAttributeImpl) arg0;
    return new EqualsBuilder().append(this.id, rhs.id).append(this.getName(), rhs.getName()).append(this.getValueAsString(), rhs.getValueAsString()).isEquals();
  }

}
