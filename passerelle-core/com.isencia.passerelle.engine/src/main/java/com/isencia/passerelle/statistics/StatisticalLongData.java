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


/**
 * StatisticalLongData
 * 
 * @author erwin
 */
class StatisticalLongData {
	
	long count;
	long avgData;
	Long minData;
	Long maxData;

	/**
	 * Recalculate statistics, including this new data
	 * @param data
	 */
	public synchronized void acceptData(long data) {
		count++;
		if((minData==null) || (minData.longValue()>data)) {
			minData = new Long(data);
		}
		if((maxData==null) || (maxData.longValue()<data)) {
			maxData = new Long(data);
		}
		avgData += (data - avgData) / count;
	}
	
	/**
	 * Loose all statistical data
	 *
	 */
	public synchronized void reset() {
		count=0;
		avgData=0;
		minData=null;
		maxData=null;
	}
	
	public long getAvgData() throws InsufficientDataException {
		if(isWaitingForSufficientData())
			throw new InsufficientDataException();
		
		return avgData;
	}
	
	/**
	 * @return Returns the maximal data value received.
	 * @throws InsufficientDataException 
	 */
	public long getMaxData() throws InsufficientDataException {
		if(isWaitingForSufficientData())
			throw new InsufficientDataException();
		
		return maxData.longValue();
	}
	
	/**
	 * @return Returns the minimal data value received.
	 * @throws InsufficientDataException 
	 */
	public long getMinData() throws InsufficientDataException {
		if(isWaitingForSufficientData())
			throw new InsufficientDataException();
		
		return minData.longValue();
	}

	public boolean isWaitingForSufficientData() {
		return count==0;
	}

	public long getCount() {
		return count;
	}

	public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("[StatisticalLongData:");
			buffer.append(" count: ");
			buffer.append(count);
			buffer.append(" avgData: ");
			buffer.append(avgData);
			buffer.append(" minData: ");
			buffer.append(minData);
			buffer.append(" maxData: ");
			buffer.append(maxData);
			buffer.append("]");
			return buffer.toString();
		}


}
