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
package com.isencia.passerelle.process.actor.flow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.core.PasserelleException;
import com.isencia.passerelle.message.ManagedMessage;
import com.isencia.passerelle.message.internal.MessageContainer;
import com.isencia.passerelle.message.internal.sequence.SequenceTrace;
import com.isencia.passerelle.process.actor.Actor;
import com.isencia.passerelle.process.actor.ProcessRequest;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.service.ProcessManager;

/**
 * @author erwin
 */
public abstract class AbstractMessageSequenceGenerator extends Actor implements MessageSequenceGenerator {
  private static final long serialVersionUID = 1L;
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMessageSequenceGenerator.class);

  private Map<Long, SequenceTrace> msgSequences = new HashMap<Long, SequenceTrace>();
  private Map<Long, MsgTimeEntry> sequenceScopeMessages = new HashMap<Long, MsgTimeEntry>();
  private LinkedList<MsgTimeEntry> sequenceTimeEntries = new LinkedList<MsgTimeEntry>();

  private Lock seqTELock;

  private AggregationStrategy aggregationStrategy;
  private EvictedMessagesHandler evictedMessagesHandler;
  private boolean stateful;
  // < 1 for unlimited, or the max number of message sequences that will be retained. When this limit is passed while
  // adding a new entry, the oldest entry is evicted.
  private long maxRetentionCount;
  // < 1 for unlimited, or a time in ms. Entries that pass this age/time limit will be evicted.
  private long maxRetentionTime;

  public Parameter stateFulParameter;
  public Parameter maxRetentionCountParameter;
  public Parameter maxRetentionTimeParameter;

  /**
   * Construct an instance without eviction info. I.e. any state (i.c.o. a stateful one) will stay forever, or at least
   * until the aggregation has been done. By default, for stateful instances, a <code>ContextAggregationStrategy</code>
   * is set.
   * 
   * @param container
   * @param name
   * @param stateful
   * @throws IllegalActionException
   * @throws NameDuplicationException
   */
  public AbstractMessageSequenceGenerator(CompositeEntity container, String name, boolean stateful) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    this.stateful = stateful;
    if (stateful) {
      aggregationStrategy = new ContextAggregationStrategy();
      maxRetentionCountParameter = new Parameter(this, "maxRetentionCount", new IntToken(-1));
      maxRetentionCountParameter.setDisplayName("Max retention count");
      maxRetentionTimeParameter = new Parameter(this, "maxRetentionTime", new IntToken(-1));
      maxRetentionTimeParameter.setDisplayName("Max retention time (s)");
    }
  }

  @Override
  protected void doInitialize() throws InitializationException {
    super.doInitialize();
    if (isStateful()) {
      seqTELock = new ReentrantLock();
      if (seqTELock.tryLock()) {
        try {
          msgSequences = new HashMap<Long, SequenceTrace>();
          sequenceScopeMessages = new HashMap<Long, MsgTimeEntry>();
          sequenceTimeEntries = new LinkedList<MsgTimeEntry>();
          aggregationStrategy = new ContextAggregationStrategy();
          if (evictedMessagesHandler == null) {
            // has not been explicitly set, so take the default one
            evictedMessagesHandler = new ErrorThrowingEvictedMessageHandler(this);
          }

          maxRetentionCount = ((IntToken) maxRetentionCountParameter.getToken()).longValue();
          maxRetentionTime = ((IntToken) maxRetentionTimeParameter.getToken()).longValue();
          // TODO activate eviction-by-time mechanism(s)
        } catch (IllegalActionException e) {
          throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Error reading retention parameters", this, e);
        } finally {
          try {
            seqTELock.unlock();
          } catch (Exception e) {/* ignore */
          }
        }
      } else {
        throw new InitializationException(ErrorCode.ACTOR_INITIALISATION_ERROR, "Could not acquire seq trace Q lock", this, null);
      }
    }
  }

  public boolean isStateful() {
    return stateful;
  }

  public boolean wasGeneratedHere(ManagedMessage seqMsg) {
    if (isStateful()) {
      try {
        if (!seqTELock.tryLock(10, TimeUnit.SECONDS)) {
          // if we did not get the lock, bad luck
          // we'll try to do our thing without it then
          getLogger().warn("{} - wasGeneratedHere() - Unable to acquire lock, trying without it", getFullName());
        }
        return sequenceScopeMessages.get(seqMsg.getSequenceID()) != null;
      } catch (Exception e) {
        return false;
      } finally {
        try {
          seqTELock.unlock();
        } catch (Exception e) {/* ignore */
        }
      }
    } else {
      return false;
    }
  }

  protected void registerSequenceScopeMessage(Long seqID, ManagedMessage message) throws ProcessingException {
    if (isStateful()) {
      try {
        if (!seqTELock.tryLock(10, TimeUnit.SECONDS)) {
          // if we did not get the lock, bad luck
          // we'll try to do our thing without it then
          getLogger().warn("{} - registerSequenceScopeMessage() - Unable to acquire lock, trying without it", getFullName());
        }
        if ((maxRetentionCount > 0) && (sequenceTimeEntries.size() >= maxRetentionCount)) {
          // evict the oldest entry
          MsgTimeEntry msgTimeEntry = sequenceTimeEntries.removeLast();
          evict(msgTimeEntry.seqID);
        }
        MsgTimeEntry entry = new MsgTimeEntry(seqID, message);
        sequenceScopeMessages.put(seqID, entry);
        sequenceTimeEntries.add(entry);
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error registering sequence scope message", this, message, e);
      } finally {
        try {
          seqTELock.unlock();
        } catch (Exception e) {/* ignore */
        }
      }
    }
  }

  public ManagedMessage aggregateProcessedMessage(ProcessManager processManager, ManagedMessage seqMsg) throws ProcessingException {
    if (isStateful()) {
      try {
        if (!seqTELock.tryLock(10, TimeUnit.SECONDS)) {
          // if we did not get the lock, bad luck
          // we'll try to do our thing without it then
          getLogger().warn("{} - aggregateProcessedMessage() - Unable to acquire lock, trying without it", getFullName());
        }
        Long scopeId = seqMsg.getSequenceID();
        Context branchedCtxt = null;
        if (sequenceScopeMessages.get(scopeId) != null) {
          branchedCtxt = getBranchedContextFor(processManager, seqMsg);
        }
        ManagedMessage mergedMsg = null;
        if (branchedCtxt != null) {
          SequenceTrace seqTrace = msgSequences.get(scopeId);
          if (seqTrace == null) {
            seqTrace = new SequenceTrace(scopeId);
            msgSequences.put(scopeId, seqTrace);
          }
          seqTrace.addMessage(seqMsg);
          if (seqTrace.isComplete()) {
            try {
              getAuditLogger().debug("{} All sequence messages received for scope {}", getFullName(), scopeId);
              ManagedMessage[] messages = seqTrace.getMessagesInSequence();
              MessageContainer scopeMsg = (MessageContainer) sequenceScopeMessages.get(seqTrace.getSequenceID()).message;
              mergedMsg = aggregationStrategy != null ? aggregationStrategy.aggregateMessages(processManager,scopeMsg, messages) : scopeMsg;
            } catch (Exception e) {
              throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error aggregating messages for scope " + scopeId, this, e);
            } finally {
              msgSequences.remove(seqTrace);
              seqTrace.clear();
              MsgTimeEntry removedEntry = sequenceScopeMessages.remove(scopeId);
              sequenceTimeEntries.remove(removedEntry);
            }
          }
        }
        return mergedMsg;
      } catch (ProcessingException e) {
        throw e;
      } catch (Exception e) {
        throw new ProcessingException(ErrorCode.ACTOR_EXECUTION_ERROR, "Error handling sequence message", this, seqMsg, e);
      } finally {
        try {
          seqTELock.unlock();
        } catch (Exception e) {/* ignore */
        }
      }
    } else {
      return seqMsg;
    }
  }
  
  protected Context getBranchedContextFor(ProcessManager processManager, ManagedMessage msg) {
    String[] scopeGrp = msg.getHeader(ProcessRequest.HEADER_CTXT_SCOPE_GRP);
    String[] scope = msg.getHeader(ProcessRequest.HEADER_CTXT_SCOPE);
    Context branchedCtx = null;
    if(scopeGrp!=null && scope!=null && scopeGrp.length==1 && scope.length==1) {
      branchedCtx = processManager.getScopedProcessContext(scopeGrp[0], scope[0]);
    }
    return (branchedCtx!=null) ? branchedCtx : processManager.getRequest().getProcessingContext();
  }

  public void evict(Long seqID) {
    MsgTimeEntry evictedScopeEntry = sequenceScopeMessages.remove(seqID);
    if (evictedScopeEntry != null) {
      getLogger().warn("{} - evicting sequence {}", getFullName(), seqID);
      SequenceTrace evictedSeqTrace = msgSequences.remove(seqID);
      ManagedMessage evictedScopeMsg = evictedScopeEntry.message;
      if (getEvictedMessagesHandler() != null) {
        try {
          if (evictedSeqTrace != null) {
            getEvictedMessagesHandler().handleEvictedMessages(evictedScopeMsg, evictedSeqTrace.getMessagesInSequence());
          } else {
            getEvictedMessagesHandler().handleEvictedMessages(evictedScopeMsg);
          }
        } catch (PasserelleException e) {
          try {
            sendErrorMessage(e);
          } catch (IllegalActionException e1) {
            getLogger().error("Error sending error msg", e1);
            getLogger().error("Message eviction error", e);
          }
        }
      }
    } else {
      getLogger().warn("{} - failed eviction request for unknown sequence {}", getFullName(), seqID);
    }
  }

  public AggregationStrategy getAggregationStrategy() {
    return aggregationStrategy;
  }

  public void setAggregationStrategy(AggregationStrategy aggregationStrategy) {
    this.aggregationStrategy = aggregationStrategy;
  }

  public EvictedMessagesHandler getEvictedMessagesHandler() {
    return evictedMessagesHandler;
  }

  public void setEvictedMessagesHandler(EvictedMessagesHandler evictedMessagesHandler) {
    this.evictedMessagesHandler = evictedMessagesHandler;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  static class MsgTimeEntry {
    Long seqID;
    long creationTime;
    ManagedMessage message;

    public MsgTimeEntry(Long seqID, ManagedMessage message) {
      this.seqID = seqID;
      this.message = message;
      this.creationTime = System.currentTimeMillis();
    }
  }
}
