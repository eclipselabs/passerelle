/* Copyright 2012 - iSencia Belgium NV

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
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.ext.DirectorAdapter;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.MultiBlockingInputActor;

public class ActorApiTest extends TestCase {
  /**
   * A unit test for an actor with multiple blocking inputs
   * 
   * @throws Exception
   */
  public void testMultiBlockingInputs() throws Exception {
    Flow flow = new Flow("testMultiBlockingInputs",null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    Const source1 = new Const(flow,"Constant1");
    Const source2 = new Const(flow,"Constant2");
    Delay delay = new Delay(flow, "delay");
    MultiBlockingInputActor trf = new MultiBlockingInputActor(flow, "trf");
    DevNullActor sink = new DevNullActor(flow, "sink");
    
    flow.connect(source1.output, trf.input1);
    flow.connect(source2.output, delay.input);
    flow.connect(delay.output, trf.input2);
    flow.connect(trf, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant1.value", "Hello world");
    props.put("Constant2.value", "Goodbye world");
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source1, 1L)
    .expectMsgSentCount(source2, 1L)
    .expectActorIterationCount(trf, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }
  
  /**
   * A unit test for a plain HelloPasserelle model.
   * 
   * @throws Exception
   */
  public void testHelloPasserelle() throws Exception {
    Flow flow = new Flow("testHelloPasserelle",null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow,"director"));
    
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    
    flow.connect(source, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow,props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }
  
}
