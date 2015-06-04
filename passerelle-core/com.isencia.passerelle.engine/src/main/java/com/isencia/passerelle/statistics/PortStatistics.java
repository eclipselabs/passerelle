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


import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.message.ManagedMessage;

/**
 * PortStatistics
 * 
 * @author erwin
 */
public class PortStatistics implements PortStatisticsMBean, NamedStatistics {
	
	private String portName;
	
	/**
	 * Some performance statistics. Could be usefull for monitoring purposes.
	 */
	private EventStatistics receiptStatistics;
	private EventStatistics sendingStatistics;


	public PortStatistics(Port port) {
		this.portName=port.getFullName();
		receiptStatistics = new EventStatistics();
		sendingStatistics = new EventStatistics();
	}
	
	public void acceptReceivedMessage(ManagedMessage msg) {
		receiptStatistics.acceptEvent(msg);
	}

	public void acceptSentMessage(ManagedMessage msg) {
		sendingStatistics.acceptEvent(msg);
	}

	public EventStatistics getReceiptStatistics() {
		return receiptStatistics;
	}

	public EventStatistics getSendingStatistics() {
		return sendingStatistics;
	}

	public long getNrSentMessages() {
		return sendingStatistics.getNrEvents();
	}

	/**
	 * in msec
	 */
	public long getAvgIntervalSentMessages() {
		try {
			return sendingStatistics.getAvgInterval();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}

	public long getNrReceivedMessages() {
		return receiptStatistics.getNrEvents();
	}

	/**
	 * in msec
	 */
	public long getAvgIntervalReceivedMessages() {
		try {
			return receiptStatistics.getAvgInterval();
		} catch (InsufficientDataException e) {
			return 0;
		}
	}

	public void reset() {
		receiptStatistics.reset();
		sendingStatistics.reset();
	}

	public String getName() {
		return portName;
	}
}
