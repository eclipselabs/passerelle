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
package com.isencia.passerelle.process.actor.test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowAlreadyExecutingException;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.process.actor.BatchRequestSequenceSource;
import com.isencia.passerelle.process.actor.DelimitedResultLineGenerator;
import com.isencia.passerelle.process.actor.RequestSource;
import com.isencia.passerelle.process.actor.TaskResultActor;
import com.isencia.passerelle.process.actor.flow.CollectingEvictedMessageHandler;
import com.isencia.passerelle.process.actor.flow.Fork;
import com.isencia.passerelle.process.actor.flow.Join;
import com.isencia.passerelle.process.actor.flow.Splitter;
import com.isencia.passerelle.process.actor.trial.ContextTracerConsole;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.DevNullActor;

public class ProcessActorTest extends TestCase {

  public void testForkJoinFromMOML() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/testForkJoin.moml"));
    Flow f = FlowManager.readMoml(in);
    Map<String, String> props = new HashMap<String, String>();
    FlowManager flowMgr = new FlowManager();
    flowMgr.executeBlockingLocally(f, props);

    new FlowStatisticsAssertion()
      .expectMsgSentCount("Fork.t1", 1L)
      .expectMsgSentCount("Fork.t2", 1L)
      .expectMsgSentCount("Fork.t3", 1L)
      .expectMsgReceiptCount("Join.input", 3L)
      .expectMsgReceiptCount("Tracer Console.input", 1L)
      .assertFlow(f);
  }
  
  public void testEvictionByCount() throws Exception {
    Flow flow = new Flow("testEvictionByCount", null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow, "director"));

    RequestSource start = new RequestSource(flow, "start");
    BatchRequestSequenceSource batchStart = new BatchRequestSequenceSource(flow, "batchStart");
    Fork fork = new Fork(flow, "fork");
    CollectingEvictedMessageHandler evictedMessagesHandler = new CollectingEvictedMessageHandler();
    fork.setEvictedMessagesHandler(evictedMessagesHandler);
    DevNullActor sink = new DevNullActor(flow, "sink");
    fork.outputPortCfgExt.outputPortNamesParameter.setToken("p0,p1");
    flow.connect(start, batchStart);
    flow.connect(batchStart, fork);
    flow.connect((IOPort) fork.getPort("p0"), sink.input);

    Map<String, String> props = new HashMap<String, String>();
    props.put("fork.maxRetentionCount", "1");
    props.put("batchStart.Request parameter names", "NA");
    props.put("batchStart.Request parameter values", "021234567,027654321,012345678");
    props.put("batchStart.throttle interval (s)", "1");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion().expectMsgSentCount(start, 1L).expectMsgSentCount(batchStart, 3L).expectMsgReceiptCount(sink, 3L).assertFlow(flow);

    assertEquals("", 2, evictedMessagesHandler.evictedMessages.size());
  }

  public void testSplitJoin5Branches_1s() throws Exception {
    _testSplitJoin(5, "1");
  }
  public void testSplitJoin0Branches_1s() throws Exception {
    _testSplitJoin(0, "1");
  }
  public void testForkJoin5Branches_1s() throws Exception {
    _testForkJoin(5, "1");
  }

  public void testForkJoin5Branches_0s() throws Exception {
    _testForkJoin(5, "0");
  }

  public void testForkJoin5Branches_1_5s() throws Exception {
    _testForkJoin(5, "1", "1", "2", "3", "5");
  }

  public void testForkJoin100Branches_1s() throws Exception {
    _testForkJoin(100, "1");
  }

  public void testBatchSource() throws IllegalActionException, NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
    Flow flow = new Flow("testBatchSource", null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow, "director"));

    RequestSource start = new RequestSource(flow, "start");
    BatchRequestSequenceSource batchStart = new BatchRequestSequenceSource(flow, "batchStart");
    ContextTracerConsole sink = new ContextTracerConsole(flow, "sink");
    flow.connect(start, batchStart);
    flow.connect(batchStart, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("batchStart.Request parameter names", "NA,SERVICE");
    props.put("batchStart.Request parameter values", "021234567,027654321\r\nFIA,IDTV");

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(start, 1L)
    .expectMsgSentCount(batchStart, 2L)
    .expectMsgReceiptCount(sink, 2L)
    .assertFlow(flow);
  }

  public void testBatchSourceWithOverrides() throws IllegalActionException, NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
    Flow flow = new Flow("testBatchSourceWithOverrides", null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow, "director"));

    RequestSource start = new RequestSource(flow, "start");
    BatchRequestSequenceSource batchStart = new BatchRequestSequenceSource(flow, "batchStart");
    ContextTracerConsole sink = new ContextTracerConsole(flow, "sink");
    flow.connect(start, batchStart);
    flow.connect(batchStart, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("batchStart.Request parameter names", "NA,SERVICE");
    props.put("batchStart.Request parameter values", "021234567,027654321\r\nFIA,IDTV");
    props.put("start.request parameters", "NA=01111111,01111112,01111113\r\nSERVICE=FIA,IDTV,FIA");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(start, 1L)
    .expectMsgSentCount(batchStart, 3L)
    .expectMsgReceiptCount(sink, 3L)
    .assertFlow(flow);
  }

  protected void _testForkJoin(int branchCount, String... taskTimes) throws IllegalActionException, NameDuplicationException, FlowAlreadyExecutingException, PasserelleException {
    Flow flow = new Flow("testForkJoin_"+branchCount,null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow, "director"));

    RequestSource start = new RequestSource(flow, "start");
    Fork fork = new Fork(flow, "fork");
    Join join = new Join(flow, "join");
    DelimitedResultLineGenerator lineGen = new DelimitedResultLineGenerator(flow, "lineGen");
    ContextTracerConsole sink = new ContextTracerConsole(flow, "sink");
    flow.connect(start, fork);
    flow.connect(join, lineGen);
    flow.connect(lineGen, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("lineGen.result item names", "requestID,task0_says,goodbye");

    String[] portNames = new String[branchCount];
    portNames[0] = "p0";
    StringBuilder portNamesBldr = new StringBuilder(portNames[0]);
    for (int i = 1; i < branchCount; ++i) {
      portNames[i] = "p" + i;
      portNamesBldr.append("," + portNames[i]);
    }

    fork.outputPortCfgExt.outputPortNamesParameter.setToken(portNamesBldr.toString());

    TaskResultActor[] taskActors = new TaskResultActor[branchCount];
    for (int i = 0; i < branchCount; ++i) {
      String actorName = "task" + i;
      String portName = portNames[i];
      taskActors[i] = new TaskResultActor(flow, actorName);
      flow.connect((IOPort) fork.getPort(portName), taskActors[i].input);
      flow.connect(taskActors[i], join);
      props.put(actorName + ".Result items", "task" + i + "_says=hello world" + i);
      int ttIndex = Math.min(taskTimes.length - 1, i);
      props.put(actorName + ".time(s)", taskTimes[ttIndex]);
    }

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(start, 1L)
    .expectMsgReceiptCount(join, (long) branchCount)
    .expectMsgReceiptCount(sink, 1L)
        .assertFlow(flow);
  }

  protected void _testSplitJoin(int branchCount, String taskTime) throws IllegalActionException, NameDuplicationException, FlowAlreadyExecutingException,
      PasserelleException {
    Flow flow = new Flow("testSplitJoin_" + branchCount, null);
    FlowManager flowMgr = new FlowManager();
    flow.setDirector(new ETDirector(flow, "director"));

    RequestSource start = new RequestSource(flow, "start");
    Splitter splitter = new Splitter(flow, "splitter");
    TaskResultActor taskActor = new TaskResultActor(flow, "task");
    Join join = new Join(flow, "join");
    DelimitedResultLineGenerator lineGen = new DelimitedResultLineGenerator(flow, "lineGen");
    ContextTracerConsole sink = new ContextTracerConsole(flow, "sink");
    flow.connect(start, splitter);
    flow.connect(splitter, taskActor);
    flow.connect(taskActor, join);
    flow.connect(join, lineGen);
    flow.connect(splitter.outputNoSplit, sink.input);
    flow.connect(lineGen, sink);

    StringBuilder paramValues = new StringBuilder();
    for (int i = 0; i < branchCount; ++i) {
      paramValues.append(i + ",");
    }

    Map<String, String> props = new HashMap<String, String>();
    props.put("start.request parameters", "hello=world\ngoodbye=moon\nNA=" + paramValues);
    props.put("splitter.Split src item", "NA");
    props.put("lineGen.result item names", "requestID,task_says,goodbye");
    props.put("task.Result items", "task_says=hello world");
    props.put("task.time(s)", taskTime);

    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
      .expectMsgSentCount(start, 1L)
      .expectMsgReceiptCount(join, (long) branchCount)
      .expectMsgReceiptCount(sink, 1L)
      .assertFlow(flow);
  }
}
