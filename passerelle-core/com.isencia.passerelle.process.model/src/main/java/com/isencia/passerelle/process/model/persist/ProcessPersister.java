package com.isencia.passerelle.process.model.persist;

import com.isencia.passerelle.process.model.Case;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;

public interface ProcessPersister {
  /** 
   * opens a unit of work, and starts a transaction if required.
   */
  boolean open(boolean transactional) throws PersistenceException;
	/**
	 * closes a unit of work, committing or rolling back the transaction if one is active. 
	 */
	void close();

	Case getCase(Long id) throws PersistenceException;
	ContextEvent getContextEvent(Request request, Long id) throws PersistenceException;
	Request getRequest(Case caze, Long id) throws PersistenceException;
	Request getRequestWithTasks(Case caze, Long id) throws PersistenceException;
	Task getTask(Request request, Long id) throws PersistenceException;
	Task getTaskWithResults(Request request, Long id) throws PersistenceException;
	void persistAttributes(Request request) throws PersistenceException;
	void persistCase(Case caze) throws PersistenceException;
	void persistContextEvent(ContextEvent event) throws PersistenceException;
	void persistRequest(Request request) throws PersistenceException;
	void persistResultBlocks(ResultBlock... resultBlocks) throws PersistenceException;
	void persistTask(Task task) throws PersistenceException;
	void updateResultBlock(ResultBlock resultBlock) throws PersistenceException;
	void updateResultBlock(ResultBlock resultBlock, ResultBlock oldBlock) throws PersistenceException;
	void updateStatus(Request request) throws PersistenceException;
  void updateExecutor(Request request) throws PersistenceException;
}
