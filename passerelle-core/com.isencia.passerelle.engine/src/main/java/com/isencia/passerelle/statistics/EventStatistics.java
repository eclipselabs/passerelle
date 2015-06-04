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
 * A container for counting and timing information about arbitrary
 * actions/events/....
 * 
 * For the moment, this class is only meant for usage in this package.
 * Actual "public" statistics are provided by specific wrapper classes,
 * related to actual passerelle components (actors, ports,...).
 * 
 * @author erwin
 *
 */
final class EventStatistics {
	
	private Date lastTime=new Date();
	private StatisticalLongData statData = new StatisticalLongData();
	
	/**
	 * Add a new event, and recalculate all interval data.
	 *
	 */
	public void acceptEvent(Object event) {
		Date time = new Date();
		statData.acceptData(time.getTime() - lastTime.getTime());
		lastTime = time;
	}
	
	/**
	 * Loose all statistical data and start all-over again.
	 *
	 */
	void reset() {
		lastTime=new Date();
		statData.reset();
	}

	/**
	 * @return Returns the avgInterval.
	 * @throws InsufficientDataException 
	 */
	public long getAvgInterval() throws InsufficientDataException {
		return statData.getAvgData();
	}
	
	/**
	 * @return Returns the lastTime.
	 */
	public Date getLastTime() {
		return lastTime;
	}
	
	/**
	 * @return Returns the maxInterval.
	 * @throws InsufficientDataException 
	 */
	public long getMaxInterval() throws InsufficientDataException {
		return statData.getMaxData();
	}
	
	/**
	 * @return Returns the minInterval.
	 * @throws InsufficientDataException 
	 */
	public long getMinInterval() throws InsufficientDataException {
		return statData.getMinData();
	}

	public boolean isWaitingForSufficientData() {
		return statData.isWaitingForSufficientData();
	}

	public long getNrEvents() {
		return statData.getCount();
	}

	public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("[EventStatistics:");
			buffer.append(" lastTime: ");
			buffer.append(lastTime);
			buffer.append(statData);
			buffer.append("]");
			return buffer.toString();
		}
}