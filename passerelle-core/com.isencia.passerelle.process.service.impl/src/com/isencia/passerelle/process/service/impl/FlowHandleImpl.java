package com.isencia.passerelle.process.service.impl;

import java.net.URI;

import com.isencia.passerelle.actor.FlowUtils;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

public class FlowHandleImpl implements FlowHandle {
	private static final long serialVersionUID = 1L;

	private URI resourceLocator;
	private String code;
	private VersionSpecification version;
	private String rawFlowDefinition;
	private transient Flow flow;
	
	public FlowHandleImpl() {
	}
	
	public FlowHandleImpl(Flow flow) {
		code = flow.getName();
	    if (code.contains(FlowUtils.FLOW_SEPARATOR)) {
	        code = code.split(FlowUtils.FLOW_SEPARATOR)[0];
	    }
		
		this.flow = flow;
	}
	
	@Override
	public String getCode() {
		return code;
	}

	@Override
	public Flow getFlow() {
		return flow;
	}

	@Override
	public String getRawFlowDefinition() {
		return rawFlowDefinition;
	}

	@Override
	public URI getResourceLocation() {
		return resourceLocator;
	}

	@Override
	public VersionSpecification getVersion() {
		return version;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public void setFlow(Flow flow) {
		this.flow = flow;
	}
	
	public void setRawFlowDefinition(String rawFlowDefinition) {
		this.rawFlowDefinition = rawFlowDefinition;
	}
	
	public void setResourceLocator(URI resourceLocator) {
		this.resourceLocator = resourceLocator;
	}
	
	public void setVersion(VersionSpecification version) {
		this.version = version;
	}
}
