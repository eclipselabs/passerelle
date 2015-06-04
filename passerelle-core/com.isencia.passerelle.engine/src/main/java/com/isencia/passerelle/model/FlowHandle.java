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

import java.net.URL;
import java.util.Date;

/**
 * Represents a handle to a (typically remote) flow/model resource.
 * 
 * Depending on the amount of info already available, this can contain different things :
 * <ul>
 * <li>id : a PK uniquely identifying the resource in some persistent storage
 * <li>name : a more-or-less user-friendly name of the model/flow
 * <li>authorativeResourceLocation : a REST URL pointing to a remote resource location 
 * (normally on a passerelle manager), or a local file:// url
 * </ul>
 * 
 * Remark that in an environment with multiple resource "stores", e.g. multiple Passerelle Manager
 * installations, the id is typically not globally unique. <br/>
 * Only the authorativeResourceLocation can then be considered as a truly unique identifier.
 * 
 * @author erwin
 *
 */
public class FlowHandle {
	
	private Long id;
	private String code;
	private String name;
	private URL authorativeResourceLocation;
	private String moml;
	private Date creationTS;
	
	public Date getCreationTS() {
    return creationTS;
  }

  /**
	 * if the handle relates to a flow that is executing (at least at the moment 
	 * when the handle is constructed), the UUID identifying the execution instance
	 * is stored in execId.
	 * For the moment this is only relevant for flows executing in a Passerelle Manager server,
	 * in which case it corresponds to the ID of the ScheduledJob for the corresponding flow.
	 */
	private String execId;
	private URL execResourceLocation;
	
	private Flow localFlow;
	
	public Flow getLocalFlow() {
    return localFlow;
  }

  public void setLocalFlow(Flow localFlow) {
    this.localFlow = localFlow;
  }

  /**
	 * 
	 * @param id
	 * @param name
	 * @param authorativeResourceLocation
	 */
	public FlowHandle(Long id, String name, URL authorativeResourceLocation) {
		this.id = id;
		this.code = name;
		this.name = name;
		this.authorativeResourceLocation = authorativeResourceLocation;
		this.creationTS = new Date();
	}
	
	/**
	 * FlowHandle for a locally created Flow instance.
	 * It takes the flow's full name as the handle's name.
	 * 
	 * @param id
	 * @param flow should be not-null!
	 * @param authorativeResourceLocation
	 */
  public FlowHandle(Long id, Flow flow, URL authorativeResourceLocation) {
    this.id = id;
    this.code = name;
    this.localFlow = flow;
    this.name = flow.getFullName();
    this.authorativeResourceLocation = authorativeResourceLocation;
    this.creationTS = new Date();
  }
  
	void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @param id
	 * @param code
	 * @param name
	 * @param authorativeResourceLocation
	 */
	public FlowHandle(Long id, String code, String name, URL authorativeResourceLocation) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.authorativeResourceLocation = authorativeResourceLocation;
	}
	
	/**
	 * Determines whether this handle points to a remote location,
	 * with the assumption that this implies a http(s) URL as
	 * authorativeResourceLocation
	 * 
	 * @return
	 */
	public boolean isRemote() {
		if(authorativeResourceLocation==null)
			return false;
		else {
			String protocol = authorativeResourceLocation.getProtocol();
			if(protocol==null) {
				return false;
			}
			return protocol.startsWith("http");
		}
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the asset code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the authorativeResourceLocation
	 */
	public URL getAuthorativeResourceLocation() {
		return authorativeResourceLocation;
	}
	
	public String getExecId() {
		return execId;
	}

	public void setExecId(String execId) {
		this.execId = execId;
	}

	public URL getExecResourceLocation() {
		return execResourceLocation;
	}

	public void setExecResourceLocation(URL execResourceLocation) {
		this.execResourceLocation = execResourceLocation;
	}

	/**
	 * @return the moml
	 */
	public String getMoml() {
		return moml;
	}

	/**
	 * @param moml the moml to set
	 */
	public void setMoml(String moml) {
		this.moml = moml;
	}
	
	// hashCode and equals only check the underlying model name

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlowHandle other = (FlowHandle) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[FlowHandle id:" + id + " code:" + code + " name:" + name + " url:"
				+ authorativeResourceLocation + "execId: "
				+ execResourceLocation + "]";
	}

}
