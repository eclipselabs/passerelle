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

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;

/**
 * 
 * A utility class to provide a simple access to the Passerelle engine's
 * JMX server, used to publish execution statistics and to allow external
 * management of an executing Passerelle solution assembly.
 * 
 * @author erwin
 *
 */
class StatisticsServiceImpl extends StatisticsServiceDummyImpl implements StatisticsService {
  private static final String PASSERELLE_STATISTICS_SERVICE = "Passerelle.StatisticsService";

  private final static Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceImpl.class);
	
	private MBeanServer svr;
	private Set<ObjectName> registeredNames = new HashSet<ObjectName>();
	
	protected StatisticsServiceImpl() {
		try {
			svr = ManagementFactory.getPlatformMBeanServer();
			start();
		} catch (Exception e) {
			LOGGER.error(ErrorCode.SYSTEM_CONFIGURATION_ERROR+" - Error starting StatisticsService",e);
		}
	}

	public void registerStatistics(NamedStatistics s) {
	  if(s==null)
	    throw new IllegalArgumentException("registerStatistics : null argument not allowed");
		try {
			ObjectName objName = new ObjectName(PASSERELLE_STATISTICS_SERVICE+":name="+s.getName());
      if(svr.isRegistered(objName)) {
        try {
          svr.unregisterMBean(objName);
        } catch (InstanceNotFoundException e) {
          // should not happen...
        }
      }
      svr.registerMBean(s,objName);
      registeredNames.add(objName);
		} catch (Exception e) {
			LOGGER.warn(ErrorCode.SYSTEM_CONFIGURATION_ERROR+" - Error registering statistics MBean "+s.getName(), e);
		}
	}
	
  public void unregisterStatistics(NamedStatistics s) {
    if(s==null)
      throw new IllegalArgumentException("unregisterStatistics : null argument not allowed");
    try {
      ObjectName objName = new ObjectName(PASSERELLE_STATISTICS_SERVICE+":name="+s.getName());
      unregisterMBean(objName);
    } catch (Exception e) {
      LOGGER.error(ErrorCode.SYSTEM_CONFIGURATION_ERROR+" - Error unregistering statistics MBean "+s.getName(), e);
    }
  }

  private void unregisterMBean(ObjectName objName) throws MBeanRegistrationException, InstanceNotFoundException {
    registeredNames.remove(objName);
    if(svr.isRegistered(objName)) {
      svr.unregisterMBean(objName);
    }
  }
	
	public synchronized void start() {
	}

	public synchronized void stop() {
	  reset();
	}
	
	public void reset() {
		for (Iterator<ObjectName> mbNameItr = registeredNames.iterator(); mbNameItr.hasNext();) {
			ObjectName mbName = mbNameItr.next();
			try {
        unregisterMBean(mbName);
      } catch (Exception e) {
        LOGGER.error(ErrorCode.SYSTEM_CONFIGURATION_ERROR+" - Error unregistering statistics MBean "+mbName, e);
      }
		}
	}

}
