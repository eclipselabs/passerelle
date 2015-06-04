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
 * StatisticsServiceFactory
 * 
 * TODO: class comment
 * 
 * @author erwin
 */
public class StatisticsServiceFactory {
	private static StatisticsService service;
	private static final String STATISTICS_PROP_KEY = "passerelle.statistics";
	private static boolean statisticsEnabled= (System.getProperty(STATISTICS_PROP_KEY) != null);

	public synchronized static StatisticsService getService() {
		if(service==null) {
			if(statisticsEnabled)
				service = new StatisticsServiceImpl();
			else
				service = new StatisticsServiceDummyImpl();
		}
		
		return service;
	}

}
