package com.isencia.passerelle.core;

import java.util.Date;
import java.util.Hashtable;

public interface IEventLog {
	String getCategory();
	String getClassName();
	Date getDate();
	String getFileName();
	Integer getLineNumber();
	String getLocation();
	Hashtable<String,String> getMdc();
	String getMessage();
	String getMethodName();
	Integer getMilliSeconds();
	String getNdc();
	String getThreadName();
	String getSessionId();
	String getStatus();
}
