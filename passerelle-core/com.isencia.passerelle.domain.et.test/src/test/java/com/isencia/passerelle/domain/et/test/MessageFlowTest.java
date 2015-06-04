/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.domain.et.test;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.RandomMatrixSource;

public class MessageFlowTest extends TestCase {

  private FlowManager flowMgr;

  @Override
  protected void setUp() throws Exception {
    flowMgr = new FlowManager();
  }

  /**
   * A simple test to check for the risks for OutOfMemory when using ET i.o. ProcessDirectors
   * 
   * @throws Exception
   */
  public void testReceiverQueueNoCapacityLimitWithOutOfMemory() throws Exception {
    Flow flow = new Flow("testReceiverQueueNoCapacityLimitWithOutOfMemory", null);
    ETDirector director = new ETDirector(flow, "director");
    flow.setDirector(director);

    RandomMatrixSource source = new RandomMatrixSource(flow, "source");
    Delay delay = new Delay(flow, "delay");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, delay);
    flow.connect(delay, sink);

    Long nrOfMessages = new Long(200);
    Map<String, String> props = new HashMap<String, String>();
    props.put("source.Nr of matrices", nrOfMessages.toString());
    props.put("source.Nr of rows", "1000");
    props.put("source.Nr of columns", "1000");
    props.put("delay.time(ms)", "100");

    flowMgr.executeBlockingLocally(flow, props);
    new FlowStatisticsAssertion().expectMsgReceiptCount(sink, nrOfMessages).assertFlow(flow);
  }
}
