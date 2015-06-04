package com.isencia.passerelle.project.repository.api;

import java.io.Serializable;

import com.isencia.passerelle.model.Flow;

/**
 * This interface of the root-level entries in the Passerelle Repository, i.e. Projects, hides the generic Repository
 * API, and just exposes the Passerelle/Drools-related concepts. I.e. there is no way to navigate the Package/Asset
 * tree, as there is no reason for it either in the given context.
 * 
 * The only things that are exposed, are Passerelle Flows and Drools Knowledgebases.
 * 
 * @author delerw
 * 
 */
public interface Project extends Serializable {

  MetaData getMetaData();

  String getCode();

  /**
   * @param seqCode
   *          the unique code identifying the flow in the project (also unique within the complete repository??)
   * @return the sequence in this project, for the given flowCode, or null if not found
   */
  Flow getSubModel(String flowCode);

  /**
   * @param flowCode
   *          the unique code identifying the flow in the project (also unique within the complete repository??)
   * @return the sequence in this project, for the given flowCode, or null if not found
   */
  Flow getFlow(String flowCode);

  MetaData getFlowMetaData(String flowCode);

  /**
   * 
   * @return an array of all flows defined within this project
   */
  String[] getAllFlows();

}
