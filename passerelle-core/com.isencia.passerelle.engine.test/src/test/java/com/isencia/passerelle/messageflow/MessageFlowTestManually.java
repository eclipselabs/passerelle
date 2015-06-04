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
package com.isencia.passerelle.messageflow;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import ptolemy.actor.NoRoomException;
import ptolemy.data.IntToken;
import com.isencia.passerelle.domain.cap.CapDirector;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowStatisticsAssertion;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.RandomMatrixSource;
import com.isencia.passerelle.testsupport.actor.TextSource;
import com.isencia.passerelle.testsupport.utils.CollectingLog4JAppender;
import com.isencia.passerelle.testsupport.utils.ExecutionErrorCatcher;
import com.isencia.passerelle.testsupport.utils.ExecutionErrorCounter;

public class MessageFlowTestManually extends TestCase {

  private FlowManager flowMgr;

  @Override
  protected void setUp() throws Exception {
    flowMgr = new FlowManager();
    CollectingLog4JAppender.initWithPattern(".*delay.input - reached/passed warning threshold size 1");
    Logger.getRootLogger().removeAllAppenders();
    Logger.getRootLogger().addAppender(new CollectingLog4JAppender());
  }

  /**
   * A simple test on limited input queue sizes, 
   * i.e. where putting extra messages on a full receiver queue blocks until room is available again on the queue.
   * 
   * This mode allows to use a model in streaming mode for streams with large data volumes, without risking OutOfMemory issues.
   * 
   * @throws Exception
   */
  public void testReceiverQueueCapacityLimit() throws Exception {
    Flow flow = new Flow("ReceiverQueueCapacityLimit", null);
    CapDirector director = new CapDirector(flow, "director");
    flow.setDirector(director);
    director.maximumQueueCapacity.setToken(new IntToken(1));

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

  /**
   * A simple test on unlimited input queue sizes, which should cause an OutOfMemory.
   * 
   * @throws Exception
   */
  public void testReceiverQueueNoCapacityLimitWithOutOfMemory() throws Exception {
    try {
      Flow flow = new Flow("testReceiverQueueNoCapacityLimitWithOutOfMemory", null);
      CapDirector director = new CapDirector(flow, "director");
      flow.setDirector(director);
      director.initialQueueCapacity.setToken(new IntToken(65536));

      RandomMatrixSource source = new RandomMatrixSource(flow, "source");
      Delay delay = new Delay(flow, "delay");
      DevNullActor sink = new DevNullActor(flow, "sink");
      flow.connect(source, delay);
      flow.connect(delay, sink);

      Map<String, String> props = new HashMap<String, String>();
      props.put("source.Nr of matrices", "20000");
      props.put("source.Nr of rows", "100");
      props.put("source.Nr of columns", "100");
      props.put("delay.time(ms)", "1000");

      ExecutionErrorCatcher listener = new ExecutionErrorCatcher(OutOfMemoryError.class, ExecutionErrorCatcher.STOP_ON_ERROR);
      flowMgr.executeBlockingLocally(flow, props, listener );
      assertEquals("Should have received an OutOfMemory", 1, listener.getErrorCount());
    } catch (OutOfMemoryError e) {
      // all's well ;-)
    }
  }
  /**
   * A simple test on limited input queue sizes,
   * i.e. where extra messages get refused.
   * 
   * @throws Exception
   */
  public void testActorReceiverQueueCapacityLimit() throws Exception {
    Flow flow = new Flow("", null);
    flow.setDirector(new Director(flow, "director"));
    
    TextSource source = new TextSource(flow, "source");
    Delay delay = new Delay(flow, "delay");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source,delay);
    flow.connect(delay,sink);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("source.values", "Hello,world,again,and,again");
    props.put("delay.time(ms)", "100");
    props.put("delay.Receiver Q Capacity (-1)", "1");
    
    ExecutionErrorCounter listener = new ExecutionErrorCounter(NoRoomException.class);
    flowMgr.executeBlockingLocally(flow, props, listener);
    // at least 3 since the Delay actor does not block before retrieving its first message;
    // i.e. the second message is in fact the 1st one to stay in the receiver queue for a while,
    // and the third message will get refused then if it arrives within the same delay-timeslot.
    assertTrue("Should get at least 3 NoRoomExceptions", listener.getErrorCount()>=3);
  }
  
  /**
   * A simple test on size warnings for input queues,
   * i.e. where extra messages get through but warning messages are logged.
   * 
   * @throws Exception
   */
  public void testActorReceiverQueueCapacityWarning() throws Exception {
	
    Flow flow = new Flow("", null);
    flow.setDirector(new Director(flow, "director"));
    
    TextSource source = new TextSource(flow, "source");
    Delay delay = new Delay(flow, "delay");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source,delay);
    flow.connect(delay,sink);
    
    
    Map<String, String> props = new HashMap<String, String>();
    props.put("source.values", "Hello,world,again,and,again");
    props.put("delay.time(ms)", "100");
    props.put("delay.Receiver Q warning size (-1)", "1");
    
    flowMgr.executeBlockingLocally(flow, props);
    new FlowStatisticsAssertion()
      .expectMsgReceiptCount(sink, 5L)
      .assertFlow(flow);
    
    assertEquals("Should get 5 warnings",5,CollectingLog4JAppender.getLoggedMessages());
  }
}
