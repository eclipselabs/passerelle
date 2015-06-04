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
package com.isencia.passerelle.testsupport;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.core.Port;
import com.isencia.passerelle.model.Flow;

/**
 * This is a simple "builder" class to specify and assert flow execution expectations :
 * <ul>
 * <li>Number of messages that have been received by input ports</li>
 * <li>Number of messages that have been sent by output ports</li>
 * <li>Number of fire/process iterations done by actors</li>
 * </ul>
 * <p>
 * The ports and actors are specified by their full name, optionally including the root model name.<br/>
 * E.g. the input port of the <code>Console</code> actor can be named <code>"Console.input"</code> or, when used within a known flow called
 * <code>"HelloWorld"</code>, it can be named as <code>".HelloWorld.Console.input"</code>, which is the result of the method <code>Port.getFullName()</code>.
 * </p>
 * If a port or an actor is inside a submodel in a hierarchic model/flow, its hierarchic path must always be specified. E.g. for a <code>Console</code> actor
 * inside a submodel <code>"SayHello"</code> of the model <code>"HelloWorld"</code>, the actor can be referred to as <code>"SayHello.Console"</code> or
 * <code>".HelloWorld.SayHello.Console"</code>, but not simply as <code>"Console"</code>.
 * <p>
 * Same goes for the actors.
 * </p>
 * The initial "." is typical for a full name of a Passerelle/Ptolemy model element, as obtained from that method call.
 * 
 * @author erwin
 */
public class FlowStatisticsAssertion {
  private static final Logger LOGGER = LoggerFactory.getLogger(FlowStatisticsAssertion.class);

  /**
   * Maintains the expected counts of received messages for input ports. Ports are specified by their name. Please check the class doc for info about naming
   * options.
   */
  private Map<String, Long> portMsgReceiptCounts = new HashMap<String, Long>();

  /**
   * Maintains the expected counts of sent messages for output ports. Ports are specified by their name. Please check the class doc for info about naming
   * options.
   */
  private Map<String, Long> portMsgSentCounts = new HashMap<String, Long>();

  /**
   * Maintains the expected iteration counts for actors. Actors are specified by their name. Please check the class doc for info about naming options.
   */
  private Map<String, Long> actorIterationCounts = new HashMap<String, Long>();

  /**
   * The default constructor creates an instance without initial test outcome expectations. The expectations should all be defined by repeatedly invoking the
   * methods <code>expectMsg...</code> and <code>expectActor...</code>
   */
  public FlowStatisticsAssertion() {
  }

  /**
   * This constructor creates an instance with some initial test outcome expectations, specified in <code>Maps</code>.
   * <p>
   * Further expectations can still be defined by repeatedly invoking the methods <code>expectMsg...</code> and <code>expectActor...</code>
   * </p>
   * 
   * @param portMsgReceiptCounts
   * @param portMsgSentCounts
   * @param actorIterationCounts
   */
  public FlowStatisticsAssertion(Map<String, Long> portMsgReceiptCounts, Map<String, Long> portMsgSentCounts, Map<String, Long> actorIterationCounts) {
    this.portMsgReceiptCounts = portMsgReceiptCounts;
    this.portMsgSentCounts = portMsgSentCounts;
    this.actorIterationCounts = actorIterationCounts;
  }

  /**
   * Clear all test result expectations.
   * <p>
   * New expectations can be defined again by repeatedly invoking the methods <code>expectMsg...</code> and <code>expectActor...</code>
   * </p>
   * 
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion clear() {
    portMsgReceiptCounts.clear();
    portMsgSentCounts.clear();
    actorIterationCounts.clear();
    return this;
  }

  /**
   * Assert all configured expectations on the given flow. The assertions are done using JUnit's <code>Assert.assert...()</code> methods, so any discovered
   * deviation will result in a JUnit test failure.
   * <p>
   * If all expectations are ok, further tests can be chained through the returned reference to this <code>FlowStatisticsAssertion</code> instance.
   * </p>
   * 
   * @param flow
   *          the flow that has been executed and for which test result expectations must be asserted.
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion assertFlow(Flow flow) {
    assertPortReceiptStatistics(flow, portMsgReceiptCounts);
    assertPortSentStatistics(flow, portMsgSentCounts);
    assertActorIterationStatistics(flow, actorIterationCounts);
    return this;
  }

  /**
   * Add an expected count for messages received on the default input port of the given actor.
   * <p>
   * When the actor doesn't have an input port named "input", an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgReceiptCount(Actor actor, Long expectedCount) {
    return expectMsgReceiptCount(actor, "input", expectedCount);
  }

  /**
   * Add an expected count for messages sent on the default output port of the given actor.
   * <p>
   * When the actor doesn't have an output port named "output", an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgSentCount(Actor actor, Long expectedCount) {
    return expectMsgSentCount(actor, "output", expectedCount);
  }

  /**
   * Add an expected count for messages received on the given port.
   * <p>
   * When the port is not an input port, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param port
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgReceiptCount(Port port, Long expectedCount) {
    if (port == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      if (!port.isInput()) {
        throw new IllegalArgumentException("Port " + port.getFullName() + " is not an input port.");
      } else {
        String portName = port.getFullName().split("\\.", 3)[2];
        return expectMsgReceiptCount(portName, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages sent on the given port.
   * <p>
   * When the port is not an output port, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param port
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgSentCount(Port port, Long expectedCount) {
    if (port == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      if (!port.isOutput()) {
        throw new IllegalArgumentException("Port " + port.getFullName() + " is not an output port.");
      } else {
        String portName = port.getFullName().split("\\.", 3)[2];
        return expectMsgSentCount(portName, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages received on the given named input port of the given actor.
   * <p>
   * When the actor doesn't have a port with the given name, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param portName
   *          should be a simple name of the port of the given actor. I.e. not a full hierarchic name in the sense as described in the class doc, but e.g.
   *          plainly <code>"input"</code>.
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgReceiptCount(Actor actor, String portName, Long expectedCount) {
    if (actor == null || portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      Port p = (Port) actor.getPort(portName);
      if (p == null) {
        throw new IllegalArgumentException("Port " + portName + " does not exist for actor " + actor.getFullName());
      } else if (!p.isInput()) {
        throw new IllegalArgumentException("Port " + portName + " is not an input port.");
      } else {
        return expectMsgReceiptCount(p, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages sent on the given named output port of the given actor.
   * <p>
   * When the actor doesn't have a port with the given name, an <code>IllegalArgumentException</code> will be thrown.<br/>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actor
   * @param portName
   *          should be a simple name of the port of the given actor. I.e. not a full hierarchic name in the sense as described in the class doc, but e.g.
   *          plainly <code>"output"</code>.
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgSentCount(Actor actor, String portName, Long expectedCount) {
    if (actor == null || portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      Port p = (Port) actor.getPort(portName);
      if (p == null) {
        throw new IllegalArgumentException("Port " + portName + " does not exist for actor " + actor.getFullName());
      } else if (!p.isOutput()) {
        throw new IllegalArgumentException("Port " + portName + " is not an output port.");
      } else {
        return expectMsgSentCount(p, expectedCount);
      }
    }
  }

  /**
   * Add an expected count for messages received on the port with given name.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param portName
   *          check the class doc for port naming options
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgReceiptCount(String portName, Long expectedCount) {
    if (portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      portMsgReceiptCounts.put(portName, expectedCount);
      return this;
    }
  }

  /**
   * Add an expected count for messages sent on the port with given name.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param portName
   *          check the class doc for port naming options
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectMsgSentCount(String portName, Long expectedCount) {
    if (portName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      portMsgSentCounts.put(portName, expectedCount);
      return this;
    }
  }

  /**
   * Add an expected count for fire/process iterations for the given actor.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param port
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectActorIterationCount(Actor actor, Long expectedCount) {
    if (actor == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      String actorName = actor.getFullName().split("\\.", 3)[2];
      return expectActorIterationCount(actorName, expectedCount);
    }
  }

  /**
   * Add an expected count for fire/process iterations for the actor with given name.
   * <p>
   * When one of the arguments is null, a <code>NullPointerException</code> will be thrown.
   * </p>
   * 
   * @param actorName
   *          check the class doc for actor naming options
   * @param expectedCount
   * @return this FlowStatisticsAssertion instance to allow fluent method chaining
   */
  public FlowStatisticsAssertion expectActorIterationCount(String actorName, Long expectedCount) {
    if (actorName == null || expectedCount == null) {
      throw new NullPointerException("null arguments not allowed");
    } else {
      actorIterationCounts.put(actorName, expectedCount);
      return this;
    }
  }

  /**
   * Assert all specified expectations for message receipts on input ports, applied on the given flow instance.
   * 
   * @param flow
   * @param receivedCounts
   */
  protected void assertPortReceiptStatistics(Flow flow, Map<String, Long> receivedCounts) {
    if (receivedCounts != null) {
      for (Entry<String, Long> rcvCountEntry : receivedCounts.entrySet()) {
        String portName = rcvCountEntry.getKey();
        LOGGER.debug("assertPortReceiptStatistics called for port " + portName);
        long expCount = rcvCountEntry.getValue();
        if (portName.startsWith("." + flow.getName())) {
          // chop flow name
          portName = portName.split("\\.", 3)[2];
        }
        Port p = (Port) flow.getPort(portName);
        Assert.assertNotNull("No port " + portName + " found in flow " + flow.getName(), p);
        Assert.assertTrue("Port " + portName + " is not an input port.", p.isInput());
        Assert.assertEquals("Wrong received count for port " + portName, expCount, p.getStatistics().getNrReceivedMessages());
      }
    }
  }

  /**
   * Assert all specified expectations for message sent by output ports, applied on the given flow instance.
   * 
   * @param flow
   * @param sentCounts
   */
  protected void assertPortSentStatistics(Flow flow, Map<String, Long> sentCounts) {
    if (sentCounts != null) {
      for (Entry<String, Long> rcvCountEntry : sentCounts.entrySet()) {
        String portName = rcvCountEntry.getKey();
        LOGGER.debug("assertPortSentStatistics called for port " + portName);
        long expCount = rcvCountEntry.getValue();
        if (portName.startsWith("." + flow.getName())) {
          // chop flow name
          portName = portName.split("\\.", 3)[2];
        }
        Port p = (Port) flow.getPort(portName);
        Assert.assertNotNull("No port " + portName + " found in flow " + flow.getName(), p);
        Assert.assertTrue("Port " + portName + " is not an output port.", p.isOutput());
        Assert.assertEquals("Wrong sent count for port " + portName, expCount, p.getStatistics().getNrSentMessages());
      }
    }
  }

  /**
   * Assert all specified expectations for actor iteration counts, applied on the given flow instance.
   * 
   * @param flow
   * @param iterationCounts
   */
  protected void assertActorIterationStatistics(Flow flow, Map<String, Long> iterationCounts) {
    if (iterationCounts != null) {
      for (Entry<String, Long> itrCountEntry : iterationCounts.entrySet()) {
        String actorName = itrCountEntry.getKey();
        long expCount = itrCountEntry.getValue();
        if (actorName.startsWith("." + flow.getName())) {
          // chop flow name
          actorName = actorName.split("\\.", 3)[2];
        }
        Actor a = (Actor) flow.getEntity(actorName);
        Assert.assertNotNull("No actor " + actorName + " found in flow " + flow.getName(), a);
        if (expCount != 0) {
          Assert.assertEquals("Wrong iteration count for actor " + actorName, expCount, a.getStatistics().getNrCycles());
        } else {
          // in some test cases, actors may not even have been initialized if we look for 0 iterations
          if(a.getStatistics()!=null) {
            Assert.assertEquals("Wrong iteration count for actor " + actorName, expCount, a.getStatistics().getNrCycles());
          }
        }
      }
    }
  }
}
