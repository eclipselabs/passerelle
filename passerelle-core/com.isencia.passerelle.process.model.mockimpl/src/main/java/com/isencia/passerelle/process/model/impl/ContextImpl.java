/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Matcher;
import com.isencia.passerelle.process.model.NamedValue;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.model.Task;

/**
 * @author "puidir"
 */
public class ContextImpl implements Context {

  private static final long serialVersionUID = 1L;

  private Long id;

  private String repositoryId;

  private Status status;

  private Request request;

  private List<Task> tasks = new ArrayList<Task>();

  private List<ContextEvent> events = new ArrayList<ContextEvent>();

  private Map<String, Serializable> entries = new ConcurrentHashMap<String, Serializable>();

  private Date creationTS;

  private Date endTS;

  // join/fork support
  private Stack<Integer> taskCursorStack = new Stack<Integer>();
  private Stack<Integer> eventCursorStack = new Stack<Integer>();
  private boolean transientBranch = false;

  private ReentrantLock lock = new ReentrantLock();

  private List<Context> forkedContexts = new CopyOnWriteArrayList<Context>();

  public ContextImpl() {
  }

  public ContextImpl(Request request) {
    this.status = Status.CREATED;
    this.creationTS = new Date();
    this.request = request;
    new ContextStatusEventImpl(this);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContextRepositoryID() {
    return repositoryId;
  }

  public void setContextRepositoryID(String repositoryId) {
    this.repositoryId = repositoryId;
  }

  public Status getStatus() {
    return status;
  }

  public boolean setStatus(Status status) {
    if (this.status != null && this.status.isFinalStatus()) {
      return false;
    } else if (!this.status.equals(status)) {
      this.status = status;
      new ContextStatusEventImpl(this);
      // Mark the end of processing
      if (status.isFinalStatus()) {
        endTS = new Date();
      }
      // TODO: should notify status listeners
      return true;
    } else {
      return false;
    }
  }

  public Request getRequest() {
    return request;
  }

  /**
   * Replace an existing task with one that is more up-to-date.
   * 
   * @param task
   *          The more up-to-date version of the task
   */
  public void reattachTask(Task task) {
    // Check if we simply need to reattach the task
    if (task.getId() != null) {
      synchronized (tasks) {
        // Remark: not using indexOf to allow Task implementations to
        // have their own equals()
        for (int taskIndex = 0; taskIndex < tasks.size(); taskIndex++) {
          if (task.getId().equals(tasks.get(taskIndex).getId())) {
            tasks.set(taskIndex, task);
            return;
          }
        }
      }
    }
  }

  public synchronized void addTask(Task task) {
    this.tasks.add(task);
  }

  public List<Task> getTasks() {
    return Collections.unmodifiableList(tasks);
  }

  synchronized void addEvent(ContextEvent event) {
    events.add(event);
  }

  public List<ContextEvent> getEvents() {
    return Collections.unmodifiableList(events);
  }
  
  @Override
  public List<ContextEvent> getMatchingEvents(Matcher<ContextEvent> matcher) {
    if (matcher == null) {
      return events;
    }
    List<ContextEvent> results = new ArrayList<>();
    for (ContextEvent contextEvent : events) {
      if(matcher.matches(contextEvent)) {
        results.add(contextEvent);
      }
    }
    return results;
  }


  public void putEntry(String name, Serializable value) {
    entries.put(name, value);
  }

  public Serializable getEntryValue(String name) {
    return entries.get(name);
  }

  public Iterator<String> getEntryNames() {
    return entries.keySet().iterator();
  }

  public String lookupValue(String dataType, String name) {

    String result = null;

    // first check in the context entries, these have highest priority
    Object contextEntry = getEntryValue(name);
    if (contextEntry != null) {
      // need to force this into a string somehow
      result = contextEntry.toString();
    } else {
      // check in task results, most recent first
      // we get a copy of the tasks list, so need to synchronize etc
      List<Task> tasks = getTasks();
      for (int taskIdx = tasks.size() - 1; taskIdx >= 0 && result == null; taskIdx--) {
        Task task = tasks.get(taskIdx);
        if (task != null) {
          Collection<ResultBlock> blocks = task.getResultBlocks();
          for (ResultBlock block : blocks) {
            if (dataType == null || block.getType().equalsIgnoreCase(dataType)) {
              ResultItem<?> item = block.getItemForName(name);
              if (item != null) {
                result = item.getValueAsString();
                break;
              }
            }
          }
        }
      }

      // if still nothing found, check in the original request
      if (result == null && getRequest() != null) {
        NamedValue<?> reqAttribute = getRequest().getAttribute(name);
        result = reqAttribute != null ? reqAttribute.getValueAsString() : null;
      }

      // if still nothin found, check in the historical data
    }

    return result;
  }

  public String lookupValue(String name) {
    return lookupValue(null, name);
  }

  public boolean isFinished() {
    return status.isFinalStatus();
  }

  public Date getCreationTS() {
    return creationTS;
  }

  public Date getEndTS() {
    return endTS;
  }

  public Long getDurationInMillis() {
    if (creationTS != null && endTS != null) {
      return endTS.getTime() - creationTS.getTime();
    }

    // Not finished or not started yet
    return null;
  }

  /**
   * @return the current top cursor index on the stack, and remove it from the stack.
   */
  protected int popTaskCursorIndex() {
    return taskCursorStack.pop();
  }

  /**
   * Adds the current task list size to the cursor stack. I.e. this cursor identifies the position of the next result
   * entry that will be added.
   */
  protected void pushCurrentTaskCursorIndex() {
    taskCursorStack.push(tasks.size());
  }

  /**
   * @return the current top event cursor index on the stack, without removing it from the stack.
   */
  protected int peekEventCursorIndex() {
    return eventCursorStack.peek();
  }

  /**
   * @return the current top event cursor index on the stack, and remove it from the stack.
   */
  protected int popEventCursorIndex() {
    return eventCursorStack.pop();
  }

  /**
   * Adds the current event list size to the cursor stack. I.e. this cursor identifies the position of the next event
   * entry that will be added.
   */
  protected void pushCurrentEventCursorIndex() {
    eventCursorStack.push(events.size());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.Context#join(com.isencia.passerelle .process.model.Context)
   */
  public void join(Context other) {
    ContextImpl contextToMerge = (ContextImpl) other;
    try {
      lock.lock();

      // add new tasks obtained from the related branch
      int taskCursorIndex = contextToMerge.popTaskCursorIndex();
      List<Task> tasks = contextToMerge.getTasks();
      if (tasks.size() > taskCursorIndex) {
        for (int r = taskCursorIndex; r < tasks.size(); ++r) {
          addTask(tasks.get(r));
        }
      }
      // add new events obtained from the related branch
      int eventCursorIndex = contextToMerge.popEventCursorIndex();
      List<ContextEvent> events = contextToMerge.getEvents();
      if (events.size() > eventCursorIndex) {
        for (int r = eventCursorIndex; r < events.size(); ++r) {
          getEvents().add(events.get(r));
        }
      }

      // merge context entries
      entries.putAll(contextToMerge.entries);

    } finally {
      lock.unlock();
      forkedContexts.remove(other);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.Context#fork()
   */
  public Context fork() {
    ContextImpl copy = new ContextImpl();
    try {
      lock.lock();
      copy.id = id;
      copy.status = status;
      copy.request = request;
      copy.tasks.addAll(tasks);
      copy.events.addAll(events);
      copy.entries.putAll(entries);
      copy.transientBranch = true;
      // Mark the current results size, so we're able to identify
      // what's been added on the copy afterwards.
      copy.pushCurrentTaskCursorIndex();
      copy.pushCurrentEventCursorIndex();
      forkedContexts.add(copy);
    } finally {
      lock.unlock();
    }
    return copy;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.isencia.passerelle.process.model.Context#isForkedContext()
   */
  public boolean isForkedContext() {
    return transientBranch;
  }

  public List<Context> getForkedChildContexts() {
    return Collections.unmodifiableList(forkedContexts);
  }

  public List<ErrorItem> getErrors() {
    // TODO Auto-generated method stub
    return null;
  }

  public Serializable removeEntry(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public Context minimize() {
    // TODO Auto-generated method stub
    return null;
  }

  public Context restore() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isMinimized() {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ContextImpl [id=" + id + ", status=" + status + ", creationTS=" + creationTS + ", endTS=" + endTS + ",\n request=" + request + ",\n tasks=" + tasks + ",\n events="
        + events + "]";
  }

  @Override
  public String getProcessId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setProcessId(String processId) {
    // TODO Auto-generated method stub

  }

  @Override
  public void join(Context... contexts) {
    // TODO Auto-generated method stub

  }

  @Override
  public Serializable getDeepEntryValue(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Serializable> getDeepEntryValues() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Serializable removeDeepEntry(String name) {
    // TODO Auto-generated method stub
    return null;
  }

}
