package com.isencia.passerelle.process.scheduler.impl;

import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClass;
import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClassifierStrategy;
import com.isencia.passerelle.process.model.Context;

public class SingleClassRequestClassifierStrategy implements TaskClassifierStrategy {

	private final static TaskClass SINGLE_CLASS = new TaskClass("single class", 10);
	
	public TaskClass getClassForTask(Context context) {
		return SINGLE_CLASS;
	}

}
