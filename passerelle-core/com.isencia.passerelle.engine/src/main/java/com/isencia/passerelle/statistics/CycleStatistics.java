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
package com.isencia.passerelle.statistics;

import java.util.Date;


/**
 * A container for counting and timing information about (processing) cycles.
 * <br>
 * <b>Limited to sequential cycles, no support for parallel/nested cycles!</b>
 * <p>
 * For the moment, this class is only meant for usage in this package.
 * Actual "public" statistics are provided by specific wrapper classes,
 * related to actual passerelle components (actors, ports,...).
 * <p>
 * <b>Implementation not:</b> the current implementation assumes the thing is in the 
 * <i>idle</i> state when a CycleStatistics instance is created. 
 * So, the time interval from instance construction time until the first call to acceptCycleBegin()
 * is considered as the first idle period.
 * 
 * @author erwin
 *
 */
final class CycleStatistics {
	
	// boolean flag to maintain whether we're
	// in a cycle (idle==false) or in-between cycles (idle==true)
	private boolean idle=true;
	
	private Date lastCycleStartTime;
	private Date lastCycleEndTime=new Date();
	
	private StatisticalLongData idleData = new StatisticalLongData();
	private StatisticalLongData cycleData = new StatisticalLongData();
	
	/**
	 * Loose all statistical data.
	 *
	 */
	public void reset() {
		lastCycleStartTime=null;
		lastCycleEndTime=new Date();
		idle=true;
		idleData.reset();
		cycleData.reset();
	}
	
	/**
	 * 
	 * @throws IllegalStateException
	 */
	public void acceptCycleBegin() throws IllegalStateException {
		if(!idle)
			throw new IllegalStateException("Not idle");
		idle=false;
		lastCycleStartTime = new Date();
		idleData.acceptData(lastCycleStartTime.getTime() - lastCycleEndTime.getTime());
	}
	
	/**
	 * 
	 * @throws IllegalStateException
	 */
	public void acceptCycleEnd() throws IllegalStateException {
		if(idle)
			throw new IllegalStateException("Idle");
		idle=true;
		lastCycleEndTime = new Date();
		cycleData.acceptData(lastCycleEndTime.getTime()-lastCycleStartTime.getTime());
	}
	
	public long getAvgIdleTime() throws InsufficientDataException {
		return idleData.getAvgData();
	}
	
	public long getMinIdleTime() throws InsufficientDataException {
		return idleData.getMinData();
	}
	
	public long getMaxIdleTime() throws InsufficientDataException {
		return idleData.getMaxData();
	}
	
	public long getAvgProcessingTime() throws InsufficientDataException {
		return cycleData.getAvgData();
	}
	
	public long getMinProcessingTime() throws InsufficientDataException {
		return cycleData.getMinData();
	}
	
	public long getMaxProcessingTime() throws InsufficientDataException {
		return cycleData.getMaxData();
	}
	
	public long getNrCycles() {
		return cycleData.getCount();
	}

}