/**
 * 
 */
package com.isencia.passerelle.process.scheduler.impl;

/**
 * Remark: in Dare, this class was part of bgc.dar.engine.monitoring
 * Remark: in Dare, this was an MBean
 * 
 * @author "puidir"
 *
 */
public class ResourceUsageStatus {

	private String resourceName;
	
	private int nrOfResources;
	private int currentUsed;
	private int maxUsed;
	
	private int pendingQueueSize;
	private int currentPendingCount;
	private int maxPendingCount;

	public ResourceUsageStatus(String resourceName, int nrOfResources,
			int currentUsed, int maxUsed, int pendingQueueSize,
			int currentPendingCount, int maxPendingCount) {
		this.resourceName = resourceName;
		this.nrOfResources = nrOfResources;
		this.currentUsed = currentUsed;
		this.maxUsed = maxUsed;
		this.pendingQueueSize = pendingQueueSize;
		this.currentPendingCount = currentPendingCount;
		this.maxPendingCount = maxPendingCount;
	}
	
	public String getName() {
		return resourceName;
	}
	
	public int getNrOfResources() {
		return nrOfResources;
	}

	public int getCurrentUsed() {
		return currentUsed;
	}

	public int getMaxUsed() {
		return maxUsed;
	}

	public int getPendingQueueSize() {
		return pendingQueueSize;
	}

	public int getCurrentPendingCount() {
		return currentPendingCount;
	}

	public int getMaxPendingCount() {
		return maxPendingCount;
	}
	
	public void setNrOfResources(int nrOfResources) {
		this.nrOfResources = nrOfResources;
	}

	public void setCurrentUsed(int currentUsed) {
		this.currentUsed = currentUsed;
	}

	public void setMaxUsed(int maxUsed) {
		this.maxUsed = maxUsed;
	}

	public void setPendingQueueSize(int pendingQueueSize) {
		this.pendingQueueSize = pendingQueueSize;
	}

	public void setCurrentPendingCount(int currentPendingCount) {
		this.currentPendingCount = currentPendingCount;
	}

	public void setMaxPendingCount(int maxPendingCount) {
		this.maxPendingCount = maxPendingCount;
	}

	public String toString() {
		StringBuilder strB = new StringBuilder("[Resource "+resourceName+" -- Usage Status : ");
		strB.append(" Resource usage Current/Max/Total : "+currentUsed+"/"+maxUsed+"/"+nrOfResources);
		strB.append(" Pending requests Current/Max/Total : "+currentPendingCount+"/"+maxPendingCount+"/"+pendingQueueSize);
		strB.append("]");
		return strB.toString();
	}

}
