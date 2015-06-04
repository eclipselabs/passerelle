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
import ptolemy.actor.Manager;
import ptolemy.actor.Manager.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.control.Stop;
import com.isencia.passerelle.actor.convert.HeaderModifier;
import com.isencia.passerelle.actor.error.ErrorObserver;
import com.isencia.passerelle.actor.filter.HeaderFilter;
import com.isencia.passerelle.actor.general.DevNullActor;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.FlowNotExecutingException;
import com.isencia.passerelle.testsupport.FlowBuilder;
import com.isencia.passerelle.testsupport.FlowExecutionTester;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.AsynchDelay;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.ExceptionGenerator;
import com.isencia.passerelle.testsupport.actor.ForLoop;
import com.isencia.passerelle.testsupport.actor.MessageHistoryStack;
import com.isencia.passerelle.testsupport.actor.TextSource;

/**
 * Some unit tests for Passerelle's ET domain
 * 
 * @author erwin
 */
public class EtDomainModelExecutionsTest extends TestCase {
  private Flow flow;
  private FlowManager flowMgr;

  protected void setUp() throws Exception {
    flowMgr = new FlowManager();
  }

//  public void testHelloPasserelle() throws Exception {
//    flow = new Flow("testHelloPasserelle", null);
//    ETDirector director = new ETDirector(flow, "director");
//    flow.setDirector(director);
//
//    Const constant = new Const(flow, "Constant");
//    Actor helloHello = new Forwarder(flow, "HelloHello");
//    Actor tracerConsole = new MessageHistoryStack(flow, "TracerConsole");
//
//    flow.connect(constant, helloHello);
//    flow.connect(helloHello, tracerConsole);
//
//    Map<String, String> props = new HashMap<String, String>();
//    props.put("director.Nr of dispatch threads", "2");
//    props.put("director.Dispatch timeout(ms)", "250");
//    props.put("Constant.value", "Hello world");
//    flowMgr.executeBlockingLocally(flow, props);
//
//    // now check if all went as expected
//    new FlowStatisticsAssertion().expectMsgSentCount(constant, 1L).expectMsgReceiptCount(tracerConsole, 1L).expectActorIterationCount(helloHello, 1L)
//        .assertFlow(flow);
//  }

  private static Delay createDelayActor(boolean asynchDelay, Flow flow, String name) throws IllegalActionException, NameDuplicationException {
    return asynchDelay ? new AsynchDelay(flow, name) : new Delay(flow, name);
  }

  public void testSynchDelayedForwarder() throws Exception {
    flow = new Flow("testSynchDelayedForwarder", null);
    ETDirector d = new ETDirector(flow, "director");
    __testDelayedForwarder(false, d, new HashMap<String, String>());
  }

  public void testAsynchDelayedForwarder() throws Exception {
    flow = new Flow("testAsynchDelayedForwarder", null);
    ETDirector d = new ETDirector(flow, "director");
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
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "2000");
    props.put("Constant.value", "Hello world");
    props.putAll(paramOverrides);

    flowMgr.executeBlockingLocally(flow, props);

    // now check if all went as expected
    new FlowStatisticsAssertion()
      .expectMsgSentCount(constant, 1L)
      .expectMsgReceiptCount(tracerConsole, 1L)
      .expectActorIterationCount(helloHello, 1L)
      .assertFlow(flow);
  }

  /**
   * This test illustrates the chaining of each delay when only 1 work thread is available to execute all work steps. The total model execution time thus
   * becomes of the order of 3*(3+3+3), i.e. each of the 3 src msg needs to pass through 3 consecutive work steps, each one taking 3s.
   */
  public void testChainedDelaysET1ThreadWithEventHistory() throws Exception {
    flow = new Flow("testChainedDelaysET1ThreadWithEventHistory", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director." + ETDirector.KEEP_EVENT_HISTORY_PARAMNAME, "true");
    ETDirector d = new ETDirector(flow, "director");
    __testChainedDelays(false, d, props);
    assertFalse("Director must maintain event history", d.getEventHistory().isEmpty());
  }

  public void testChainedDelaysET2ThreadsWithoutEventHistory() throws Exception {
    flow = new Flow("testChainedDelaysET2ThreadsWithoutEventHistory", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "250");
    ETDirector d = new ETDirector(flow, "director");
    __testChainedDelays(false, d, props);
    assertTrue("Director must NOT maintain event history", d.getEventHistory().isEmpty());
  }

  public void testChainedDelaysET3Threads() throws Exception {
    flow = new Flow("testChainedDelaysET3Threads", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    ETDirector d = new ETDirector(flow, "director");
    __testChainedDelays(false, d, props);
  }

  public void testChainedAsynchDelaysET1Thread() throws Exception {
    flow = new Flow("testChainedAsynchDelaysET1Thread", null);
    Map<String, String> props = new HashMap<String, String>();
    __testChainedDelays(true, new ETDirector(flow, "director"), props);
  }

  public void testChainedAsynchDelaysET2Threads() throws Exception {
    flow = new Flow("testChainedAsynchDelaysET2Threads", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedDelays(true, new ETDirector(flow, "director"), props);
  }

  public void testChainedAsynchDelaysET3Threads() throws Exception {
    flow = new Flow("testChainedAsynchDelaysET3Threads", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    ETDirector d = new ETDirector(flow, "director");
    __testChainedDelays(true, d, props);
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

    new FlowStatisticsAssertion()
      .expectMsgReceiptCount(sink, 3L)
      .assertFlow(flow);
  }

//  public void test300Times_ConcurrentInputsOnAsynchDelayET3Threads() throws Exception {
//    int errCount = 0;
//    int i = 0;
//    for (i = 0; i < 300; i++) {
//      flow = new Flow("ConcurrentInputsOnAsynchDelayET3Thread" + i, null);
//      ETDirector d = null;
//      try {
//        Map<String, String> props = new HashMap<String, String>();
//        props.put("director." + ETDirector.NR_OF_DISPATCH_THREADS_PARAMNAME, "6");
//        props.put("director." + ETDirector.DISPATCH_TIMEOUT_PARAMNAME, "250");
//        props.put("director." + ETDirector.KEEP_EVENT_HISTORY_PARAMNAME, "true");
//        d = new ETDirector(flow, "director");
//        __testConcurrentInputsOnDelay(true, d, props);
//      } catch (Error e) {
//        errCount++;
//        System.err.println(e.getMessage());
//        System.err.println("Event History");
//        for (Event evt : d.getEventHistory()) {
//          System.err.println(evt);
//        }
//        System.err.println("Event Errors");
//        for (EventError evtErr : d.getEventErrors()) {
//          System.err.println(evtErr);
//        }
//        System.err.println("Unhandled Events");
//        for (Event evt : d.getUnhandledEvents()) {
//          System.err.println(evt);
//        }
//        System.err.println("Pending Events");
//        for (Event evt : d.getPendingEvents()) {
//          System.err.println(evt);
//        }
//      }
//    }
//
//    System.out.println(errCount + " errors on "+i);
//  }

  public void testConcurrentInputsOnHeaderFilter() throws Exception {
    flow = new Flow("testConcurrentInputsOnHeaderFilter", null);
    flow.setDirector(new ETDirector(flow, "director"));

    Actor src1 = new TextSource(flow, "src1");
    Actor src2 = new TextSource(flow, "src2");
    Actor src3 = new TextSource(flow, "src3");
    HeaderModifier hdrModif1 = new HeaderModifier(flow, "hdrModif1");
    HeaderFilter hdrFltr2 = new HeaderFilter(flow, "hdrFltr2");
    HeaderModifier hdrModif3 = new HeaderModifier(flow, "hdrModif3");
    MessageHistoryStack sink1 = new MessageHistoryStack(flow, "sink1");
    MessageHistoryStack sink2 = new MessageHistoryStack(flow, "sink2");

    flow.connect(src1, hdrModif1);
    flow.connect(src2, hdrFltr2);
    flow.connect(src3, hdrFltr2);
    flow.connect(hdrModif1, hdrFltr2);
    flow.connect(hdrFltr2.outputOk, hdrModif3.input);
    flow.connect(hdrFltr2.outputNotOk, sink2.input);
    flow.connect(hdrModif3, sink1);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src1.values", "pol1,pol2,pol3");
    props.put("src2.values", "pel1,pel2,pel3");
    props.put("src3.values", "pingo1,pingo2,pingo3");
    props.put("hdrModif1.mode", "Add");
    props.put("hdrFltr2.FilterType", "Contains");
    props.put("hdrModif3.mode", "Remove");
    props.put("hdrModif1.header name", "Hello");
    props.put("hdrModif1.header value", "world");
    props.put("hdrFltr2.Header", "Hello");
    props.put("hdrFltr2.Filter", "world");
    props.put("hdrModif3.header name", "Hello");

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
      .expectMsgReceiptCount(sink1, 3L)
      .expectMsgReceiptCount(sink2, 6L)
      .assertFlow(flow);
  }

  public void testConcurrentInputsOnHeaderModifier() throws Exception {
    flow = new Flow("testConcurrentInputsOnHeaderModifier", null);
    flow.setDirector(new ETDirector(flow, "director"));

    Actor src1 = new TextSource(flow, "src1");
    Actor src2 = new TextSource(flow, "src2");
    Actor src3 = new TextSource(flow, "src3");
    HeaderModifier hdrModif1 = new HeaderModifier(flow, "hdrModif1");
    HeaderModifier hdrModif2 = new HeaderModifier(flow, "hdrModif2");
    HeaderModifier hdrModif3 = new HeaderModifier(flow, "hdrModif3");
    Actor sink = new MessageHistoryStack(flow, "sink");

    flow.connect(src1, hdrModif1);
    flow.connect(src2, hdrModif1);
    flow.connect(src3, hdrModif1);
    flow.connect(hdrModif1, hdrModif2);
    flow.connect(hdrModif2, hdrModif3);
    flow.connect(hdrModif3, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src1.values", "pol1,pol2,pol3");
    props.put("src2.values", "pel1,pel2,pel3");
    props.put("src3.values", "pingo1,pingo2,pingo3");
    props.put("hdrModif1.mode", "Add");
    props.put("hdrModif2.mode", "Modify");
    props.put("hdrModif3.mode", "Remove");
    props.put("hdrModif1.header name", "Hello");
    props.put("hdrModif1.header value", "world");
    props.put("hdrModif2.header name", "Hello");
    props.put("hdrModif2.header value", "moon");
    props.put("hdrModif3.header name", "Hello");

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
      .expectMsgReceiptCount(sink, 9L)
      .assertFlow(flow);
  }
// this test fails sometimes for unknown reason
//  public void testConcurrentInputsOnAsynchDelayET3Threads() throws Exception {
//    Map<String, String> props = new HashMap<String, String>();
//    props.put("director." + ETDirector.NR_OF_DISPATCH_THREADS_PARAMNAME, "3");
//    props.put("director." + ETDirector.DISPATCH_TIMEOUT_PARAMNAME, "250");
//    flow = new Flow("testConcurrentInputsOnAsynchDelayET3Threads", null);
//    ETDirector d = new ETDirector(flow, "director");
//    __testConcurrentInputsOnDelay(true, d, props);
//  }

  public void testConcurrentInputsOnAsynchDelayET4Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "4");
    props.put("director.Dispatch timeout(ms)", "250");
    flow = new Flow("testConcurrentInputsOnAsynchDelayET4Threads", null);
    ETDirector d = new ETDirector(flow, "director");
    __testConcurrentInputsOnDelay(true, d, props);
  }

  public void testConcurrentInputsOnDelayET3Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    flow = new Flow("testConcurrentInputsOnDelayET3Threads", null);
    ETDirector d = new ETDirector(flow, "director");
    __testConcurrentInputsOnDelay(false, d, props);
  }

  public void testConcurrentInputsOnDelayET4Threads() throws Exception {
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "4");
    props.put("director.Dispatch timeout(ms)", "250");
    flow = new Flow("testConcurrentInputsOnDelayET4Threads", null);
    ETDirector d = new ETDirector(flow, "director");
    __testConcurrentInputsOnDelay(false, d, props);
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

    new FlowStatisticsAssertion()
      .expectMsgReceiptCount(sink, 9L)
      .assertFlow(flow);
  }

  /**
   * A more chaotic delay model, with two parallel branches with delay actors, ending up in their own sinks.
   */
  public void testChainedAndParallelDelaysET3Threads() throws Exception {
    flow = new Flow("testChainedAndParallelDelaysET3Threads", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedAndParallelDelays(false, new ETDirector(flow, "director"), props);
  }

  public void testChainedAndParallelDelaysET5Threads() throws Exception {
    flow = new Flow("testChainedAndParallelDelaysET5Threads", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "5");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedAndParallelDelays(false, new ETDirector(flow, "director"), props);
  }

  public void testChainedAndParallelAsynchDelaysET3Threads() throws Exception {
    flow = new Flow("testChainedAndParallelAsynchDelaysET3Threads", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "3");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedAndParallelDelays(true, new ETDirector(flow, "director"), props);
  }

  public void testChainedAndParallelAsynchDelaysET5Threads() throws Exception {
    flow = new Flow("testChainedAndParallelAsynchDelaysET5Threads", null);
    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "5");
    props.put("director.Dispatch timeout(ms)", "250");
    __testChainedAndParallelDelays(true, new ETDirector(flow, "director"), props);
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

    new FlowStatisticsAssertion()
      .expectMsgReceiptCount(sink1, 3L)
      .expectMsgReceiptCount(sink2, 3L)
      .assertFlow(flow);
  }

  public void testProcessException() throws Exception {
    flow = new Flow("testProcessException", null);
    ETDirector director = new ETDirector(flow, "director");
    flow.setDirector(director);

    Const constant = new Const(flow, "const");
    Actor excGenerator = new ExceptionGenerator(flow, "excGenerator");
    Actor sink = new MessageHistoryStack(flow, "sink");

    flow.connect(constant, excGenerator);
    flow.connect(excGenerator, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("director.Nr of dispatch threads", "2");
    props.put("director.Dispatch timeout(ms)", "250");
    props.put("const.value", "Hello world");
    props.put("excGenerator.process Exception", "true");
    flowMgr.executeBlockingLocally(flow, props);

    // now check if all went as expected
    new FlowStatisticsAssertion()
      .expectMsgSentCount(constant, 1L)
      .expectMsgReceiptCount(sink, 0L)
      .expectActorIterationCount(excGenerator, 1L)
      .assertFlow(flow);
  }

  /**
   * A unit test for a plain model with Error Observer This model should never stop by itself.
   * 
   * @throws Exception
   */
  public void testFlowWithErrorObserver() throws Exception {
    flow = new Flow("testFlowWithErrorObserver", null);
    flow.setDirector(new ETDirector(flow, "director"));

    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    ErrorObserver errObs = new ErrorObserver(flow, "errObs");

    flow.connect(source, sink);
    flow.connect(errObs.messageInErrorOutput, sink.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    // launch the flow in a background thread
    flowMgr.execute(flow, props);
    // now wait a while
    Thread.sleep(3000);
    try {
      State state = flowMgr.getLocalExecutionState(flow);
      assertEquals("Flow must be stopped", Manager.IDLE, state);
    } catch (FlowNotExecutingException e) {
    }
    new FlowStatisticsAssertion()
      .expectMsgSentCount(source, 1L)
      .expectMsgReceiptCount(sink, 1L)
      .assertFlow(flow);

  }

  /**
   * A unit test for a plain model with Error Observer
   * 
   * @throws Exception
   */
  public void testFlowWithErrorObserverAndStop() throws Exception {
    flow = new Flow("testFlowWithErrorObserverAndStop", null);
    flow.setDirector(new ETDirector(flow, "director"));

    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");
    ErrorObserver errObs = new ErrorObserver(flow, "errObs");
    Stop stop = new Stop(flow, "stop");

    flow.connect(source, sink);
    flow.connect(errObs.messageInErrorOutput, sink.input);
    flow.connect(sink.hasFiredPort, stop.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
      .expectMsgSentCount(source, 1L)
      .expectMsgReceiptCount(sink, 1L)
      .assertFlow(flow);
  }
  
  /**
   * A unit test for a plain looping model.
   * 
   * @throws Exception
   */
  public void testLoop() throws Exception {
    flow = new Flow("testHelloPasserelle", null);
    flow.setDirector(new ETDirector(flow, "director"));

    Const source = new Const(flow, "src");
    ForLoop loopCtrl = new ForLoop(flow, "loop");
    AsynchDelay delay = new AsynchDelay(flow, "delay");
    DevNullActor sink = new DevNullActor(flow, "sink");

    flow.connect(source.output, loopCtrl.startPort);
    flow.connect(loopCtrl.outputPort, delay.input);
    flow.connect(delay.output, loopCtrl.nextPort);
    flow.connect(loopCtrl.endPort, sink.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("src.value", "Hello world");
    props.put("loop.Max Count", "2");
    props.put("delay.time(ms)", "100");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgSentCount(loopCtrl.outputPort, 3L)
    .expectMsgReceiptCount(loopCtrl.nextPort, 3L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
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
      public Flow buildFlow(String name) throws Exception {
        final Flow flow = new Flow(name, null);
        flow.setDirector(new ETDirector(flow, "director"));
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
      public Flow buildFlow(String name) throws Exception {
        final Flow flow = new Flow(name, null);
        flow.setDirector(new ETDirector(flow, "director"));
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
