package com.isencia.passerelle.process.model.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Task;

public class TaskImpl extends RequestImpl implements Task {

	private static final long serialVersionUID = 1L;

	private ContextImpl parentContext;

	private Set<ResultBlock> resultBlocks = new HashSet<ResultBlock>();

	public static final String _PARENT_CONTEXT = "parentContext";
	public static final String _RESULT_BLOCKS = "resultBlocks";
	public static final String _RESULT_ITEMS = "resultItems";

	public TaskImpl() {
	}

	public TaskImpl(String initiator, String type) {
		super(initiator, type);
	}

	public TaskImpl(Context parentContext, String initiator, String type) {
		super(parentContext.getRequest().getCase(), initiator, type);
		this.parentContext = (ContextImpl) parentContext;

		this.parentContext.addTask(this);
	}

	public Context getParentContext() {
		return parentContext;
	}

	public boolean addResultBlock(ResultBlock block) {
		return resultBlocks.add(block);
	}

	public Collection<ResultBlock> getResultBlocks() {
		// TODO this was unmodifiable set but this gave difficulties for sherpa
		// when used to show resultBlocks
		return resultBlocks;
	}

	public Set<ResultItem> getResultItems() {

		return null;
	}

	public ResultBlock getResultBlock(String type) {
		for (ResultBlock block : resultBlocks) {
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

  @Override
  public String toString() {
    return "TaskImpl [id=" + getId() + ", correlationId=" + getCorrelationId()
        + ", type=" + getType() + ", initiator=" + getInitiator() + ", executor=" + getExecutor() 
        + ", \n attributes=" + getAttributes() +", \n resultBlocks=" + resultBlocks + "]";
  }
}
