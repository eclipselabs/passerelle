/**
 * 
 */
package com.isencia.passerelle.process.scheduler.impl;

import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClass;

/**
 * @author "puidir"
 *
 */
public class PrioritizedTaskClass {

	private TaskClass entityClass;
	private Double priorityScore;

	/**
	 * @param entityClass
	 * @param priorityScore
	 */
	public PrioritizedTaskClass(TaskClass taskClass, double priorityScore) {
		this.entityClass = taskClass;
		this.priorityScore = priorityScore;
	}

	public TaskClass getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(TaskClass requestClass) {
		this.entityClass = requestClass;
	}

	public Double getPriorityScore() {
		return priorityScore;
	}

	public void setPriorityScore(double priorityScore) {
		this.priorityScore = priorityScore;
	}

	@Override
	public String toString() {
		return "[PrioritizedLifeCycleEntityClass : entityClass : " + entityClass + " priorityScore" + priorityScore + " ]";
	}

	/**
	 * Does a "largest priorityScore first" order, i.e. inverse semantics as
	 * a normal numerical compareTo would do.
	 * <br>
	 * For identical scores, enforces an alphabetical order on the name.
	 */
	public int compareTo(PrioritizedTaskClass o) {
		if (o == null || o.priorityScore == null) {
			return -1;
		}
		if (priorityScore == null) {
			return 1;
		}
		int result = o.priorityScore.compareTo(priorityScore);
		if (result != 0) {
			return getEntityClass().getName().compareTo(o.getEntityClass().getName());
		} else {
			return result;
		}
	}
}
