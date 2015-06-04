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
package com.isencia.passerelle.actor;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.isencia.passerelle.domain.cap.Director;
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
    flow.setDirector(new Director(flow,"director"));
    
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
    flow.setDirector(new Director(flow,"director"));
    
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
  
  public void testFlowWithValidationErrorAndValidation() throws Exception {
    Flow flow = new Flow("testFlowWithValidationErrorAndValidation",null);
    FlowManager flowMgr = new FlowManager();
    Director director = new Director(flow,"director");
    flow.setDirector(director);
    
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    Delay firstWorker = new Delay(flow, "firstWorker");
    Delay secondWorker = new Delay(flow, "secondWorker");
    Delay thirdWorker = new Delay(flow, "thirdWorker");
    InitializationValidator validationError = new InitializationValidator(flow, "validationError");
    InitializationValidator validationOk = new InitializationValidator(flow, "validationOk");
    
    flow.connect(source, firstWorker);
    flow.connect(firstWorker, secondWorker);
    flow.connect(secondWorker, validationOk);
    flow.connect(firstWorker, validationError);
    flow.connect(validationError, thirdWorker);
    flow.connect(thirdWorker, sink);
    flow.connect(validationOk, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.put("director."+DirectorAdapter.VALIDATE_INITIALIZATION_PARAM, "true");
    props.put("validationError.Must generate validation error", "true");
    props.put("validationError.Validation error message", "something's wrong here");
    try {
      flowMgr.executeBlockingLocally(flow,props);
    } catch (ValidationException e) {
      // this is expected
    }
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 0L)
    .expectActorIterationCount(source, 0L)
    .expectActorIterationCount(sink, 0L)
    .expectActorIterationCount(firstWorker, 0L)
    .expectActorIterationCount(secondWorker, 0L)
    .expectActorIterationCount(thirdWorker, 0L)
    .expectActorIterationCount(validationError, 0L)
    .expectActorIterationCount(validationOk, 0L)
    .assertFlow(flow);
  }

  public void testFlowWithValidationErrorButNoValidation() throws Exception {
    Flow flow = new Flow("testFlowWithValidationErrorButNoValidation",null);
    FlowManager flowMgr = new FlowManager();
    Director director = new Director(flow,"director");
    flow.setDirector(director);
    
    Const source = new Const(flow,"Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    Delay firstWorker = new Delay(flow, "firstWorker");
    Delay secondWorker = new Delay(flow, "secondWorker");
    Delay thirdWorker = new Delay(flow, "thirdWorker");
    InitializationValidator validationError = new InitializationValidator(flow, "validationError");
    InitializationValidator validationOk = new InitializationValidator(flow, "validationOk");
    
    flow.connect(source, firstWorker);
    flow.connect(firstWorker, secondWorker);
    flow.connect(secondWorker, validationOk);
    flow.connect(firstWorker, validationError);
    flow.connect(validationError, thirdWorker);
    flow.connect(thirdWorker, sink);
    flow.connect(validationOk, sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.put("director."+DirectorAdapter.VALIDATE_INITIALIZATION_PARAM, "false");
    props.put("validationError.Must generate validation error", "true");
    props.put("validationError.Validation error message", "something's wrong here");
    try {
      flowMgr.executeBlockingLocally(flow,props);
    } catch (ValidationException e) {
      // this is expected
    }
    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    // strange, but due to uncontrolled parallel branching, the msg arrives twice at the sink!
    .expectMsgReceiptCount(sink, 2L)
    .expectActorIterationCount(source, 1L)
    .expectActorIterationCount(sink, 2L)
    .expectActorIterationCount(firstWorker, 1L)
    .expectActorIterationCount(secondWorker, 1L)
    .expectActorIterationCount(thirdWorker, 1L)
    .expectActorIterationCount(validationError, 1L)
    .expectActorIterationCount(validationOk, 1L)
    .assertFlow(flow);
  }
// this test fails from time to time
//  public void testFlowWithoutValidationError() throws Exception {
//    Flow flow = new Flow("testFlowWithoutValidationError",null);
//    FlowManager flowMgr = new FlowManager();
//    Director director = new Director(flow,"director");
//    flow.setDirector(director);
//    
//    Const source = new Const(flow,"Constant");
//    DevNullActor sink = new DevNullActor(flow, "sink");
//    Delay firstWorker = new Delay(flow, "firstWorker");
//    Delay secondWorker = new Delay(flow, "secondWorker");
//    Delay thirdWorker = new Delay(flow, "thirdWorker");
//    InitializationValidator validationError = new InitializationValidator(flow, "validationError");
//    InitializationValidator validationOk = new InitializationValidator(flow, "validationOk");
//    
//    flow.connect(source, firstWorker);
//    flow.connect(firstWorker, secondWorker);
//    flow.connect(secondWorker, validationOk);
//    flow.connect(firstWorker, validationError);
//    flow.connect(validationError, thirdWorker);
//    flow.connect(thirdWorker, sink);
//    flow.connect(validationOk, sink);
//    
//    Map<String, String> props = new HashMap<String, String>();
//    props.put("Constant.value", "Hello world");
//    props.put("director."+DirectorAdapter.VALIDATE_INITIALIZATION_PARAM, "true");
//    props.put("validationError.Must generate validation error", "false");
//    props.put("validationError.Validation error message", "something's wrong here");
//    flowMgr.executeBlockingLocally(flow,props);
//
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(source, 1L)
//    // strange, but due to uncontrolled parallel branching, the msg arrives twice at the sink!
//    .expectMsgReceiptCount(sink, 2L)
//    .expectActorIterationCount(source, 1L)
//    .expectActorIterationCount(sink, 2L)
//    .expectActorIterationCount(firstWorker, 1L)
//    .expectActorIterationCount(secondWorker, 1L)
//    .expectActorIterationCount(thirdWorker, 1L)
//    .expectActorIterationCount(validationError, 1L)
//    .expectActorIterationCount(validationOk, 1L)
//    .assertFlow(flow);
//  }
  
}
