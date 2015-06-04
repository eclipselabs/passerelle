package com.isencia.passerelle.process.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.impl.util.ProcessUtils;

@Cacheable(false)
@Entity
@DiscriminatorValue("TASK")
public class TaskImpl extends RequestImpl implements Task {

	private static final long serialVersionUID = 1L;

	// Remark: need to use the implementation class instead of the interface
	// here to ensure jpa implementations like EclipseLink will generate setter
	// methods
	@ManyToOne(targetEntity = ContextImpl.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "PARENT_CONTEXT_ID", nullable = false, updatable = true)
	private ContextImpl parentContext;

	@OneToMany(targetEntity = ResultBlockImpl.class, mappedBy = "task", fetch = FetchType.LAZY)
	private Set<ResultBlock> resultBlocks = ProcessUtils.emptySet();

	public static final String _PARENT_CONTEXT = "parentContext";
	public static final String _RESULT_BLOCKS = "resultBlocks";
	public static final String _RESULT_ITEMS = "resultItems";

	public TaskImpl() {
	}

	public TaskImpl(Context parentContext, String initiator, String type) {
		super(parentContext.getRequest().getCase(), initiator, type);
		this.parentContext = (ContextImpl) parentContext;

		this.parentContext.addTask(this);
	}

	public ContextImpl getParentContext() {
		return parentContext;
	}
	
	public void setParentContext(ContextImpl parentContext) {
		this.parentContext = parentContext;
	}

	public boolean addResultBlock(ResultBlock block) {
		if (!ProcessUtils.isInitialized(resultBlocks))
			initializeResultBlocks();
		return resultBlocks.add(block);
	}

	public Set<ResultBlock> getResultBlocks() {
		if (!ProcessUtils.isInitialized(resultBlocks)) {
			return resultBlocks;
		}
			
		// TODO check if returning an unmodifiable collection still gives difficulties for sherpa when used to show resultBlocks
		return Collections.unmodifiableSet(resultBlocks);
	}

	// this is a utility getter for sherpa
	@OneToMany(targetEntity = ResultItemImpl.class, mappedBy = "resultBlock.task")
	public Set<ResultItem> getResultItems() {
		return null;
	}

	public ResultBlock getResultBlock(String type) {
		for (ResultBlock block : getResultBlocks()) {
			if (type.equals(block.getType())) {
				return block;
			}
		}
		return null;
	}

	@SuppressWarnings("all")
	public int hashCode() {
		return new HashCodeBuilder(31, 71).append(getId()).append(getType()).toHashCode();
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof TaskImpl)) {
			return false;
		}
		TaskImpl rhs = (TaskImpl) arg0;
		return new EqualsBuilder().append(this.getId(), rhs.getId()).append(this.getType(), rhs.getType()).isEquals();
	}
	
	public void initializeResultBlocks() {
    resultBlocks = new HashSet<ResultBlock>();
	}
}
