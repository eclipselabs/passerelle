/**
 * 
 */
package com.isencia.passerelle.project.repository.api;

import java.io.File;
import java.util.List;

import ptolemy.actor.CompositeActor;

import com.isencia.passerelle.core.IEventLog;
import com.isencia.passerelle.model.Flow;

/**
 * Entry point for the Sherpa Repository, when applied for the 3-level structure that is defined for
 * Passerelle&Drools-related assets :
 * <ul>
 * <li>Project
 * <li>Package : set of assets; always contains a set of Drools rules files, and already the serialized resulting
 * Knowledgebase
 * <li>Asset : can be a Passerelle Flow or a Drools rules file
 * </ul>
 * 
 * The Passerelle Repository API hides the underlying 3-level structure, and just exposes Projects, that can contain
 * Passerelle Flows and Drools pre-built KnowledgeBases.
 * 
 * @author delerw
 * 
 */
public interface RepositoryService {

  String PROJECT_ROOT = "com.isencia.passerelle.project.root";
  String SUBMODEL_ROOT = "com.isencia.passerelle.submodel.root";
  String REFERENCE = "REFERENCE";
  String FLOW_NAME = "flowname";
  String SUBMODEL_NAME = "submodelname";
  String USER_ID = "userID";
  String JOB_ID = "jobID";

  void commitFlow(Flow flow, String comment) throws Exception;

  void createSubmodel(CompositeActor flow);

  void deleteSubmodel(String flow);

  /**
   * 
   * @param flowCode
   * @return exists a new version of the project in the repository
   */
  boolean existNewSubModel(String flowCode);

  /**
   * 
   * @return an array of the metadata for all submodels in the Repository, this avoids double call
   */
  MetaData[] getAllSubmodelMetaData();

  /**
   * 
   * @return an array of the codes for all submodels in the Repository
   */
  String[] getAllSubmodels();

  /**
   * 
   * @param flowCode
   * @return the flow for the given flowCode, or null if not found
   */
  Flow getFlow(String flowCode);

  /**
   * 
   * @param flowCode
   * @return the metadata for the given flow, or null if not found
   */
  MetaData getFlowMetaData(String flowCode);

  List<IEventLog> getLogs(String name, Integer maxResult);

  /**
   * 
   * @param projectCode
   * @return the project for the given projectCode, or null if not found
   */
  Project getProject(String projectCode);

  /**
   * 
   * @param submodelCode
   * @return the submodel for the given flowCode, or null if not found
   */
  Flow getSubmodel(String flowCode);

  File getSubmodelFolder();

  /**
   * 
   * @param flowCode
   * @return the metadata for the given submodel, or null if not found
   */
  MetaData getSubmodelMetaData(String flowCode);

  void setSubmodelFolder(File folder);

}
