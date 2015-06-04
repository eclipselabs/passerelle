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


import com.isencia.passerelle.actor.Actor;

/**
 * 
 * @author erwin
 */
public class ActorStatistics implements ActorStatisticsMBean, NamedStatistics {
	
	private String actorName;
	
	/**
	 * Some performance statistics. Could be usefull for monitoring purposes.
	 */
	private CycleStatistics cycleStatistics = new CycleStatistics();


	public ActorStatistics(Actor actor) {
		this.actorName=actor.getFullName();
	}
	
	public void beginCycle() {
		cycleStatistics.acceptCycleBegin();
	}
	
	public void endCycle() {
		cycleStatistics.acceptCycleEnd();
	}
	
	public long getNrCycles() {
		return cycleStatistics.getNrCycles();
	}

	public void reset() {
		cycleStatistics.reset();
	}

	public String getName() {
		return actorName;
	}

	public long getAvgCycleTime() {
		try {
			return cycleStatistics.getAvgProcessingTime();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}
	
	public long getAvgIdleTime() {
		try {
			return cycleStatistics.getAvgIdleTime();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}

	public long getMinCycleTime() {
		try {
			return cycleStatistics.getMinProcessingTime();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}
	
	public long getMinIdleTime() {
		try {
			return cycleStatistics.getMinIdleTime();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}

	public long getMaxCycleTime() {
		try {
			return cycleStatistics.getMaxProcessingTime();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}
	
	public long getMaxIdleTime() {
		try {
			return cycleStatistics.getMaxIdleTime();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}
}
