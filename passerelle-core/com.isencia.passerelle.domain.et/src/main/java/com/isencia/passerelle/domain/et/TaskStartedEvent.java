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

package com.isencia.passerelle.domain.et;

import java.text.DateFormat;
import java.util.Date;
import ptolemy.actor.Actor;
import ptolemy.kernel.util.NamedObj;

/**
 * @author delerw
 *
 */
public class TaskStartedEvent<T> extends AbstractEvent {
  
  private static final long serialVersionUID = 6544105600466662464L;

  public final static String TOPIC=TOPIC_PREFIX+"STARTED";

  private Actor taskOwner;
  private T task;

  protected TaskStartedEvent(T task, Actor taskOwner) {
    this(task, taskOwner, new Date());
  }

  protected TaskStartedEvent(T task, Actor taskOwner, Date timeStamp) {
    super((NamedObj) taskOwner, TOPIC, timeStamp);
    this.task = task;
    this.taskOwner = taskOwner;
  }

  public TaskStartedEvent<T> copy() {
    return new TaskStartedEvent<T>(task, taskOwner);
  }
  
  public Actor getTaskOwner() {
    return taskOwner;
  }

  public T getTask() {
    return task;
  }

  public String toString(DateFormat dateFormat) {
    return dateFormat.format(getCreationTS()) + " " + getId() + "TaskStartedEvent [taskOwner=" + taskOwner.getFullName() 
        + ", task=" + task + "]";
  }
}
