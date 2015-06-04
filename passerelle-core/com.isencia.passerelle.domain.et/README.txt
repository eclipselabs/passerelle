ET : A new Passerelle execution domain based on Events and Tasks
================================================================

In high-end use-cases for Passerelle, the basic PN-based domain leads to system resource issues.
The combination of the centralization of model executions (in a Passerelle Manager), 
with the need to run many different models concurrently, leads to a need for an excessive number of threads.
I.e. each live actor needs at least 1 thread and often even more (to handle multiple PULL/blocking inputs).

The ET domain will provide a more scalable domain implementation, based on a combination of two proven concepts :

1. Events
--------- 
By using an event-based execution model, a simple executing model can in principle be driven 
from just one thread, i.o. a-thread-per-actor.

But compared to fwks like Scala actors, the situation in Passerelle is somewhat more complex. 
- Contrary to Scala/Akka actors that have one input "mailbox", Passerelle actors may have multiple input Ports,
  each potentially with multiple Receivers.
- The traditional Passerelle concepts of "relations" and "messages" and "Ports" must be unified with "events".
- Long-running tasks are typical i.o. exceptional. E.g. in the EDM-space, the "real" work done by actors consists
  of calling other systems to obtain data or launch tests etc, or of performing rules-based analysis on the collected data.

2. Tasks
--------
Within some Passerelle use cases, Tasks are already used to represent "work" done by actors.
Here Tasks are application-level entities with lifecycle events (representing state changes) and persistence.

Long-running tasks are often scheduled for asynchronous execution, in which case the task requester must register
a TaskListener to be notified about the task being finished successfully (or in error).

Such asynchronous execution in a system with need for high throughput and high concurrency is done with a managed
thread pool.


The ET domain formalizes a unified execution model based on events and tasks.

Each message being sent to an actor's input port is handled as an event on a model-wide event queue.
An EventDispatcher offers the events to the actors involved, which may lead to an actor doing one iteration,
if all its required inputs have been received.

An actor iteration typically causes messages being sent on one or more of its output ports, which in turn adds
new events on the event queue.

An empty event queue, combined with exhausted source actors, means that the model can be wrapped up.
But with long-running Tasks, we need to ensure that the model Director is aware of them, so models are not 
prematurely wrapped up. I.e. when the event queue would be empty but there would still be tasks busy.

To allow this, the Task concept will be generalized within the Passerelle engine and execution domain(s).
 