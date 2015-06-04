/*
 * (c) Copyright 2001-2007, iSencia Belgium NV
 * All Rights Reserved.
 *
 * This software is the proprietary information of iSencia Belgium NV.
 * Use is subject to license terms.
 */
package com.isencia.passerelle.actor.advanced;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.v3.ActorContext;
import com.isencia.passerelle.actor.v3.ProcessRequest;
import com.isencia.passerelle.actor.v3.ProcessResponse;
import com.isencia.passerelle.core.ErrorCode;

/**
 * <p>
 * Passerelle actors support multi-threaded handling of requests. To enable this feature, the actor implementation must declare the desired nr of threads in its
 * super() constructor invocation. <br>
 * For multiple threads, requests are passed to a threadpool via a queue. The process() method is then invoked by a thread from this threadpool, i.e. not from
 * the main actor thread. <br>
 * In order to guarantee a correct grouping/ordering of output messages, response objects are also handled via an intermediate queue which is processed by one
 * "output sending" thread. (again not the actor's main thread!)
 * </p>
 * <p>
 * A consequence of this multi-threading support is the split of the doFire() method in 3 parts, so these can be invoked from separate handling scopes (in
 * different runnables).
 * </p>
 * REMARK : this is beta-level implementation. Exact consequences for actor lifecycle mgmt are not thoroughly understood yet!
 * 
 * @author erwin.de.ley@isencia.be
 */
public abstract class MultiThreadedActor extends AdvancedActor {
  private static final long serialVersionUID = 1L;
  private final static Logger LOGGER = LoggerFactory.getLogger(MultiThreadedActor.class);
  private final static Logger PERFORMANCELOGGER = LoggerFactory.getLogger("performance.sequence");

  protected class RequestProcessor implements Callable<ProcessResponse> {
    private ProcessRequest request;

    public RequestProcessor(ProcessRequest req) {
      this.request = req;
    }

    public ProcessResponse call() {
      ActorContext ctxt = new ActorContext();
      ProcessResponse response = null;
      try {
        doFire_ValidationPart(request, ctxt);
        response = doFire_ProcessingPart(request, ctxt);
        responseQueue.put(response);
      } catch (ProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        if (PERFORMANCELOGGER.isDebugEnabled()) {
          PERFORMANCELOGGER.debug("at the end: " + getStatus());
        }
      }
      return response;
    }
  }

  protected String getStatus() {
    int corePoolSize = processingExecutor.getCorePoolSize();
    int maxPoolSize = processingExecutor.getMaximumPoolSize();

    int currentPoolSize = processingExecutor.getPoolSize();
    int largestPoolSize = processingExecutor.getLargestPoolSize();

    int activeCount = processingExecutor.getActiveCount();
    long completedTaskCount = processingExecutor.getCompletedTaskCount();
    long scheduledCount = processingExecutor.getTaskCount();

    StringBuilder strB = new StringBuilder("[Actor " + getName() + " -- Usage Status : ");
    strB.append(" Resource usage Current/MaxUsed/Core/Max : " + currentPoolSize + "/" + largestPoolSize + "/" + corePoolSize + "/" + maxPoolSize);
    strB.append(" Task usage Active/Completed/Scheduled : " + activeCount + "/" + completedTaskCount + "/" + scheduledCount);
    strB.append("]");
    return strB.toString();
  }

  protected class RequestQueueDepleter implements Runnable {
    // flag to manage a nice shutdown
    private boolean keepOnRunning = true;

    public void run() {
      try {
        while (keepOnRunning) {
          ProcessRequest req = requestQueue.take();
          if (!FinishRequest.FINISH_REQUEST_MARKER.equals(req)) {
            // might we use this future for some usefull things later on??
            // Future<ProcessResponse> futureResponse =
            boolean accepted = false;
            while (!accepted) {
              try {
                if (PERFORMANCELOGGER.isDebugEnabled()) {
                  PERFORMANCELOGGER.debug(getStatus());
                }

                processingExecutor.submit(new RequestProcessor(req));
                accepted = true;
                if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("request accepted :" + req);
                }
              } catch (RejectedExecutionException e) {
                if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("request rejected :" + req);
                }
                Thread.sleep(1000);
              }
            }
          } else {
            // need to wait till all previous requests have been processed
            // TODO maybe implement as a parameter the option to allow abrupt termination
            // or the need to always be sure that all requests finish their normal processing...
            processingExecutor.shutdown();
            boolean processingDone = false;
            while (!processingDone) {
              processingDone = processingExecutor.awaitTermination(10, TimeUnit.SECONDS);
              if (!processingDone && LOGGER.isWarnEnabled()) {
                LOGGER.warn("Trying to wrapup, but request processing still busy. Waiting a bit more...");
              }
            }
            if (LOGGER.isInfoEnabled()) {
              LOGGER.info("Shutting down request queue");
            }
            keepOnRunning = false;
            responseQueue.offer(FinishResponse.FINISH_RESPONSE_MARKER);
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        requestQueueDepletionExecutor.shutdown();
      }
    }
  }

  protected class ResponseQueueDepleter implements Runnable {
    // flag to manage a nice shutdown
    private boolean keepOnRunning = true;

    public void run() {
      try {
        while (keepOnRunning) {
          ProcessResponse response = responseQueue.take();
          if (!FinishResponse.FINISH_RESPONSE_MARKER.equals(response)) {
            doFire_SendOutputPart(response);
          } else {
            if (LOGGER.isInfoEnabled()) {
              LOGGER.info("Shutting down response queue");
            }
            keepOnRunning = false;
          }
        }
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        responseQueueDepletionExecutor.shutdown();
        shutdownDone = true;
      }
    }
  }

  // Below are some extra things, meant to be able to support multi-threaded
  // actors.
  // These use their "main" actor thread to read inputs and prepare a Request
  // object.
  // Then they use a thread pool (JDK executor) to allow processing multiple
  // requests concurrently,
  // e.g. when the processing takes longer than the arrival rate of input
  // messages.
  // Resulting Response objects are placed on a queue again, from which they
  // are handled
  // sequentially again.

  // default values :

  private final static int defaultMaxNrThreads = 1;
  private final static int defaultMinNrThreads = 1;
  private final static int defaultKeepAliveTime = 10000;
  private final static int defaultThreadPoolQueueSize = 10;

  private int maxNrThreads;
  private int minNrThreads;
  private int keepAliveTime;
  private int threadPoolQueueSize;

  private Parameter maxNrThreadsParameter;
  private Parameter minNrThreadsParameter;
  private Parameter keepAliveTimeParameter;
  private Parameter threadPoolQueueSizeParameter;
  private BlockingQueue<ProcessRequest> requestQueue;
  private BlockingQueue<ProcessResponse> responseQueue;
  private ExecutorService requestQueueDepletionExecutor;
  private ExecutorService responseQueueDepletionExecutor;
  private ThreadPoolExecutor processingExecutor;

  private boolean shutdownDone = false;

  /**
   * @param container
   * @param name
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public MultiThreadedActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    maxNrThreadsParameter = new Parameter(this, "Max threads", new IntToken(defaultMaxNrThreads));
    minNrThreadsParameter = new Parameter(this, "Min threads", new IntToken(defaultMinNrThreads));
    keepAliveTimeParameter = new Parameter(this, "Keep Alive[ms]", new IntToken(defaultKeepAliveTime));
    threadPoolQueueSizeParameter = new Parameter(this, "Queue size", new IntToken(defaultThreadPoolQueueSize));
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }
  
  protected void doInitialize() throws InitializationException {
    try {
      maxNrThreads = ((IntToken) maxNrThreadsParameter.getToken()).intValue() > 0 ? ((IntToken) maxNrThreadsParameter.getToken()).intValue()
          : defaultMaxNrThreads;
      minNrThreads = ((IntToken) minNrThreadsParameter.getToken()).intValue() > 0 ? ((IntToken) minNrThreadsParameter.getToken()).intValue()
          : defaultMinNrThreads;
      keepAliveTime = ((IntToken) keepAliveTimeParameter.getToken()).intValue() > 0 ? ((IntToken) keepAliveTimeParameter.getToken()).intValue()
          : defaultKeepAliveTime;
      threadPoolQueueSize = ((IntToken) threadPoolQueueSizeParameter.getToken()).intValue() > 0 ? ((IntToken) threadPoolQueueSizeParameter.getToken())
          .intValue() : defaultThreadPoolQueueSize;
    } catch (IllegalActionException e) {
      throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading thread params", null, e);
    }

    if (minNrThreads > maxNrThreads) {
      minNrThreads = maxNrThreads;
    }

    super.doInitialize();
    shutdownDone = false;

    if (maxNrThreads > 1) {
      requestQueue = new LinkedBlockingQueue<ProcessRequest>();
      responseQueue = new LinkedBlockingQueue<ProcessResponse>();

      requestQueueDepletionExecutor = Executors.newSingleThreadExecutor();
      try {
        requestQueueDepletionExecutor.execute(new RequestQueueDepleter());
      } catch (RejectedExecutionException e) {
        throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Failed to start request Q management for a multithreaded actor", this, e);
      }
      responseQueueDepletionExecutor = Executors.newSingleThreadExecutor();
      try {
        responseQueueDepletionExecutor.execute(new ResponseQueueDepleter());
      } catch (RejectedExecutionException e) {
        throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Failed to start response Q management for a multithreaded actor", this, e);
      }
      processingExecutor = new ThreadPoolExecutor(minNrThreads, maxNrThreads, keepAliveTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
          threadPoolQueueSize));

    }
  }

  @Override
  protected void doFire_HandleRequest(ProcessRequest req) throws ProcessingException {
    if (maxNrThreads < 2) {
      super.doFire_HandleRequest(req);
    } else if (!isFinishRequested() && !requestQueue.contains(FinishRequest.FINISH_REQUEST_MARKER)) {
      // 1st test above is very fast and assures that "in regime" the request receipt is done as efficiently as possible
      // only during shutdown handling, the 2nd test is done to prevent race conditions with queue additions
      requestQueue.add(req);
    }
  }

  // need to find a way to ensure that the last outgoign msgs
  // generated by busy request processing while the requestFinish was done
  // can get sent out without TerminateProcessExceptions.
  // should be by blocking the main iteration thread somewhere, e.g. here
  // untill the processing has been nicely finished.
  // but the trial below causes the actor/processing to not terminate at all somehow...
  // @Override
  // protected boolean doPostFire() throws ProcessingException {
  // boolean superDone = super.doPostFire();
  // if(nrThreads<2 || ! superDone) {
  // return superDone;
  // } else {
  // // //it's a multithreaded one, in it's shutdown sequence,
  // // //so we need to wait here till the shutdown sequence is finished...
  // // try {
  // // boolean processingDone = false;
  // // while(!processingDone) {
  // // processingDone = responseQueueDepletionExecutor.awaitTermination(10, TimeUnit.SECONDS);
  // // if(!processingDone && logger.isWarnEnabled()) {
  // // logger.warn("Trying to wrapup, but response queue still busy. Waiting a bit more...");
  // // }
  // // }
  // // } catch (InterruptedException e) {
  // // // ignore
  // // }
  // return false;
  // }
  // }

  @Override
  protected void doStopFire() {
    super.doStopFire();
    if (maxNrThreads < 2) {
      // shutdown will be nice automatically
      shutdownDone = true;
      return;
    } else {
      // use a marker request object,
      // to allow nice handling of pending requests in the queue
      // and then do a nice shutdown
      requestQueue.add(FinishRequest.FINISH_REQUEST_MARKER);
    }
  }

  private static class FinishRequest extends ProcessRequest {
    public final static FinishRequest FINISH_REQUEST_MARKER = new FinishRequest();

    private FinishRequest() {
    }
  }

  private static class FinishResponse extends ProcessResponse {
    public final static FinishResponse FINISH_RESPONSE_MARKER = new FinishResponse();

    private FinishResponse() {
      super(FinishRequest.FINISH_REQUEST_MARKER);
    }
  }
}
