package com.isencia.passerelle.domain.et.test;

import junit.framework.TestCase;

// this is sample code to try an alternative flow cloning via http://code.google.com/p/cloning/
// but it seems the execution of the cloned models fails
// we get dramatic JVM crashes then with illegal access errors...
// in order to retry this you need to add cloning & objenesis to the target
public class CloneViaClonerTest extends TestCase {
	
	public void testNeedToFixThis() {
		// TODO Auto-generated method stub

	}
  
//  public void testFlowClone() throws Exception {
//    Flow flow = new Flow("testHelloPasserelle",null);
//    ETDirector director = new ETDirector(flow,"director");
//    flow.setDirector(director);
//    Attribute expertModeParameter = director.getAttribute(DirectorAdapter.EXPERTMODE_PARAM);
//    Const source = new Const(flow,"Constant");
//    DevNullActor sink = new DevNullActor(flow, "sink");
//    flow.connect(source, sink);
//    
//    ((Parameter)director.getAttribute(DirectorAdapter.EXPERTMODE_PARAM)).setToken(BooleanToken.TRUE);
//    ((Parameter)director.getAttribute(DirectorAdapter.MOCKMODE_PARAM)).setToken(BooleanToken.TRUE);
//    ((Parameter)director.getAttribute(DirectorAdapter.STOP_FOR_UNHANDLED_ERROR_PARAM)).setToken(BooleanToken.TRUE);
//    flow.getDirectorAdapter().setErrorControlStrategy(new ErrorControlStrategy() {
//      public void handleTerminationException(Actor a, TerminationException e) throws IllegalActionException {
//      }
//      public void handlePreFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
//      }
//      public void handlePreFireException(Actor a, ProcessingException e) throws IllegalActionException {
//      }
//      public void handlePostFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
//      }
//      public void handlePostFireException(Actor a, ProcessingException e) throws IllegalActionException {
//      }
//      public void handleIterationValidationException(Actor actor, ValidationException e) throws IllegalActionException {
//      }
//      public void handleInitializationValidationException(Actor actor, ValidationException e) throws IllegalActionException {
//      }
//      public void handleInitializationException(Actor a, InitializationException e) throws IllegalActionException {
//      }
//      public void handleFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
//      }
//      public void handleFireException(Actor a, ProcessingException e) throws IllegalActionException {
//      }
//    }, true);
//
//    Cloner cloner = new Cloner();
//    Flow clone = cloner.deepClone(flow);
//    
//    new FlowDefinitionAssertion()
//      .expectActor(source.getFullName())
//      .expectActor(sink.getFullName())
//      .expectParameter(expertModeParameter.getFullName())
//      .expectRelation(source.output.getFullName(), sink.input.getFullName())
//      .assertFlow(flow)
//      .assertFlow(clone);
//    
//    assertEquals("Expert mode not cloned correctly", flow.getDirectorAdapter().isExpertMode(), clone.getDirectorAdapter().isExpertMode());
//    assertEquals("Mock mode not cloned correctly", flow.getDirectorAdapter().isMockMode(), clone.getDirectorAdapter().isMockMode());
//    assertEquals("Error stop mode not cloned correctly", flow.getDirectorAdapter().isStopForUnhandledError(), clone.getDirectorAdapter().isStopForUnhandledError());
//    assertEquals("Error ctrl strategy not cloned correctly", flow.getDirectorAdapter().getErrorControlStrategy().getClass().getName(), 
//        clone.getDirectorAdapter().getErrorControlStrategy().getClass().getName());
//  }
//
//  public void testFlowCloneExecution() throws Exception {
//    Flow flow = new Flow("testHelloPasserelle",null);
//    flow.setDirector(new ETDirector(flow,"director"));
//    Const source = new Const(flow,"Constant");
//    DevNullActor sink = new DevNullActor(flow, "sink");
//    flow.connect(source, sink);
//    
//    Cloner cloner = new Cloner();
//    Flow clone = cloner.deepClone(flow);
//    
//    FlowManager flowMgr = new FlowManager();
//
//    Map<String, String> props = new HashMap<String, String>();
//    props.put("Constant.value", "Hello world");
//
//    flowMgr.executeBlockingLocally(clone,props);
//
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(source, 1L)
//    .expectMsgReceiptCount(sink, 1L)
//    .assertFlow(clone);
//  }
//
//  public void testFlowClone100Times() throws Exception {
//    Flow flow = new Flow("testHelloPasserelle",null);
//    ETDirector director = new ETDirector(flow,"director");
//    flow.setDirector(director);
//    Attribute expertModeParameter = director.getAttribute(DirectorAdapter.EXPERTMODE_PARAM);
//    Const source = new Const(flow,"Constant");
//    DevNullActor sink = new DevNullActor(flow, "sink");
//    flow.connect(source, sink);
//    
//    ((Parameter)director.getAttribute(DirectorAdapter.EXPERTMODE_PARAM)).setToken(BooleanToken.TRUE);
//    ((Parameter)director.getAttribute(DirectorAdapter.MOCKMODE_PARAM)).setToken(BooleanToken.TRUE);
//    ((Parameter)director.getAttribute(DirectorAdapter.STOP_FOR_UNHANDLED_ERROR_PARAM)).setToken(BooleanToken.TRUE);
//    flow.getDirectorAdapter().setErrorControlStrategy(new ErrorControlStrategy() {
//      public void handleTerminationException(Actor a, TerminationException e) throws IllegalActionException {
//      }
//      public void handlePreFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
//      }
//      public void handlePreFireException(Actor a, ProcessingException e) throws IllegalActionException {
//      }
//      public void handlePostFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
//      }
//      public void handlePostFireException(Actor a, ProcessingException e) throws IllegalActionException {
//      }
//      public void handleIterationValidationException(Actor actor, ValidationException e) throws IllegalActionException {
//      }
//      public void handleInitializationValidationException(Actor actor, ValidationException e) throws IllegalActionException {
//      }
//      public void handleInitializationException(Actor a, InitializationException e) throws IllegalActionException {
//      }
//      public void handleFireRuntimeException(Actor a, RuntimeException e) throws IllegalActionException {
//      }
//      public void handleFireException(Actor a, ProcessingException e) throws IllegalActionException {
//      }
//    }, true);
//
//    Cloner cloner = new Cloner();
//    Flow clone = null;
//    for(int i=0; i<100; ++i) {
//      clone = cloner.deepClone(flow);
//    }
//    
//    new FlowDefinitionAssertion()
//      .expectActor(source.getFullName())
//      .expectActor(sink.getFullName())
//      .expectParameter(expertModeParameter.getFullName())
//      .expectRelation(source.output.getFullName(), sink.input.getFullName())
//      .assertFlow(flow)
//      .assertFlow(clone);
//    
//    assertEquals("Expert mode not cloned correctly", flow.getDirectorAdapter().isExpertMode(), clone.getDirectorAdapter().isExpertMode());
//    assertEquals("Mock mode not cloned correctly", flow.getDirectorAdapter().isMockMode(), clone.getDirectorAdapter().isMockMode());
//    assertEquals("Error stop mode not cloned correctly", flow.getDirectorAdapter().isStopForUnhandledError(), clone.getDirectorAdapter().isStopForUnhandledError());
//    assertEquals("Error ctrl strategy not cloned correctly", flow.getDirectorAdapter().getErrorControlStrategy().getClass().getName(), 
//        clone.getDirectorAdapter().getErrorControlStrategy().getClass().getName());
//  }
//
//  public void testFlowCloneExecution100Times() throws Exception {
//    Flow flow = new Flow("testHelloPasserelle",null);
//    flow.setDirector(new ETDirector(flow,"director"));
//    Const source = new Const(flow,"Constant");
//    DevNullActor sink = new DevNullActor(flow, "sink");
//    flow.connect(source, sink);
//    
//    Cloner cloner = new Cloner();
//    Flow clone = null;
//    for(int i=0; i<100; ++i) {
//      clone = cloner.deepClone(flow);
//    }
//    
//    FlowManager flowMgr = new FlowManager();
//
//    Map<String, String> props = new HashMap<String, String>();
//    props.put("Constant.value", "Hello world");
//
//    flowMgr.executeBlockingLocally(clone,props);
//
//    new FlowStatisticsAssertion()
//    .expectMsgSentCount(source, 1L)
//    .expectMsgReceiptCount(sink, 1L)
//    .assertFlow(clone);
//  }
//
}
