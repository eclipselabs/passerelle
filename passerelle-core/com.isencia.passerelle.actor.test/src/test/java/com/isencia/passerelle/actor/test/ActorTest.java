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
package com.isencia.passerelle.actor.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import ptolemy.actor.Manager;
import ptolemy.actor.Manager.State;
import ptolemy.data.StringToken;
import com.isencia.passerelle.actor.control.Stop;
import com.isencia.passerelle.actor.control.Trigger;
import com.isencia.passerelle.actor.convert.HeaderModifier;
import com.isencia.passerelle.actor.error.ErrorCatcher;
import com.isencia.passerelle.actor.error.ErrorObserver;
import com.isencia.passerelle.actor.filter.HeaderFilter;
import com.isencia.passerelle.actor.general.CommandExecutor;
import com.isencia.passerelle.actor.general.Const;
import com.isencia.passerelle.actor.general.DevNullActor;
import com.isencia.passerelle.actor.v5.Actor;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.model.FlowNotExecutingException;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.AsynchDelay;
import com.isencia.passerelle.testsupport.actor.ForLoop;
import com.isencia.passerelle.testsupport.actor.MapBasedRouter;
import com.isencia.passerelle.testsupport.actor.MapSource;
import com.isencia.passerelle.testsupport.actor.MessageHistoryStack;
import com.isencia.passerelle.testsupport.actor.TextSource;

public class ActorTest extends TestCase {
  private Flow flow;
  private FlowManager flowMgr = FlowManager.getDefault();

  /**
   * A unit test for a plain model with Error Observer
   * This model should never stop by itself.
   * 
   * @throws Exception
   */
  public void testFlowWithErrorObserver() throws Exception {
    flow = new Flow("testFlowWithErrorObserver", null);
    flow.setDirector(new Director(flow, "director"));

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
    Thread.sleep(5000);
    // the flow will still be running
    try {
      State state = flowMgr.getLocalExecutionState(flow);
      assertEquals("Flow must still be iterating", Manager.ITERATING, state);
      flowMgr.stopExecution(flow, 1000);
      
      new FlowStatisticsAssertion()
      .expectMsgSentCount(source, 1L)
      .expectMsgReceiptCount(sink, 1L)
      .assertFlow(flow);
    } catch (FlowNotExecutingException e) {
      // hmmm weird...
      fail("Flow must still be iterating");
    }

  }

  /**
   * A unit test for a plain model with Error Observer
   * 
   * @throws Exception
   */
  public void testFlowWithErrorObserverAndStop() throws Exception {
    flow = new Flow("testFlowWithErrorObserverAndStop", null);
    flow.setDirector(new Director(flow, "director"));

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
    flow.setDirector(new Director(flow, "director"));

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


  /**
   * A unit test for a plain HelloPasserelle model.
   * 
   * @throws Exception
   */
  public void testHelloPasserelle() throws Exception {
    flow = new Flow("testHelloPasserelle", null);
    flow.setDirector(new Director(flow, "director"));

    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");

    flow.connect(source, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }

  public void testConcurrentInputsOnHeaderModifier() throws Exception {
    flow = new Flow("testConcurrentInputsOnHeaderModifier", null);
    flow.setDirector(new Director(flow, "director"));

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

    new FlowStatisticsAssertion().expectMsgReceiptCount(sink, 9L).assertFlow(flow);
  }

  public void testConcurrentInputsOnHeaderFilter() throws Exception {
    flow = new Flow("testConcurrentInputsOnHeaderFilter", null);
    flow.setDirector(new Director(flow, "director"));

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

    new FlowStatisticsAssertion().
    expectMsgReceiptCount(sink1, 3L).
    expectMsgReceiptCount(sink2, 6L).
    assertFlow(flow);
  }

  /**
   * A unit test for a plain HelloPasserelle model but with an additional Trigger for the Const.
   * 
   * @throws Exception
   */
  public void testHelloPasserelleWithTrigger() throws Exception {
    flow = new Flow("testHelloPasserelleWithTrigger", null);
    flow.setDirector(new Director(flow, "director"));

    Trigger trigger = new Trigger(flow, "trigger");
    Const source = new Const(flow, "Constant");
    DevNullActor sink = new DevNullActor(flow, "sink");

    flow.connect(trigger.output, source.trigger);
    flow.connect(source, sink);

    Map<String, String> props = new HashMap<String, String>();
    props.put("Constant.value", "Hello world");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(source, 1L)
    .expectMsgReceiptCount(sink, 1L)
    .assertFlow(flow);
  }

  public void testMapTestActors() throws Exception {
    Flow flow = new Flow("testMapTestActors", null);
    Director d = new Director(flow,"director");
    MapSource src = new MapSource(flow, "src");
    MapBasedRouter router = new MapBasedRouter(flow, "router");
    router.outputPortCfgExt.outputPortNamesParameter.setToken(new StringToken("1,2"));
    DevNullActor sink1 = new DevNullActor(flow, "sink1");
    DevNullActor sink2 = new DevNullActor(flow, "sink2");
    flow.connect(src, router);
    flow.connect((Port)router.getPort("1"), sink1.input);
    flow.connect((Port)router.getPort("2"), sink2.input);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("src.entries", "pol=2");
    props.put("router.key", "pol");
    new FlowManager().executeBlockingLocally(flow, props);
    
    new FlowStatisticsAssertion()
    .expectMsgSentCount(src, 1L)
    .expectMsgReceiptCount(sink1, 0L)
    .expectMsgReceiptCount(sink2, 1L)
    .assertFlow(flow);
  }

  //TODO fix this test so that it runs on linux as well
  public void __testCommandExecutor() throws Exception {
    flow = new Flow("testCommandExecutor", null);
    flow.setDirector(new Director(flow, "director"));

    CommandExecutor cmdExecutor = new CommandExecutor(flow, "cmdExecutor");
    MessageHistoryStack cmdStdOutSink = new MessageHistoryStack(flow, "cmdStdOutSink");
    MessageHistoryStack cmdStdErrSink = new MessageHistoryStack(flow, "cmdStdErrSink");
    MessageHistoryStack cmdExitErrorSink = new MessageHistoryStack(flow, "cmdExitErrorSink");
    ErrorCatcher errorCatcher = new ErrorCatcher(flow, "errorCatcher");

    flow.connect(cmdExecutor.cmdOut, cmdStdOutSink.input);
    flow.connect(cmdExecutor.cmdErr, cmdStdErrSink.input);
    flow.connect(cmdExecutor.errorPort, errorCatcher.input);
    flow.connect(errorCatcher.errorDescrOutput, cmdExitErrorSink.input);

    String scriptPath = getTempFilePath("/runEchoes.bat");//bat file does not work on *nux systems
    Map<String, String> props = new HashMap<String, String>();
    props.put("cmdExecutor.command", scriptPath);
    props.put("errorCatcher.Log received messages", "true");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(cmdExecutor.cmdOut, 2L)
    .expectMsgSentCount(cmdExecutor.cmdErr, 1L)
    .expectMsgSentCount(cmdExecutor.errorPort, 0L)
    .assertFlow(flow);
    
    assertEquals("Wrong last stdOut", "and now for something completely different", cmdStdOutSink.poll().getBodyContentAsString());
    assertEquals("Wrong first stdOut", "this is a first output to stdout", cmdStdOutSink.poll().getBodyContentAsString());
    assertEquals("Wrong stdErr", "some error msg", cmdStdErrSink.poll().getBodyContentAsString());
  }

  private String getTempFilePath(String resourcePath) throws URISyntaxException, IOException {
    File tempFile = File.createTempFile("script", ".bat");
    FileUtils.copyURLToFile(this.getClass().getResource(resourcePath), tempFile);
    return tempFile.getAbsolutePath();
  }

  //TODO fix this test so that it runs on linux as well
  public void __testCommandExecutorWithErrorExit() throws Exception {
    flow = new Flow("testCommandExecutorWithErrorExit", null);
    flow.setDirector(new Director(flow, "director"));

    CommandExecutor cmdExecutor = new CommandExecutor(flow, "cmdExecutor");
    MessageHistoryStack cmdStdOutSink = new MessageHistoryStack(flow, "cmdStdOutSink");
    MessageHistoryStack cmdStdErrSink = new MessageHistoryStack(flow, "cmdStdErrSink");
    MessageHistoryStack cmdExitErrorSink = new MessageHistoryStack(flow, "cmdExitErrorSink");
    ErrorCatcher errorCatcher = new ErrorCatcher(flow, "errorCatcher");

    flow.connect(cmdExecutor.cmdOut, cmdStdOutSink.input);
    flow.connect(cmdExecutor.cmdErr, cmdStdErrSink.input);
    flow.connect(cmdExecutor.errorPort, errorCatcher.input);
    flow.connect(errorCatcher.errorDescrOutput, cmdExitErrorSink.input);

    String scriptPath = getTempFilePath("/runEchoesWithErrorExit.bat");//bat file does not work on *nux systems
    Map<String, String> props = new HashMap<String, String>();
    props.put("cmdExecutor.command", scriptPath);
    props.put("errorCatcher.Log received messages", "true");
    flowMgr.executeBlockingLocally(flow, props);

    new FlowStatisticsAssertion()
    .expectMsgSentCount(cmdExecutor.cmdOut, 2L)
    .expectMsgSentCount(cmdExecutor.cmdErr, 1L)
    .expectMsgSentCount(cmdExecutor.errorPort, 1L)
    .expectMsgReceiptCount(cmdExitErrorSink.input, 1L)
    .assertFlow(flow);
    
    assertTrue("Wrong exit code", cmdExitErrorSink.poll().getBodyContentAsString().contains("Exit : 13"));
  }

  // temporarily commented out until build issue with this test is resolved
  // it works perfectly in workspace JUnit testing...
//  public void testReadMoml() throws Exception {
//    Reader in = new InputStreamReader(getClass().getResourceAsStream("/test.xml"));
//    Flow f = FlowManager.readMoml(in);
//    Map<String, String> props = new HashMap<String, String>();
//    props.put("constant1.value", "howdy madurodam");
//    props.put("console1.Chop output at #chars", "200");
//    flowMgr.executeBlockingLocally(f,props);
//    System.out.println("Finished");
//  }
}
