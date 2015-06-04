package com.isencia.passerelle.process.model.factory;

import java.util.List;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ResultBlock;

public interface HistoricalDataProvider {

	String ALLOW_HISTORICAL_DATA = "com.isencia.passerelle.process.model.allow.historical.data";
	String RECENT_ONLY = "com.isencia.passerelle.process.model.recent.only";
	String ALLOWED_REQUEST_TYPES = "com.isencia.passerelle.process.model.allowed.requesttypes";
	String ALLOWED_TASK_TYPES = "com.isencia.passerelle.process.model.allowed.tasketypes";
	String ALLOWED_DATA_TYPES = "com.isencia.passerelle.process.model.allowed.datatypes";
	String INCLUDE_REQUEST_ATTRIBUTES = "com.isencia.passerelle.process.model.include.request.attributes";
	String INCLUDE_RESULTBLOCK_ATTRIBUTES = "com.isencia.passerelle.process.model.include.resultblock.attributes";

	void setAllowHistoricalData(Context context, boolean value);

	void setRecentOnly(Context context, boolean value);

	void setAllowedTaskTypes(Context context, String[] value);
	
	void setAllowedRequestTypes(Context context, String[] value);

	void setAllowedDataTypes(Context context, String[] value);

	void setIncludeRequestAttributes(Context context, boolean value);
	
	void setIncludeResultBlockAttributes(Context context, boolean value);

	List<ResultBlock> getResultBlocks(Context context);

	List<Attribute> getRequestAttributes(Context context);
	
	void reset(Context context);
}
