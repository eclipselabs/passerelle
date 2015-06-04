/* Copyright 2011 - iSencia Belgium NV

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
package com.isencia.passerelle.actor.control;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.SingletonAttribute;
import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.actor.Sink;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.message.ManagedMessage;

//////////////////////////////////////////////////////////////////////////
//// Stop
/**
 * SLIGHT VARIATION ON THE PTOLEMY STOP ACTOR, FOR PASSERELLE: STOPS MODEL
 * EXECUTION ON RECEIVING ANY KIND OF MESSAGE, NOT JUST TRUE TOKENS... 
 * An actor
 * that stops execution of a model when it receives a true token on any input
 * channel. This is accomplished by calling finish() on the manager, which
 * requests that the current iteration be completed and then the model execution
 * be halted. If the input is not connected to anything, then this actor
 * requests a stop whenever it fires.
 * <p>
 * When exactly this stops the execution depends on the domain. For example, in
 * DE, if an event with time stamp <i>T</i> and value <i>true</i> arrives at
 * this actor, then the current iteration will be concluded, and then the model
 * will halt. Concluding the current iteration means processing all events in
 * the event queue with time stamp <i>T</i>. Thus, it is possible for actors to
 * be invoked after this one is invoked with a <i>true</i> input.
 * <p>
 * In SDF, if this actor receives <i>true</i>, then the current iteration is
 * concluded and then execution is stopped. Similarly in SR.
 * <p>
 * In PN, where each actor has its own thread, there is no well-defined notion
 * of an iteration. The finish() method of the manager calls stopFire() on all
 * actors, which for threaded actors results in halting them upon their next
 * attempt to read an input or write an output. When all actor threads have
 * stopped, the iteration concludes and the model halts. <b>NOTE</b>: <i>This is
 * not the best way to stop a PN model!</i> This mechanism is nondeterministic
 * in the sense that there is no way to control exactly what data is produced or
 * consumed on the connections before the model stops. To stop a PN model, it is
 * better to design the model so that all actors are starved of data when the
 * model is to stop. The director will detect this starvation, and halt the
 * model. Nonetheless, if the nondeterminism is acceptable, this actor can be
 * used.
 * 
 * @author Edward A. Lee
 * @version $Id: Stop.java,v 1.1 2006/04/06 21:08:00 erwin Exp $
 * @since Ptolemy II 2.1
 * @Pt.ProposedRating Green (eal)
 * @Pt.AcceptedRating Green (neuendor)
 */

public class Stop extends Sink {

  private static final long serialVersionUID = 1L;

  /**
   * Construct an actor in the specified container with the specified name.
   * 
   * @param container The container.
   * @param name The name of this actor within the container.
   * @exception IllegalActionException If the actor cannot be contained by the
   *              proposed container.
   * @exception NameDuplicationException If the name coincides with an actor
   *              already in the container.
   */
  public Stop(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);

    // Icon is a stop sign.
    _attachText("_iconDescription", "<svg>\n" + "<polygon points=\"-8,-19 8,-19 19,-8 19,8 8,19 " + "-8,19 -19,8 -19,-8\" " + "style=\"fill:red\"/>\n"
        + "<text x=\"-15\" y=\"4\"" + "style=\"font-size:11; fill:white; font-family:SansSerif\">" + "STOP</text>\n" + "</svg>\n");
    // Hide the name because the name is in the icon.
    SingletonAttribute hideAttribute = new SingletonAttribute(this, "_hideName");
    hideAttribute.setPersistent(false);
  }

  public boolean doPostfire() throws IllegalActionException {
    return false;
  }

  protected void sendMessage(ManagedMessage outgoingMessage) throws ProcessingException {
    Nameable container = getContainer();
    if (container instanceof CompositeActor) {
      Manager manager = ((CompositeActor) container).getManager();
      if (manager != null) {
        manager.finish();
      } else {
        throw new ProcessingException(ErrorCode.FLOW_EXECUTION_ERROR, "Cannot stop without a Manager.", this, null);
      }
    } else {
      throw new ProcessingException(ErrorCode.FLOW_EXECUTION_ERROR, "Cannot stop without a container that is a CompositeActor.", this, null);
    }
  }
}
