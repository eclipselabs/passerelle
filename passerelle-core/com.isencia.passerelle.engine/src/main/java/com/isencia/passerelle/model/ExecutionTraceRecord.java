/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.model;

import java.util.Date;

public class ExecutionTraceRecord {
	
	private Long id;
	private String execId;
	private String execName;
	private String source;
	private Date timeStamp;
	private String message;
	
	public ExecutionTraceRecord(Long id, String execId, String execName,
			String source, String message, Date timeStamp) {
		this.id = id;
		this.execId = execId;
		this.execName = execName;
		this.source = source;
		this.message = message;
		this.timeStamp = timeStamp;
	}

	public Long getId() {
		return id;
	}

	public String getExecId() {
		return execId;
	}

	public String getExecName() {
		return execName;
	}

	public String getSource() {
		return source;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExecutionTraceRecord [id=");
		builder.append(id);
		builder.append(", execId=");
		builder.append(execId);
		builder.append(", execName=");
		builder.append(execName);
		builder.append(", source=");
		builder.append(source);
		builder.append(", message=");
		builder.append(message);
		builder.append(", timeStamp=");
		builder.append(timeStamp);
		builder.append("]");
		return builder.toString();
	}
}
