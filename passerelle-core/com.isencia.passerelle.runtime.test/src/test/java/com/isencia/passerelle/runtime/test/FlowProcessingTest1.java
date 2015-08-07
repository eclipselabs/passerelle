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
package com.isencia.passerelle.runtime.test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.domain.cap.Director;
import com.isencia.passerelle.domain.et.ETDirector;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.ProcessHandle;
import com.isencia.passerelle.runtime.process.FlowNotExecutingException;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.process.FlowProcessingService.StartMode;
import com.isencia.passerelle.runtime.process.ProcessStatus;
import com.isencia.passerelle.runtime.process.impl.FlowProcessingServiceImpl;
import com.isencia.passerelle.runtime.repos.impl.filesystem.FlowRepositoryServiceImpl;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.ExceptionGenerator;

public class FlowProcessingTest1 extends TestCase {

  private static final File userHome = new File(System.getProperty("user.home"));
  private static final File defaultRootFolderPath = new File(userHome, ".passerelle/passerelle-repository");
  private static final String REPOS_ROOTFOLDER = System.getProperty("com.isencia.passerelle.repository.root", defaultRootFolderPath.getAbsolutePath());

  public static FlowProcessingService processingService;
  public static FlowRepositoryService repositoryService;

  @Override
  protected void setUp() throws Exception {
    if (processingService == null) {
      processingService = new FlowProcessingServiceImpl();
    }
    if (repositoryService == null) {
      File repositoryRootFolder = new File(REPOS_ROOTFOLDER);
      FileUtils.deleteDirectory(repositoryRootFolder);
      repositoryService = new FlowRepositoryServiceImpl(repositoryRootFolder);
    } else {
      // this is a bit of a hack, assuming that when we run this test on a REST client facade,
      // the system property for the backing repos root folder has been set to the same location
      // for the server-side and this test-client-side.
      // we need to ensure the repos is cleared before each test, to avoid DuplicateEntryExceptions....
      File repositoryRootFolder = new File(REPOS_ROOTFOLDER);
      // due to asynchronous stuff going on, file/folder locks may still hang around a bit,
      // so we need to be prepared to retry...
      boolean deleteOk = false;
      while (!deleteOk) {
        try {
          FileUtils.deleteDirectory(repositoryRootFolder);
          deleteOk = true;
        } catch (IOException e) {
        }
      }
      repositoryRootFolder.mkdirs();
    }
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public final void testStartAndCheckProcessHandle() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartAndCheckProcessHandle", buildTrivialFlow("testStartAndCheckProcessHandle"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    assertNotNull("Process handle must be not-null", procHandle);
    assertNotNull("Process handle must have a non-null process context ID", procHandle.getProcessId());
    assertNotNull("Process status must be not-null", procHandle.getExecutionStatus());
    assertNotNull("Process's flow must be not-null", procHandle.getFlowHandle());
    assertEquals("Process's flow code must be as defined", "testStartAndCheckProcessHandle", procHandle.getFlowHandle().getCode());
  }

  public final void testStartFlowWithPreinitError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartFlowWithPreinitError", buildPreInitErrorFlow("testStartFlowWithPreinitError"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
  }

  public final void testStartFlowWithProcessError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartFlowWithProcessError", buildProcessErrorFlow("testStartFlowWithProcessError"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
  }
  
//  This one causes blocked threads on the runtime. Wrapup exceptions in Process domain cause such a problem.
//  public final void testStartFlowWithWrapupError() throws Exception {
//    FlowHandle flowHandle = repositoryService.commit("testStartFlowWithWrapupError", buildWrapupErrorFlow("testStartFlowWithWrapupError"));
//    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
//    Thread.sleep(500);
//    procHandle = processingService.refresh(procHandle);
//    assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
//  }

  public final void testStartETFlowWithWrapupError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartETFlowWithWrapupError", buildWrapupErrorFlowET("testStartETFlowWithWrapupError"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
  }

  public final void testGetHandle() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testGetHandle", buildDelay100msFlow("testGetHandle"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    ProcessHandle procHandle2 = processingService.getHandle(procHandle.getProcessId());
    assertEquals("Process handle from start() should be equal to one returned by getHandle()", procHandle, procHandle2);
  }

  public final void testRefresh() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testRefresh", buildTrivialFlow("testRefresh"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    assertNotNull("Process handle must be not-null", procHandle);
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should have finished OK", ProcessStatus.FINISHED, procHandle.getExecutionStatus());
  }

  // with this one, we hope to invoke terminate before the actual execution has started
  public final void testTerminateImmediately() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testTerminateImmediately", buildDelay100msFlow("testTerminateImmediately"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    ProcessHandle procHandle2 = processingService.terminate(procHandle);
    // this is a bit risky, as we can not strictly be certain that the process was indeed terminated/canceled before its start...
    // assertTrue("Process should have terminated", procHandle2.getExecutionStatus().isFinalStatus());
    // then we just let it die
    Thread.sleep(500);
    ProcessHandle procHandle3 = processingService.refresh(procHandle2);
    assertTrue("Process should have terminated", procHandle3.getExecutionStatus().isFinalStatus());
  }

  // with this one, we hope to invoke terminate when the actual execution has started
  public final void testTerminateAfterSomeTime() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testTerminateAfterSomeTime", buildDelay100msFlow("testTerminateAfterSomeTime"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    Thread.sleep(100);
    ProcessHandle procHandle2 = processingService.refresh(procHandle);
    assertEquals("Process should have started", ProcessStatus.ACTIVE, procHandle2.getExecutionStatus());
    processingService.terminate(procHandle);
    // then we just let it die
    Thread.sleep(500);
    ProcessHandle procHandle3 = processingService.refresh(procHandle2);
    assertTrue("Process should have terminated", procHandle3.getExecutionStatus().isFinalStatus());
  }

  public final void testWaitUntilFinished() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitUntilFinished", buildDelay100msFlow("testWaitUntilFinished"));
    long startTime = new Date().getTime();
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    procHandle = processingService.waitUntilFinished(procHandle, 3, TimeUnit.SECONDS);
    long endTime = new Date().getTime();
    assertTrue("Process should have terminated", procHandle.getExecutionStatus().isFinalStatus());
    assertTrue("Process should last for at least 100ms", (endTime - startTime) > 100);
  }

  public final void testWaitUntilFinishedMultiRunsSameFlow() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitUntilFinishedMultiRunsSameFlow", buildDelay100msFlow("testWaitUntilFinishedMultiRunsSameFlow"));
    Set<ProcessHandle> handles = new HashSet<ProcessHandle>();
    for(int i=0;i<5;++i) {
      Map<String, String> params = new HashMap<String, String>();
      params.put("const.value", "msg"+i);
      handles.add(processingService.start(StartMode.RUN, flowHandle, null, params, null));
    }
    for (ProcessHandle procHandle : handles) {
      procHandle = processingService.waitUntilFinished(procHandle, 5, TimeUnit.SECONDS);
      assertTrue("Process should have terminated", procHandle.getExecutionStatus().isFinalStatus());
    }
  }

  public final void testWaitForTerminatedProcess() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitForTerminatedProcess", buildDelay100msFlow("testWaitForTerminatedProcess"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    processingService.terminate(procHandle);
    try {
      procHandle = processingService.waitUntilFinished(procHandle, 1, TimeUnit.SECONDS);
      assertEquals("Process should be INTERRUPTED", ProcessStatus.INTERRUPTED, procHandle.getExecutionStatus());
    } catch (FlowNotExecutingException e) {
      // this is also possible
    }
  }

  public final void testWaitNotLongEnoughUntilFinished() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitNotLongEnoughUntilFinished", buildDelay100msFlow("testWaitNotLongEnoughUntilFinished"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    try {
      processingService.waitUntilFinished(procHandle, 50, TimeUnit.MILLISECONDS);
      fail("waitUntilFinished should have gone in timeout");
    } catch (TimeoutException e) {
      // this is as it should be
    }
  }

  public final void testWaitUntilFinishedOfFlowWithError() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testWaitUntilFinishedOfFlowWithError", buildPreInitErrorFlow("testWaitUntilFinishedOfFlowWithError"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    try {
      procHandle = processingService.waitUntilFinished(procHandle, 1, TimeUnit.SECONDS);
      // ico a remote execution, we're not able (yet?) to transfer ExecutionExceptions, and we'll just see that the status is ERROR
      assertEquals("Process should have finished in ERROR", ProcessStatus.ERROR, procHandle.getExecutionStatus());
    } catch (ExecutionException e) {
      // for local execution, an InitializationException will be so severe that the execution fails completely
      // TODO check how to implement this uniformly for remote executions as well
      assertTrue("Error cause should be an InitializationException", e.getCause() instanceof InitializationException);
    }
  }

  public final void testStartWithParameterOverrides() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testStartWithParameterOverrides", buildDelay100msFlow("testStartWithParameterOverrides"));
    Map<String, String> overrides = new HashMap<String, String>();
    overrides.put("delay.time(ms)", "2000");
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, overrides, null);
    try {
      procHandle = processingService.waitUntilFinished(procHandle, 1, TimeUnit.SECONDS);
      fail("Process should take approx 2s");
    } catch (TimeoutException e) {
      // this is as it should be
    }
    procHandle = processingService.waitUntilFinished(procHandle, 2, TimeUnit.SECONDS);
    assertTrue("Process should have terminated", procHandle.getExecutionStatus().isFinalStatus());
  }

  public final void testSuspendResume() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testSuspendResume", buildMultiDelay100msFlow("testSuspendResume"));
    ProcessHandle procHandle = processingService.start(StartMode.RUN, flowHandle, null, null, null);
    // an immediate suspend could happen BEFORE the model is already executing (as the start is an asynchronous)
    // this is OK as the processingService will maintain the suspension indicator and will suspend the execution
    // as soon as possible after its actual start.
    ProcessHandle suspendHandle = processingService.suspend(procHandle);
    // need to wait > 100ms here as the Delay actor in the test model REALLY blocks for its configured delay
    Thread.sleep(200);
    suspendHandle = processingService.refresh(suspendHandle);
    assertEquals("Process should be SUSPENDED", ProcessStatus.SUSPENDED, suspendHandle.getExecutionStatus());
    ProcessHandle resumeHandle = processingService.resume(suspendHandle);
    Thread.sleep(100);
    resumeHandle = processingService.refresh(resumeHandle);
    assertEquals("Process should be RESUMED", ProcessStatus.ACTIVE, resumeHandle.getExecutionStatus());
    processingService.waitUntilFinished(resumeHandle, 1, TimeUnit.SECONDS);
  }

  public final void testActorBreakpointResume() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testActorBreakpointResume", buildMultiDelay100msFlow("testActorBreakpointResume"));
    ProcessHandle procHandle = processingService.start(StartMode.DEBUG, flowHandle, null, null, null, "delay1");
    // need to wait a bit here to give the model time to start and hit the breakpoint
    Thread.sleep(500);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should be SUSPENDED", ProcessStatus.SUSPENDED, procHandle.getExecutionStatus());
    assertNotNull("Should have one suspended element", procHandle.getSuspendedElements());
    assertEquals("Should have one suspended element", procHandle.getSuspendedElements().length, 1);
    assertEquals("Suspended element should be delay1", "delay1", procHandle.getSuspendedElements()[0]);
    ProcessHandle resumeHandle = processingService.resume(procHandle);
    Thread.sleep(100);
    resumeHandle = processingService.refresh(resumeHandle);
    assertEquals("Process should be RESUMED", ProcessStatus.ACTIVE, resumeHandle.getExecutionStatus());
    try {
      processingService.waitUntilFinished(resumeHandle, 1, TimeUnit.SECONDS);
    } catch (Exception e) {
      fail("Process should have terminated normally in < 1 s");
    }
  }

  public final void testInputPortBreakpointResume() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testInputPortBreakpointResume", buildMultiDelay100msFlow("testInputPortBreakpointResume"));
    ProcessHandle procHandle = processingService.start(StartMode.DEBUG, flowHandle, null, null, null, "delay1.input");
    // need to wait a bit here to give the model time to start and hit the breakpoint
    Thread.sleep(200);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should be SUSPENDED", ProcessStatus.SUSPENDED, procHandle.getExecutionStatus());
    assertNotNull("Should have one suspended element", procHandle.getSuspendedElements());
    assertEquals("Should have one suspended element", procHandle.getSuspendedElements().length, 1);
    assertEquals("Suspended element should be delay1", "delay1.input", procHandle.getSuspendedElements()[0]);
    ProcessHandle resumeHandle = processingService.resume(procHandle);
    Thread.sleep(100);
    resumeHandle = processingService.refresh(resumeHandle);
    assertEquals("Process should be RESUMED", ProcessStatus.ACTIVE, resumeHandle.getExecutionStatus());
    try {
      processingService.waitUntilFinished(resumeHandle, 1, TimeUnit.SECONDS);
    } catch (Exception e) {
      fail("Process should have terminated normally in < 1 s");
    }
  }

  public final void testOutputPortBreakpointResume() throws Exception {
    FlowHandle flowHandle = repositoryService.commit("testOutputPortBreakpointResume", buildMultiDelay100msFlow("testOutputPortBreakpointResume"));
    ProcessHandle procHandle = processingService.start(StartMode.DEBUG, flowHandle, null, null, null, "const.output");
    // need to wait a bit here to give the model time to start and hit the breakpoint
    Thread.sleep(200);
    procHandle = processingService.refresh(procHandle);
    assertEquals("Process should be SUSPENDED", ProcessStatus.SUSPENDED, procHandle.getExecutionStatus());
    assertNotNull("Should have one suspended element", procHandle.getSuspendedElements());
    assertEquals("Should have one suspended element", procHandle.getSuspendedElements().length, 1);
    assertEquals("Suspended element should be delay1", "const.output", procHandle.getSuspendedElements()[0]);
    ProcessHandle resumeHandle = processingService.resume(procHandle);
    Thread.sleep(100);
    resumeHandle = processingService.refresh(resumeHandle);
    assertEquals("Process should be RESUMED", ProcessStatus.ACTIVE, resumeHandle.getExecutionStatus());
    try {
      processingService.waitUntilFinished(resumeHandle, 1, TimeUnit.SECONDS);
    } catch (Exception e) {
      fail("Process should have terminated normally in < 1 s");
    }
  }

  protected Flow buildTrivialFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new ETDirector(flow, "director"));
    Const source = new Const(flow, "const");
    DevNullActor sink = new DevNullActor(flow, "sink");
    sink.logReceivedMessages.setExpression("true");
    flow.connect(source, sink);
    return flow;
  }

  protected Flow buildPreInitErrorFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new ETDirector(flow, "director"));
    Const source = new Const(flow, "const");
    ExceptionGenerator excGen = new ExceptionGenerator(flow, "excGen");
    excGen.preInitExcParameter.setExpression("true");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, excGen);
    flow.connect(excGen, sink);
    return flow;
  }

  protected Flow buildProcessErrorFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new ETDirector(flow, "director"));
    Const source = new Const(flow, "const");
    ExceptionGenerator excGen = new ExceptionGenerator(flow, "excGen");
    excGen.processExcParameter.setExpression("true");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, excGen);
    flow.connect(excGen, sink);
    return flow;
  }

  protected Flow buildWrapupErrorFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new Director(flow, "director"));
    Const source = new Const(flow, "const");
    ExceptionGenerator excGen = new ExceptionGenerator(flow, "excGen");
    excGen.wrapupExcParameter.setExpression("true");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, excGen);
    flow.connect(excGen, sink);
    return flow;
  }

  protected Flow buildWrapupErrorFlowET(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new ETDirector(flow, "director"));
    Const source = new Const(flow, "const");
    ExceptionGenerator excGen = new ExceptionGenerator(flow, "excGen");
    excGen.wrapupExcParameter.setExpression("true");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, excGen);
    flow.connect(excGen, sink);
    return flow;
  }

  protected Flow buildDelay100msFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new ETDirector(flow, "director"));
    Const source = new Const(flow, "const");
    Delay delay = new Delay(flow, "delay");
    delay.timeParameter.setExpression("100");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, delay);
    flow.connect(delay, sink);
    return flow;
  }

  protected Flow buildMultiDelay100msFlow(String flowName) throws Exception {
    Flow flow = new Flow(flowName, null);
    flow.setDirector(new ETDirector(flow, "director"));
    Const source = new Const(flow, "const");
    Delay delay1 = new Delay(flow, "delay1");
    Delay delay2 = new Delay(flow, "delay2");
    Delay delay3 = new Delay(flow, "delay3");
    delay1.timeParameter.setExpression("100");
    delay2.timeParameter.setExpression("100");
    delay3.timeParameter.setExpression("100");
    DevNullActor sink = new DevNullActor(flow, "sink");
    flow.connect(source, delay1);
    flow.connect(delay1, delay2);
    flow.connect(delay2, delay3);
    flow.connect(delay3, sink);
    return flow;
  }

}
