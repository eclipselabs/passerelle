/**
 * 
 */
package com.isencia.passerelle.process.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextErrorEvent;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.ErrorItem;
import com.isencia.passerelle.process.model.Matcher;
import com.isencia.passerelle.process.model.NamedValue;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;
import com.isencia.passerelle.process.model.Status;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.HistoricalDataProvider;
import com.isencia.passerelle.process.model.factory.HistoricalDataProviderTracker;
import com.isencia.passerelle.process.model.impl.util.ProcessUtils;

/**
 * @author "puidir"
 * 
 */
@Cacheable(false)
@Entity
@Table(name = "PAS_CONTEXT")
public class ContextImpl implements Context {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ID", nullable = false, unique = true, updatable = false)
  @GeneratedValue(generator = "pas_context")
  private Long id;

  @Version
  private Integer version;

  @Column(name = "STATUS", nullable = false, unique = false, updatable = true, length = 50)
  @Enumerated(value = EnumType.STRING)
  private Status status;

  @OneToOne(targetEntity = RequestImpl.class, fetch = FetchType.LAZY)
  @JoinColumn(name = "REQUEST_ID", unique = true, nullable = false)
  private RequestImpl request;

  @OneToMany(targetEntity = TaskImpl.class, mappedBy = "parentContext", fetch = FetchType.LAZY)
  @OrderBy("id")
  private List<Task> tasks = ProcessUtils.emptyList();

  @OneToMany(targetEntity = ContextEventImpl.class, mappedBy = "context", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @OrderBy("creationTS")
  private List<ContextEvent> events = ProcessUtils.emptyList();

  @SuppressWarnings("unchecked")
  @Transient
  private Map<String, Serializable> entries = new CaseInsensitiveMap();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATION_TS", nullable = false, unique = false, updatable = false)
  private Date creationTS;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "END_TS", nullable = true, unique = false, updatable = true)
  private Date endTS;

  // join/fork support
  @Transient
  private Stack<Integer> taskCursorStack = new Stack<Integer>();

  @Transient
  private Stack<Integer> eventCursorStack = new Stack<Integer>();

  @Transient
  private boolean transientBranch = false;

  @Transient
  private ReentrantLock lock = new ReentrantLock();

  @Transient
  private boolean minimized = false;

  @Transient
  private List<Long> minimizedTasks = new ArrayList<Long>();

  @Transient
  private String processId;

  // avoid concurrent modif ex when getForkedContexts is called while forking or joining
  @Transient
  private List<Context> forkedContexts = new CopyOnWriteArrayList<Context>();

  public static final String _ID = "id";
  public static final String _STATUS = "status";
  public static final String _REQUEST = "request";
  public static final String _REQUEST_ID = "request.id";
  public static final String _TASKS = "tasks";
  public static final String _EVENTS = "events";
  public static final String _CREATION_TS = "creationTS";
  public static final String _END_TS = "endTS";
  public static final String _DURATION = "durationInMillis";

  public ContextImpl() {
  }

  public ContextImpl(RequestImpl request) {
    this.status = Status.CREATED;
    this.creationTS = new Date();
    this.request = request;

    new ContextEventImpl(this, this.status.name());
  }

  public Long getId() {
    return id;
  }

  // TODO triple-check if this is really needed and then remove it from this "real" entity impl and use another impl if
  // Id must be mutable
  public void setId(Long id) {
    this.id = id;
  }

  public String getProcessId() {
    if (processId == null && request instanceof Task) {
      return (((Task) request).getParentContext().getProcessId());
    }
    return processId;
  }

  public void setProcessId(String repositoryId) {
    this.processId = repositoryId;
  }

  public Status getStatus() {
    return status;
  }

  public boolean setStatus(Status status) {
    if (this.status != null && this.status.isFinalStatus() && (!Status.RESTARTED.equals(status) && !Status.CANCELLED.equals(status))) {
      return false;
    } else {
      this.status = status;

      // Mark the end of processing
      if (status.isFinalStatus()) {
        endTS = new Date();
      }

      // TODO: should notify status listeners

      return true;
    }
  }

  public RequestImpl getRequest() {
    return request;
  }

  public void addTask(Task task) {
    if (!ProcessUtils.isInitialized(tasks))
      tasks = new ArrayList<Task>();
    this.tasks.add(task);
    // TODO check if other associations must be adapted
    ((TaskImpl) task).setParentContext(this);
  }

  public List<Task> getTasks() {
    if (!ProcessUtils.isInitialized(tasks)) {
      return tasks;
    }
    return Collections.unmodifiableList(tasks);
  }

  void addEvent(ContextEvent event) {
    if (!ProcessUtils.isInitialized(events))
      events = new ArrayList<ContextEvent>();
    events.add(event);
    if (event instanceof ContextErrorEvent) {
      _getErrors().add(((ContextErrorEvent) event).getErrorItem());
    }
  }

  public List<ContextEvent> getEvents() {
    if (!ProcessUtils.isInitialized(events)) {
      return events;
    }
    return Collections.unmodifiableList(events);
  }

  @Override
  public List<ContextEvent> getMatchingEvents(Matcher<ContextEvent> matcher) {
    if (matcher == null || !ProcessUtils.isInitialized(events)) {
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

  public Serializable removeDeepEntry(String name) {
    removeEntry(name);
    List<Task> tasks = getTasks();
    for (Task task : tasks) {
      task.getProcessingContext().removeEntry(name);
    }
    return entries.remove(name);
  }

  public Serializable removeEntry(String name) {
    return entries.remove(name);
  }

  public Map<String, Serializable> getDeepEntryValues() {
    Map<String, Serializable> map = new HashMap<String, Serializable>();
    // check in task results, most recent first
    // we get a copy of the tasks list, so need to synchronize etc
    List<Task> tasks = getTasks();
    for (int taskIdx = 0; taskIdx < tasks.size(); taskIdx++) {
      Task task = tasks.get(taskIdx);
      if (task.getProcessingContext().getStatus().isFinalStatus() && !Status.CANCELLED.equals(task.getProcessingContext().getStatus())) {
        // this could override previous entries !
        Iterator<String> entryNames = task.getProcessingContext().getEntryNames();
        while (entryNames.hasNext()) {
          String entryName = entryNames.next();
          Serializable entryValue = task.getProcessingContext().getEntryValue(entryName);
          if (entryValue != null) {
            map.put(entryName, entryValue);
          }
        }
      }
    }
    // this could override previous entries !
    Iterator<String> entryNames = getEntryNames();
    while (entryNames.hasNext()) {
      String entryName = entryNames.next();
      Serializable entryValue = getEntryValue(entryName);
      if (entryValue != null) {
        map.put(entryName, entryValue);
      }
    }
    return map;

  }

  public Serializable getDeepEntryValue(String name) {
    Serializable entryValue = getEntryValue(name);
    if (entryValue != null) {
      return entryValue;
    }
    // check in task results, most recent first
    // we get a copy of the tasks list, so need to synchronize etc
    List<Task> tasks = getTasks();
    for (int taskIdx = tasks.size() - 1; taskIdx >= 0; taskIdx--) {
      Task task = tasks.get(taskIdx);
      if (task.getProcessingContext().getStatus().isFinalStatus() && !Status.CANCELLED.equals(task.getProcessingContext().getStatus())) {
        entryValue = task.getProcessingContext().getEntryValue(name);
        if (entryValue != null) {
          return entryValue;
        }
      }
    }
    return null;
  }

  public Serializable getEntryValue(String name) {
    return entries.get(name);
  }

  public Iterator<String> getEntryNames() {
    return entries.keySet().iterator();
  }

  public String lookupValue(String dataType, String name) {

    if (name == null) {
      return null;
    }

    String result = null;

    // first check in the context entries, these have highest priority
    Object contextEntry = getDeepEntryValue(name);
    if (contextEntry != null) {
      // need to force this into a string somehow
      result = contextEntry.toString();
    } else {
      // check in task results, most recent first
      // we get a copy of the tasks list, so need to synchronize etc
      List<Task> tasks = getTasks();
      for (int taskIdx = tasks.size() - 1; taskIdx >= 0 && result == null; taskIdx--) {
        Task task = tasks.get(taskIdx);
        if (task.getProcessingContext().getStatus().isFinalStatus() && !Status.CANCELLED.equals(task.getProcessingContext().getStatus())) {
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
      if (result == null) {
        HistoricalDataProvider historicalDataProvider = HistoricalDataProviderTracker.getService();
        if (historicalDataProvider != null) {
          List<Attribute> historicalRequestAttributes = historicalDataProvider.getRequestAttributes(this);
          if (historicalRequestAttributes != null) {
            for (Attribute attribute : historicalRequestAttributes) {
              if (attribute.getName().equalsIgnoreCase(name)) {
                result = attribute.getValueAsString();
                break;
              }
            }
          }

          if (result == null) {
            List<ResultBlock> historicalBlocks = historicalDataProvider.getResultBlocks(this);
            if (historicalBlocks != null) {
              for (ResultBlock block : historicalBlocks) {
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
        }
      }
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
    Date creationTS = this.creationTS;
    if (creationTS == null && request != null) {
      creationTS = request.getCreationTS();
    }
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

  public void join(Context... contexts) {
    for (Context context : contexts) {
      ContextImpl contextToMerge = (ContextImpl) context;
      try {
        lock.lock();

        // add new tasks obtained from the related branch
        int taskCursorIndex = contextToMerge.popTaskCursorIndex();
        List<Task> tasks = contextToMerge.getTasks();
        if (tasks.size() > taskCursorIndex) {
          for (int r = taskCursorIndex; r < tasks.size(); ++r) {
            final Task task = tasks.get(r);
            addTask(task);
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

        // Status.RESTARTED should be overwritten in case other status is found
        if (Status.RESTARTED.equals(this.getStatus()) && !Status.RESTARTED.equals(context.getStatus())) {
          this.setStatus(context.getStatus());
        }

      } finally {
        lock.unlock();
        forkedContexts.clear();
      }
    }
  }

  public Context fork() {
    ContextImpl copy = new ContextImpl();
    try {
      lock.lock();
      copy.id = id;
      copy.processId = processId;
      copy.status = status;
      copy.request = request;
      // use addTask() to add tasks to copy, because it has to initialize the collection
      for (Task task : tasks)
        copy.addTask(task);
      // use addEvent() to add events to copy, because it has to initialize the collection
      for (ContextEvent event : events)
        copy.addEvent(event);
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

  public boolean isForkedContext() {
    return transientBranch;
  }

  public List<Context> getForkedChildContexts() {
    return Collections.unmodifiableList(forkedContexts);
  }

  public List<ErrorItem> getErrors() {
    List<ErrorItem> allErrors = new ArrayList<ErrorItem>();
    allErrors.addAll(_getErrors());
    for (Task task : tasks) {
      allErrors.addAll(task.getProcessingContext().getErrors());
    }
    return allErrors;
  }

  private List<ErrorItem> _getErrors() {
    List<ErrorItem> errorItems = new ArrayList<ErrorItem>();
    for (ContextEvent event : events) {
      if (event instanceof ContextErrorEvent) {
        ErrorItem errorItem = ((ContextErrorEvent) event).getErrorItem();
        if (errorItem != null) {
          errorItems.add(errorItem);
        }
      }
    }
    return errorItems;
  }

  public synchronized Context minimize() {
    // allow consecutive minimize calls that each time drop whatever
    // might have been added since a previous call,
    // and maintains task ids only for those new ones...
    // if(!isMinimized()) {
    minimized = true;
    minimizedTasks.clear();
    for (Task t : tasks) {
      minimizedTasks.add(t.getId());
    }
    tasks = ProcessUtils.emptyList();
    // }
    return this;
  }

  /**
   * Not implemented yet
   */
  public synchronized Context restore() {
    throw new UnsupportedOperationException();
    // if(isMinimized()) {
    // minimized = false;
    // }
    // return this;
  }

  public synchronized boolean isMinimized() {
    return minimized;
  }
}
