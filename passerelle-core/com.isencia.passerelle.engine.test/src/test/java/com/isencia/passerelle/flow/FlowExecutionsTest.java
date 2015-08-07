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
package com.isencia.passerelle.flow;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.domain.cap.CapDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowBuilder;
import com.isencia.passerelle.testsupport.FlowExecutionTester;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.AsynchDelay;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.ExceptionGenerator;
import com.isencia.passerelle.testsupport.actor.MessageHistoryStack;
import com.isencia.passerelle.testsupport.actor.TextSource;

/**
 * Some unit tests for Passerelle's ET domain
 * 
 * @author erwin
 */
public class FlowExecutionsTest extends TestCase {
  private Flow flow;
  private FlowManager flowMgr;

  @Override
  protected void setUp() throws Exception {
    flowMgr = new FlowManager();
  }

  private static Director createProcessDirector(boolean createCapDirector, Flow flow, String name) throws IllegalActionException, NameDuplicationException {
    return createCapDirector ? new CapDirector(flow, name) : new com.isencia.passerelle.domain.cap.Director(flow, name);
  }

  private static Delay createDelayActor(boolean asynchDelay, Flow flow, String name) throws IllegalActionException, NameDuplicationException {
    return asynchDelay ? new AsynchDelay(flow, name) : new Delay(flow, name);
  }

  public void testSynchDelayedForwarder() throws Exception {
    flow = new Flow("testSynchDelayedForwarder", null);
    Director d = createProcessDirector(false, flow, "director");
    __testDelayedForwarder(false, d, new HashMap<String, String>());
  }

  public void testAsynchDelayedForwarder() throws Exception {
    flow = new Flow("testAsynchDelayedForwarder", null);
    Director d = createProcessDirector(false, flow, "director");
    __testDelayedForwarder(true, d, new HashMap<String, String>());
  }

  public void __testDelayedForwarder(boolean asynchDelay, ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);

    Const constant = new Const(flow, "Constant");
    Actor helloHello = createDelayActor(asynchDelay, flow, "HelloHello");
    Actor tracerConsole = new MessageHistoryStack(flow, "TracerConsole");

    flow.connect(constant, helloHello);
    flow.connect(helloHello, tracerConsole);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    // now check if all went as expected
    new FlowStatisticsAssertion().
    	expectMsgSentCount(constant, 1L).
    	expectMsgReceiptCount(tracerConsole, 1L).
    	expectActorIterationCount(helloHello, 1L).
    	assertFlow(flow);
  }

  /**
   * This test illustrates the "factory chain" advantage of the PN domain, where each actor has its own thread. 
	 * This leads to all 3 "worker" actors (the delays) to be able to work (spend time) concurrently.
   */
  public void testChainedDelays() throws Exception {
    flow = new Flow("testChainedDelays", null);
    __testChainedDelays(false, createProcessDirector(false, flow, "director"), new HashMap<String, String>());
  }

  public void testChainedAsynchDelays() throws Exception {
    flow = new Flow("testChainedAsynchDelays", null);
    __testChainedDelays(true, createProcessDirector(false, flow, "director"), new HashMap<String, String>());
  }

  public void __testChainedDelays(boolean asynchDelay, ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);

    Actor src = new TextSource(flow, "src");
    Actor delay1 = createDelayActor(asynchDelay, flow, "delay1");
    Actor delay2 = createDelayActor(asynchDelay, flow, "delay2");
    Actor delay3 = createDelayActor(asynchDelay, flow, "delay3");
    Actor sink = new MessageHistoryStack(flow, "sink");

    flow.connect(src, delay1);
    flow.connect(delay1, delay2);
    flow.connect(delay2, delay3);
    flow.connect(delay3, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src.values", "pol,pel,pingo");
    props.put("delay1.time(s)", "1");
    props.put("delay2.time(s)", "1");
    props.put("delay3.time(s)", "1");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay2.Buffer time (ms)", "10");
    props.put("delay3.Buffer time (ms)", "10");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion().expectMsgReceiptCount(sink, 3L).assertFlow(flow);
  }

  public void testConcurrentInputsOnDelay() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    flow = new Flow("testConcurrentInputsOnDelay", null);
    __testConcurrentInputsOnDelay(false, createProcessDirector(false, flow, "director"), props);
  }

  public void testConcurrentInputsOnAsynchDelay() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    flow = new Flow("testConcurrentInputsOnAsynchDelay", null);
    __testConcurrentInputsOnDelay(true, createProcessDirector(false, flow, "director"), props);
  }

  public void testConcurrentInputsOnAsynchDelayWithCapDirector() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    flow = new Flow("testConcurrentInputsOnAsynchDelayWithCapDirector", null);
    __testConcurrentInputsOnDelay(true, createProcessDirector(true, flow, "director"), props);
  }

  public void __testConcurrentInputsOnDelay(boolean asynchDelay, ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);

    Actor src1 = new TextSource(flow, "src1");
    Actor src2 = new TextSource(flow, "src2");
    Actor src3 = new TextSource(flow, "src3");
    Actor delay1 = createDelayActor(asynchDelay, flow, "delay1");
    Actor delay2 = createDelayActor(asynchDelay, flow, "delay2");
    Actor delay3 = createDelayActor(asynchDelay, flow, "delay3");
    Actor sink = new MessageHistoryStack(flow, "sink");

    flow.connect(src1, delay1);
    flow.connect(src2, delay1);
    flow.connect(src3, delay1);
    flow.connect(delay1, delay2);
    flow.connect(delay2, delay3);
    flow.connect(delay3, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src1.values", "pol1,pol2,pol3");
    props.put("src2.values", "pel1,pel2,pel3");
    props.put("src3.values", "pingo1,pingo2,pingo3");
    props.put("delay1.time(s)", "1");
    props.put("delay2.time(s)", "1");
    props.put("delay3.time(s)", "1");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay2.Buffer time (ms)", "10");
    props.put("delay3.Buffer time (ms)", "10");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion().expectMsgReceiptCount(sink, 9L).assertFlow(flow);
  }

  /**
   * A more chaotic delay model, with two parallel branches with delay actors, ending up in their own sinks.
   */
  public void testChainedAndParallelDelays() throws Exception {
    flow = new Flow("testChainedAndParallelDelays", null);
    __testChainedAndParallelDelays(false, createProcessDirector(false, flow, "director"), new HashMap<String, String>());
  }

  public void testChainedAndParallelAsynchDelays() throws Exception {
    flow = new Flow("testChainedAndParallelAsynchDelays", null);
    __testChainedAndParallelDelays(true, createProcessDirector(false, flow, "director"), new HashMap<String, String>());
  }

  public void testChainedAndParallelAsynchDelaysWithCapDirector() throws Exception {
    flow = new Flow("testChainedAndParallelAsynchDelaysWithCapDirector", null);
    __testChainedAndParallelDelays(true, createProcessDirector(true, flow, "director"), new HashMap<String, String>());
  }

  public void __testChainedAndParallelDelays(boolean asynchDelay, ptolemy.actor.Director d, Map<String, String> paramOverrides) throws Exception {
    flow.setDirector(d);

    Actor src = new TextSource(flow, "src");
    Actor delay1 = new Delay(flow, "delay1");
    Actor delay_branch1_1 = createDelayActor(asynchDelay, flow, "delay1_1");
    Actor delay_branch1_2 = createDelayActor(asynchDelay, flow, "delay1_2");
    Actor delay_branch2_1 = createDelayActor(asynchDelay, flow, "delay2_1");
    Actor delay_branch2_2 = createDelayActor(asynchDelay, flow, "delay2_2");
    Actor sink1 = new MessageHistoryStack(flow, "sink1");
    Actor sink2 = new MessageHistoryStack(flow, "sink2");

    flow.connect(src, delay1);
    flow.connect(delay1, delay_branch1_1);
    flow.connect(delay1, delay_branch2_1);
    flow.connect(delay_branch1_1, delay_branch1_2);
    flow.connect(delay_branch2_1, delay_branch2_2);
    flow.connect(delay_branch1_2, sink1);
    flow.connect(delay_branch2_2, sink2);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src.values", "pol,pel,pingo");
    props.put("delay1.time(ms)", "100");
    props.put("delay1_1.time(ms)", "100");
    props.put("delay1_2.time(ms)", "100");
    props.put("delay2_1.time(ms)", "100");
    props.put("delay2_2.time(ms)", "100");
    props.put("delay1.Buffer time (ms)", "10");
    props.put("delay1_1.Buffer time (ms)", "10");
    props.put("delay1_2.Buffer time (ms)", "10");
    props.put("delay2_1.Buffer time (ms)", "10");
    props.put("delay2_2.Buffer time (ms)", "10");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion().expectMsgReceiptCount(sink1, 3L).expectMsgReceiptCount(sink2, 3L).assertFlow(flow);
  }

  public void testProcessException() throws Exception {
    flow = new Flow("testProcessException", null);
    Director director = createProcessDirector(false, flow, "director");
    flow.setDirector(director);

    Const constant = new Const(flow, "const");
    Actor excGenerator = new ExceptionGenerator(flow, "excGenerator");
    Actor sink = new MessageHistoryStack(flow, "sink");

    flow.connect(constant, excGenerator);
    flow.connect(excGenerator, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("const.value", "Hello world");
    props.put("excGenerator.process Exception", "true");
    flowMgr.executeBlockingLocally(flow, props);

    // now check if all went as expected
    new FlowStatisticsAssertion().expectMsgSentCount(constant, 1L).expectMsgReceiptCount(sink, 0L).expectActorIterationCount(excGenerator, 1L).assertFlow(flow);
  }

  public void testProcessExceptionInSequentialAsyncExecutions() throws Exception {
    FlowStatisticsAssertion flowStatsAssertion = new FlowStatisticsAssertion().
        expectMsgSentCount("const.output", 1L).
        expectMsgReceiptCount("sink.input", 0L).
        expectActorIterationCount("excGenerator", 1L);

    Map<String, String> props = new HashMap<String, String>();
    props.put("const.value", "Hello world");
    props.put("excGenerator.process Exception", "true");
    props.put("excGenerator.RuntimeException", "true");
    
    FlowBuilder builder = new FlowBuilder() {
      @Override
      public Flow buildFlow(String name) throws Exception {
        final Flow flow = new Flow(name, null);
        Director director = createProcessDirector(false, flow, "director");
        flow.setDirector(director);
        Const constant = new Const(flow, "const");
        Actor excGenerator = new ExceptionGenerator(flow, "excGenerator");
        Actor sink = new MessageHistoryStack(flow, "sink");
        flow.connect(constant, excGenerator);
        flow.connect(excGenerator, sink);
        return flow;
      }
    };
    
    FlowExecutionTester.runFlowSequentially(100, "testProcessExceptionInSequentialAsyncExecutions", builder, props, flowStatsAssertion);
  }

  public void testProcessExceptionInConcurrentAsyncExecutions() throws Exception {
    FlowStatisticsAssertion flowStatsAssertion = new FlowStatisticsAssertion().
      expectMsgSentCount("const.output", 1L).
      expectMsgReceiptCount("sink.input", 0L).
      expectActorIterationCount("excGenerator", 1L);

    Map<String, String> props = new HashMap<String, String>();
    props.put("const.value", "Hello world");
    props.put("excGenerator.process Exception", "true");
    props.put("excGenerator.RuntimeException", "true");
    
    FlowBuilder builder = new FlowBuilder() {
      @Override
      public Flow buildFlow(String name) throws Exception {
        final Flow flow = new Flow(name, null);
        Director director = createProcessDirector(false, flow, "director");
        flow.setDirector(director);
        Const constant = new Const(flow, "const");
        Actor excGenerator = new ExceptionGenerator(flow, "excGenerator");
        Actor sink = new MessageHistoryStack(flow, "sink");
        flow.connect(constant, excGenerator);
        flow.connect(excGenerator, sink);
        return flow;
      }
    };
    
    FlowExecutionTester.runFlowConcurrently(100, "testProcessExceptionInConcurrentAsyncExecutions", builder, props, flowStatsAssertion);
  }

  // utility for whenever we would like to get the moml from a java-coded flow
  // private void writeFlow(Flow flow) {
  // try {
  // File flowMomlFile = new File("C:/temp/" + flow.getName() + ".moml");
  // Writer momlWriter = new FileWriter(flowMomlFile);
  // flow.exportMoML(momlWriter);
  // momlWriter.flush();
  // momlWriter.close();
  // } catch (Exception e) {
  // e.printStackTrace();
  // }
  // }
}
