/**
 * 
 */
package com.isencia.passerelle.process.scheduler.congestionmanagement;

import java.io.Serializable;

/**
 * Representation of a categorisation of tasks
 * according to which resources will be dimensioned and assigned.
 * 
 * @author "erwin"
 *
 */
public class TaskClass implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	/**
	 * <p>
	 * An indication of the fraction of the available resources
	 * that should typically be assigned for processing entities
	 * that belong to this class instance.
	 * </p>
	 * <p>
	 * Typically this is given as an integer percentage, i.e.
	 * the total of all RCAs of all RequestClasses for the linked
	 * resource type should be 100.
	 * (Although this is not a hard requirement, it's primarily an easy-to-manage approach.)
	 * </p>
	 */
	private int relativeCapacityAssignment;
	
	public TaskClass(String name) {
		this.name = name;
	}

	public TaskClass(String name, int relativeCapacityAssignment) {
		this.name = name;
		this.relativeCapacityAssignment = relativeCapacityAssignment;
	}

	public int getRelativeCapacityAssignment() {
		return relativeCapacityAssignment;
	}

	public void setRelativeCapacityAssignment(int relativeCapacityAssignment) {
		this.relativeCapacityAssignment = relativeCapacityAssignment;
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "[TaskClass : name: " + name + " rca: " + relativeCapacityAssignment+"]";
	}

}
