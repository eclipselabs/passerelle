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
 * An implementation that does nothing, to be used
 * when statistics are not activated.
 * 
 * @author erwin
 */
class StatisticsServiceDummyImpl implements StatisticsService {

	protected StatisticsServiceDummyImpl() {
		super();
	}

	@Override
	public void registerStatistics(NamedStatistics s) {
	}

  @Override
  public void unregisterStatistics(NamedStatistics s) {
  }

  @Override
  public void start() {
	}

  @Override
	public void stop() {
	}

  @Override
	public void reset() {
	}
}
