/**
 * 
 */
package com.isencia.passerelle.process.scheduler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.scheduler.ResourceToken;
import com.isencia.passerelle.process.scheduler.SchedulerException;
import com.isencia.passerelle.process.scheduler.TaskHandler;
import com.isencia.passerelle.process.scheduler.TaskRefusedException;
import com.isencia.passerelle.process.scheduler.congestionmanagement.CongestionManagementTaskScheduler;
import com.isencia.passerelle.process.scheduler.congestionmanagement.ResourceCongestionDefinitionStrategy;
import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClass;
import com.isencia.passerelle.process.scheduler.congestionmanagement.TaskClassifierStrategy;

/**
 * <p>
 * A DefaultScheduler implements a fair capacity assignment policy across context classes.
 * Entity classes can be defined as needed, and a different average capacity share
 * can be configured for each class.
 * </p>
 * <p>
 * A DefaultScheduler must be configured with the available size of the pool of identical resources
 * that is to be managed, and (optionally) with a maximum size for the queue for pending entities
 * (i.e. the queue where entities are buffered while all resources are occupied).
 * </p>
 * <p>
 * As long as the available resources are not congested, all incoming requests are immediately
 * handed to their handlers. "Busy" handlers are running in separate threads, managed by a thread pool
 * that is created and controlled by the DefaultScheduler.
 * <br>
 * As such, the context execution (while not in "congestion mode")
 * is arbitrary (but should be almost the order in which they are accepted).
 * </p>
 * <p>
 * When resources are becoming congested, the DefaultScheduler starts selecting/ordering
 * tasks according to a fairness criterion based on :
 * <ul>
 * <li> the predefined average capacity assignment for their context classes
 * <li> the current allocation of occupied resources for their context classes
 * </ul>
 * See the document of Tanguy Herriau (DARE-Congestion Management.doc)
 * for more details about the algorithm.
 * </p>
 *
 * @see TaskClass
 * @see TaskClassifierStrategy
 * @see ResourceCongestionDefinitionStrategy
 *
 * @author erwin
 *
 */
public class DefaultScheduler implements CongestionManagementTaskScheduler {

	private final static Logger LOGGER = LoggerFactory.getLogger(DefaultScheduler.class);
  private static final Logger PERFORMANCELOGGER = LoggerFactory.getLogger("performance.resource");
	
	private TaskClassifierStrategy taskClassifierStrategy;
	private ResourceCongestionDefinitionStrategy resourceCongestionDefinitionStrategy;
	
	// Queue of tokens that represent pool resources for task processing.
	private final BlockingQueue<ResourceToken> tokens;

	// Task buffers per task class
	private final Map<TaskClass, Queue<TaskEntry>> taskQueues = new HashMap<TaskClass, Queue<TaskEntry>>();
	
	// locking utilities for reliably accepting/delivering entities
	private final ReentrantLock taskQLock = new ReentrantLock(true);
	private final Condition taskQNotEmpty = taskQLock.newCondition();

	// map to track current resource allocation per task class
	private final Map<TaskClass, Collection<ResourceToken>> busyTokens = new HashMap<TaskClass, Collection<ResourceToken>>();

	private final ResourceUsageStatus usageStatus;

	// 1-thread executor for the entityBufferSink
	private final ExecutorService queueDepletionExecutor;
	
	// thread pool for the context handlers
	private final ExecutorService taskHandlingExecutor;

	// state variables for managing the shutdown sequence
	private boolean active = true;
	private boolean forcedShutdown = false;

	private final String schedulerName;
//	private IEclipsePreferences configNode;
//	private final String configNodeName;
//	private double loadMonitoringThreshold = 0.5;
//	private double bufferOccupationMonitoringThreshold = 0.2;
//	private static final String CONFIG_NODENAME_PREFIX = "monitoring/resources/";

	public DefaultScheduler(String schedulerName, int nrOfResources, int maxPendingQueueSize) {
		
		this.schedulerName = schedulerName;
//    this.configNodeName = CONFIG_NODENAME_PREFIX + schedulerName;
		tokens = new LinkedBlockingQueue<ResourceToken>(nrOfResources);
		for (int idx = 0; idx < nrOfResources; ++idx) {
			tokens.offer(new SchedulerAwareResourceToken(this));
		}
		
		resourceCongestionDefinitionStrategy = new DefaultCongestionDefinitionStrategy();
		
		int pendingQueueCapacity = Integer.MAX_VALUE;
		if (maxPendingQueueSize > 0) {
			pendingQueueCapacity = maxPendingQueueSize;
		}

		usageStatus = new ResourceUsageStatus(schedulerName, nrOfResources, 0, 0, pendingQueueCapacity, 0, 0);
		
		queueDepletionExecutor = Executors.newSingleThreadExecutor();
		try {
			queueDepletionExecutor.execute(new TaskBufferSink());
		} catch (RejectedExecutionException ex) {
			LOGGER.error(ErrorCode.SYSTEM_ERROR + " - failure to launch queueDepletionExecutor", ex);
		}
		
		taskHandlingExecutor = Executors.newFixedThreadPool(nrOfResources);
	}
	
	public String getName() {
		return schedulerName;
	}
/*
  public void configure() {
    if(configNode==null) {
      configNode = (IEclipsePreferences)PreferenceUtils.getThresholdNode().node(getConfigurationNodeName());
    }
    
    // default values from parent node, or from hard-coded value if parent pref doesn't exist
    // remark that we assume that the parent node always exists!!!
    loadMonitoringThreshold = configNode.getDouble("resource.load", configNode.parent().getDouble("resource.load", 0.5));
    bufferOccupationMonitoringThreshold = configNode.getDouble("buffer.load", configNode.parent().getDouble("buffer.load", 0.2));
  }

  public String getConfigurationNodeName() {
    return configNodeName;
  }
*/
	public TaskClassifierStrategy getTaskClassifierStrategy() {
		return taskClassifierStrategy;
	}

	public void setTaskClassifierStrategy(TaskClassifierStrategy entityClassifierStrategy) {
		this.taskClassifierStrategy = entityClassifierStrategy;
	}

	public ResourceCongestionDefinitionStrategy getResourceCongestionDefinitionStrategy() {
		return resourceCongestionDefinitionStrategy;
	}

	public void setResourceCongestionDefinitionStrategy(
			ResourceCongestionDefinitionStrategy resourceCongestionDefinitionStrategy) {
		this.resourceCongestionDefinitionStrategy = resourceCongestionDefinitionStrategy;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.TaskScheduler#accept(com.isencia.passerelle.diagnosis.LifeCycleEntity, com.isencia.passerelle.process.scheduler.TaskHandler)
	 */
	public void accept(Context context, TaskHandler handler) throws TaskRefusedException {
		try {
			if(!taskQLock.tryLock(10, TimeUnit.SECONDS)) {
				// if we did not get the lock, something is getting over-charged,
				// so refuse the task
				throw new TaskRefusedException("Scheduler lock overcharged...");
			}

      if (PERFORMANCELOGGER.isDebugEnabled()) {
        PERFORMANCELOGGER.debug("Scheduler status : " + getUsageStatus());
      }
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("accept() - entry : context " + context);
			}
			
			if (!active) {
				throw new TaskRefusedException("Shutting down...");
			}

			TaskClass taskClass = getTaskClassifierStrategy().getClassForTask(context);
			TaskEntry taskEntry = new TaskEntry(context, taskClass, handler);

			// check if the capacity for pending tasks has not been exhausted
			int currentPending = getPendingTaskCount() + 1;

			if(currentPending > usageStatus.getPendingQueueSize()) {
				throw new TaskRefusedException("Pending entities queue full. Refused " + context);
			}

			// update statistic for max pending entities
			if(currentPending > usageStatus.getMaxPendingCount()) {
				usageStatus.setMaxPendingCount(currentPending);
			}

			// check monitoring threshold for pending entries
/*			double pendingRatio = ((double)currentPending) / usageStatus.getPendingQueueSize();
			if(pendingRatio > bufferOccupationMonitoringThreshold) {
			  // FIXME: no alarming service yet
				AlarmingService alarmingSvc = Activator.getDefault().getAlarmingService();
				if(alarmingSvc!=null) {
					String bufferLoadMsg = "Buffer load " + Math.round(pendingRatio*100)+"%";
					alarmingSvc.raiseDareAlarm(ErrorCode.TASKSCHEDULER_BUFFER_LOAD, bufferLoadMsg, getName()+" scheduler");
				}
			}
*/
			// put the request in the buffers
			Queue<TaskEntry> reqQ = taskQueues.get(taskClass);
			if (reqQ == null) {
				// aha, 1st time we get this kind of request class
				// let's add a queue for it
				reqQ = new ConcurrentLinkedQueue<TaskEntry>();
				taskQueues.put(taskClass, reqQ);
			}
			reqQ.offer(taskEntry);
			taskQNotEmpty.signalAll();
		} catch (Throwable e) {	// NOSONAR
			throw new TaskRefusedException("Accept failed for " + context, e);
		} finally {
			try {taskQLock.unlock();} catch (Exception e) {/*ignore*/}
			LOGGER.debug("accept() - exit");
		}
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.TaskScheduler#shutdown()
	 */
	public void shutdown() {
		// TODO implement a nice and complete shutdown
		// i.e. need to be able to nicely continue processing
		// any pending requests, but stop accepting new ones.
		// the code below only finishes nicely the ones that
		// are already in the executor!
		LOGGER.debug("Shutting down scheduler " + schedulerName);
		active = false;
		queueDepletionExecutor.shutdown();
		taskHandlingExecutor.shutdown();
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.TaskScheduler#clearPending()
	 */
	public List<Context> clearPending() {
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("clearPending() - entry - " + schedulerName);
		}

		List<Context> results = new ArrayList<Context>();
		try {
			if(!taskQLock.tryLock(10, TimeUnit.SECONDS)) {
				// if we did not get the lock, bad luck
				// we'll try to free the task queues without it then
				LOGGER.warn("clearPending() - " + schedulerName + " - Unable to acquire lock, trying clear without it");
			}
			Collection<Queue<TaskEntry>> queues = taskQueues.values();
			for (Queue<TaskEntry> queue : queues) {
				for (TaskEntry entityEntry : queue) {
					results.add(entityEntry.getEntity());
				}
				queue.clear();
			}
			queues.clear();
		} catch (InterruptedException e) {
			// ignore
		} finally {
			try {taskQLock.unlock();} catch (Exception e) {/*ignore*/}
			LOGGER.debug("clearPending() - exit - " + schedulerName);
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.TaskScheduler#shutdownNow()
	 */
	public List<Context> shutdownNow() {
		LOGGER.debug("Shutting down NOW...");
		active = false;
		forcedShutdown = true;
		queueDepletionExecutor.shutdownNow();
		List<Runnable> busyRunners = taskHandlingExecutor.shutdownNow();
		List<Context> busyRequests = new ArrayList<Context>();
		for (Runnable runnable : busyRunners) {
			HandlerRunner handler = (HandlerRunner)runnable;
			busyRequests.add(handler.context);
			handler.token.release();
		}
		return busyRequests;
	}

	/* (non-Javadoc)
	 * @see com.isencia.passerelle.process.scheduler.TaskScheduler#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return taskHandlingExecutor.awaitTermination(timeout, unit);
	}

	/**
	 * Returns the count of resource tokens still available to be handed out.
	 * If this is a positive number, it implies that not all resources in the
	 * pool are occupied. Furthermore this implies that no requests should
	 * remain pending in the buffers.
	 *
	 * @return the count of resource tokens still available to be handed out
	 */
	public int getCurrentFreeCapacity() {
		return tokens.size();
	}

	/**
	 *
	 * @return the configured maximum capacity, i.e. the size of the resource pool
	 * that is available for processing requests
	 */
	public int getMaxCapacity() {
		return usageStatus.getNrOfResources();
	}

	public ResourceUsageStatus getUsageStatus() {
		return usageStatus;
	}

	/**
	 * internal utility method used by SchedulerAwareResourceToken
	 *
	 * @param token
	 * @param reqClass
	 */
	protected void releaseResourceToken(ResourceToken token, TaskClass reqClass) {
		LOGGER.debug("releaseResourceToken() - entry : token " + token + " for request class " + reqClass);

		synchronized(tokens) {
			if (!tokens.offer(token)) {
				// TODO better to define custom checked exception with more info in
				// there??
				throw new IllegalStateException();
			}
			busyTokens.get(reqClass).remove(token);
		}

		LOGGER.debug("releaseResourceToken() - exit - " + getTokenStatus());
	}
	
	private String getTokenStatus() {
		StringBuffer resultStr = new StringBuffer("free tokens : "+getCurrentFreeCapacity()+" - busy tokens : ");
		synchronized(tokens) {
			for (TaskClass reqClass : busyTokens.keySet()) {
				resultStr.append(" for " + reqClass.getName() + " : " + busyTokens.get(reqClass).size());
			}
		}
		return resultStr.toString();
	}

	private int getBusyTaskCount() {
		int cnt = 0;
		synchronized(tokens) {
			for (TaskClass reqClass : busyTokens.keySet()) {
				Collection<ResourceToken> busyT = busyTokens.get(reqClass);
				if(busyT != null) {
					cnt += busyT.size();
				}
			}
		}
		return cnt;
	}

	private int getPendingTaskCount() throws SchedulerException {
		int cnt = 0;
		try {
			if(!taskQLock.tryLock(10, TimeUnit.SECONDS)) {
				// if we did not get the lock, something is getting overcharged,
				throw new SchedulerException("Scheduler lock overcharged...");
			}
			for (TaskClass reqClass : taskQueues.keySet()) {
				Queue<TaskEntry> entryQ = taskQueues.get(reqClass);
				if(entryQ!=null) {
					cnt += entryQ.size();
				}
			}
		} catch (InterruptedException e) {
			throw new SchedulerException("Scheduler lock interrupted...",e);
		} finally {
			taskQLock.unlock();
		}
		return cnt;
	}

	/**
	 *
	 * A pair to maintain a request and its matching listener.
	 *
	 * @param <T> the request class
	 */
	protected static class TaskEntry {
		private final Context context;
		private final TaskClass entityClass;
		private final TaskHandler handler;

		public TaskEntry(Context context, TaskClass entityClass, TaskHandler handler) {
			this.context = context;
			this.entityClass = entityClass;
			this.handler = handler;
		}

		public Context getEntity() {
			return context;
		}

		public TaskClass getEntityClass() {
			return entityClass;
		}

		public TaskHandler getHandler() {
			return handler;
		}
	}

	/**
	 * Wait until a resource token is available, and then select the next
	 * entry to be handled.
	 */
	protected class TaskBufferSink implements Runnable {
		
		private static final String UNKNOWN_ERROR = "TaskBufferSink: Unexpected Thread Error: unknown error";

		public void run() {
			
			LOGGER.info("Starting TaskBufferSink for scheduler " + schedulerName);

			try {
				while (true) {
					TaskEntry selectedEntry = null;
			
					SchedulerAwareResourceToken token = (SchedulerAwareResourceToken) tokens.take();
					LOGGER.debug("Got a token " + getTokenStatus());
					
					try {
						if(!taskQLock.tryLock(10, TimeUnit.SECONDS)) {
							// if we did not get the lock, something is getting over-charged,
							// so log an error and retry loop
							LOGGER.error("Scheduler lock overcharged...");
							
							// in the release, the currentBusy is decremented
							// so need to quickly increment it here, to avoid miscounting
							token.release();
							continue;
						}

						// block till there's at least one context available
						while (active && getPendingTaskCount() == 0) {
							taskQNotEmpty.await(10, TimeUnit.SECONDS);
						}

						if (!active && (forcedShutdown || getPendingTaskCount() == 0)) {
							LOGGER.info("Shutting down TaskBufferSink...");
							token.release();
							break;
						}
						
						// we've got a resource token, and (while not shutting down)
						// know that at least one context is waiting in the buffer
						selectedEntry = getFairlySelectedEntry();
						
					} finally {
						try {taskQLock.unlock();} catch (Exception e) {/*ignore*/}
					}

					if (selectedEntry == null) {
						// hopefully this only happens in the "shutdown phase"
						if (active) {
							// this should never happen
							System.err.println("TaskBufferSink: Unexpected Thread Error: no LifeCycleEntityEntry available while there was a pending LifeCycleEntity Item");	// NOSONAR
							LOGGER.error("TaskBufferSink: Unexpected Thread Error: no LifeCycleEntityEntry available while there was a pending LifeCycleEntity Item");
							token.release();
							continue;
						} else {
							token.release();
							break;
						}
					}
					LOGGER.debug("Selected entry for context " + selectedEntry.context);

					// store the knowledge about which context class will receive this token
					token.lock(selectedEntry.getEntityClass());
					
					synchronized(tokens) {
						// Get or create the token collection
						Collection<ResourceToken> busyTokensForClass = busyTokens.get(selectedEntry.getEntityClass());
						if (busyTokensForClass == null) {
							busyTokensForClass = new HashSet<ResourceToken>();
							busyTokens.put(selectedEntry.getEntityClass(), busyTokensForClass);
						}
						// Add our token
						busyTokensForClass.add(token);
						int currentBusy = getBusyTaskCount();
						if(currentBusy > usageStatus.getMaxUsed()) {
							usageStatus.setMaxUsed(currentBusy);
						}

						// check monitoring threshold for busy entries
/*						double busyRatio = ((double)currentBusy)/usageStatus.getNrOfResources();
						if(busyRatio>loadMonitoringThreshold) {
						  // FIXME: no alarming service yet
							AlarmingService alarmingSvc = Activator.getDefault().getAlarmingService();
							if(alarmingSvc!=null) {
								String poolLoadMsg = "Active resources load " + Math.round(busyRatio*100)+"%";
								alarmingSvc.raiseDareAlarm(ErrorCode.TASKSCHEDULER_POOL_LOAD, poolLoadMsg, getName()+" scheduler");
							}
						}
*/
					}
					LOGGER.debug("Stored the token as busy " + getTokenStatus());
					
					// let the handler do its thing for its context, in an own thread
					try {
						taskHandlingExecutor.submit(new HandlerRunner(selectedEntry.context, token, selectedEntry.handler));
					} catch (Throwable t) {	// NOSONAR
						// in case an exception happens on submit, it would almost always be because of (temporary) memory 
						// issues (swap space). Therefore we wait 5 seconds and we try again
						// if that fails, then we are in real trouble so we should not retry anymore
						Thread.sleep(5000);
						try {
							taskHandlingExecutor.submit(new HandlerRunner(selectedEntry.context, token, selectedEntry.handler));
						} catch (Throwable tt) {	// NOSONAR
							System.err.println(UNKNOWN_ERROR);	// NOSONAR
							LOGGER.error(UNKNOWN_ERROR, tt);
							// we log the error and then we continue
						}
					}
				}
			} catch (InterruptedException e) {
				System.err.println("LifeCycleEntityBufferSink: Unexpected Thread Error: interrupted");	// NOSONAR
				LOGGER.error("LifeCycleEntityBufferSink: Unexpected Thread Error: interrupted", e);
			} catch (Throwable t) {	// NOSONAR
				System.err.println(UNKNOWN_ERROR);	// NOSONAR
				LOGGER.error(UNKNOWN_ERROR, t);
				// we can only re-throw it as a runtime exception, but that should not be an issue since the error should never occur.
				throw new RuntimeException(t);	// NOSONAR
			} finally {
				LOGGER.info("Ending TaskBufferSink for scheduler " + schedulerName);
			}
		}
		
		/**
		 * Implements the fair resource allocation algorithm:
		 * <ul>
		 * <li> if not congested : take whichever context that is first encountered while searching the buffers
		 * <li> if congested : use the fairness algorithm of Tanguy Herriau
		 * </ul>
		 * @return
		 */
		private TaskEntry getFairlySelectedEntry() {
			TaskEntry selectedEntry = null;

			if (getResourceCongestionDefinitionStrategy().resourcesAreCongested(getMaxCapacity(), getCurrentFreeCapacity())) {
				Collection<PrioritizedTaskClass> priorityScores = new TreeSet<PrioritizedTaskClass>();

				// 1. determine the priority for each class,
				// based on the nr of active requests for each class and their relative capacity assignment
				for (TaskClass entityClass : taskQueues.keySet()) {
					Collection<ResourceToken> busyTokensForClass = busyTokens.get(entityClass);
					int currentBusy = (busyTokensForClass != null) ? busyTokensForClass.size() : 0;
					double priorityScore = (currentBusy != 0) ? (entityClass.getRelativeCapacityAssignment() / currentBusy)
							: Double.MAX_VALUE;
					priorityScores.add(new PrioritizedTaskClass(entityClass, priorityScore));
				}
				
				// 2. now find the first request for the highest priority,
				// or if none present, check for the next priority etc.
				if (priorityScores.size() > 0) {
					for (PrioritizedTaskClass prioritizedTaskClass : priorityScores) {
						Queue<TaskEntry> selectedQueue = taskQueues.get(prioritizedTaskClass.getEntityClass());
						if (selectedQueue != null) {
							selectedEntry = selectedQueue.poll();
							if (selectedEntry != null) {
								break;
							}
						}
					}
				}
			}
			
			if (selectedEntry == null) {
				// Means no request found yet so just pick an arbitrary one.
				// Should be because not congested.
				// Otherwise there's an internal state mgmt issue in here,
				// as this method is only invoked when we are certain that a request is pending,
				// and in congestion mode the above code-block should have found it then...
				for (Queue<TaskEntry> reqQueue : taskQueues.values()) {
					if (reqQueue != null) {
						selectedEntry = reqQueue.poll();
						if (selectedEntry != null) {
							break;
						}
					}
				}
			}
			
			return selectedEntry;
		}
	}
	
	protected static class HandlerRunner implements Runnable {
		private final Context context;
		private final ResourceToken token;
		private final TaskHandler handler;

		/**
		 * @param context
		 * @param token
		 * @param handler
		 */
		public HandlerRunner(Context context, ResourceToken token, TaskHandler handler) {
			this.context = context;
			this.token = token;
			this.handler = handler;
		}

		public void run() {
			handler.handle(context, token);
		}
	}

}
