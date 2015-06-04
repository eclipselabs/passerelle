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
package com.isencia.passerelle.domain.cap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.actor.InitializationException;
import com.isencia.passerelle.domain.ProcessDirector;
import com.isencia.passerelle.message.MessageQueue;
import com.isencia.passerelle.message.SimpleActorMessageQueue;

/**
 * The standard Passerelle director. Besides the std Ptolemy director stuff,
 * such as providing custom receivers and process threads, this director adds:
 * <ul>
 * <li> Support for centralized maintenance of a scheduler instance
 * </ul>
 * 
 * @author dirk
 * @author erwin
 */
public class Director extends ProcessDirector {
	//~ Static variables/initializers __________________________________________________________________________________________________________________________

	private static Logger logger = LoggerFactory.getLogger(Director.class);

	//~ Instance variables _____________________________________________________________________________________________________________________________________
	private File propsFile=null;
	public FileParameter propsFileParameter;
	public final static String PROPSFILE_PARAM = "Properties File";
	
	//~ Constructors ___________________________________________________________________________________________________________________________________________

	/** Construct a director in the default workspace with an empty string
	 *  as its name. The director is added to the list of objects in
	 *  the workspace. Increment the version number of the workspace.
	 *  Create a director parameter "Initial_queue_capacity" with the default
	 *  value 1. This sets the initial capacities of the queues in all
	 *  the receivers created in the PN domain.
	 */
	public Director() throws IllegalActionException, NameDuplicationException {
		this(null);
	}

	/** Construct a director in the  workspace with an empty name.
	 *  The director is added to the list of objects in the workspace.
	 *  Increment the version number of the workspace.
	 *  Create a director parameter "Initial_queue_capacity" with the default
	 *  value 1. This sets the initial capacities of the queues in all
	 *  the receivers created in the PN domain.
	 *  @param workspace The workspace of this object.
	 */
	public Director(Workspace workspace)
		throws IllegalActionException, NameDuplicationException {
		super(workspace);
		propsFileParameter = new FileParameter(this, PROPSFILE_PARAM);
		// to trigger the creation of our default adapter
		getAdapter(null);
	}

	/** Construct a director in the given container with the given name.
	 *  If the container argument must not be null, or a
	 *  NullPointerException will be thrown.
	 *  If the name argument is null, then the name is set to the
	 *  empty string. Increment the version number of the workspace.
	 *
	 *  Create a director parameter "Initial_queue_capacity" with the default
	 *  value 1. This sets the initial capacities of the queues in all
	 *  the receivers created in the PN domain.
	 *  @param container Container of the director.
	 *  @param name Name of this director.
	 *  @exception IllegalActionException If the director is not compatible
	 *   with the specified container.  Thrown in derived classes.
	 *  @exception NameDuplicationException If the container not a
	 *   CompositeActor and the name collides with an entity in the container.
	 */
	public Director(CompositeEntity container, String name)
		throws IllegalActionException, NameDuplicationException {
		super(container, name);
		
		propsFileParameter = new FileParameter(this, PROPSFILE_PARAM);

    // to trigger the creation of our default adapter
    getAdapter(null);
    
		_attachText(
			"_iconDescription",
			"<svg>\n"
				+ "<polygon points=\"-20,0 -10,-18 10,-18 20,0 10,18 -10,18\" "
				+ "style=\"fill:red;stroke:red\"/>\n"
				+ "<line x1=\"-9.5\" y1=\"17\" x2=\"-19\" y2=\"0\" "
				+ "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"-19\" y1=\"0\" x2=\"-9.5\" y2=\"-17\" "
				+ "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"-9\" y1=\"-17\" x2=\"9\" y2=\"-17\" "
				+ "style=\"stroke-width:1.0;stroke:white\"/>\n"
				+ "<line x1=\"10\" y1=\"-17.5\" x2=\"20\" y2=\"0\" "
				+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"20\" y1=\"0\" x2=\"10\" y2=\"17.5\" "
				+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"10\" y1=\"17.5\" x2=\"-10\" y2=\"17.5\" "
				+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
				+ "<line x1=\"11\" y1=\"-15\" x2=\"19\" y2=\"0\" "
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ "<line x1=\"19\" y1=\"0\" x2=\"11\" y2=\"16\" "
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ "<line x1=\"10\" y1=\"17\" x2=\"-9\" y2=\"17\" "
				+ "style=\"stroke-width:1.0;stroke:grey\"/>\n"
				+ 

		// director stand
		"<line x1=\"0\" y1=\"0\" x2=\"0\" y2=\"10\" "
			+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
			+ "<line x1=\"-6\" y1=\"10\" x2=\"6\" y2=\"10\" "
			+ "style=\"stroke-width:1.0;stroke:black\"/>\n"
			+ "<polygon points=\"-8,0 -6,-8 8,-8 6,0\" "
			+ "style=\"fill:lightgrey\"/>\n"
			+ 

		//magic wand
		"<line x1=\"5\" y1=\"-15\" x2=\"15\" y2=\"-5\" "
			+ "style=\"stroke-width:2.0;stroke:black\"/>\n"
			+ "<line x1=\"5\" y1=\"-15\" x2=\"6\" y2=\"-14\" "
			+ "style=\"stroke-width:2.0;stroke:white\"/>\n"
			+ 
		// sparkles
		"<circle cx=\"12\" cy=\"-16\" r=\"1\""
			+ "style=\"fill:black;stroke:white\"/>\n"
			+ "<circle cx=\"16\" cy=\"-16\" r=\"1\""
			+ "style=\"fill:black;stroke:white\"/>\n"
			+ "<circle cx=\"14\" cy=\"-14\" r=\"1\""
			+ "style=\"fill:black;stroke:white\"/>\n"
			+ "</svg>\n");
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param workspace DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws CloneNotSupportedException DOCUMENT ME!
	 */
	public Object clone(Workspace workspace)
		throws CloneNotSupportedException {
		Director newObject = (Director) super.clone(workspace);

		return newObject;
	}

	public void attributeChanged(Attribute attribute) throws IllegalActionException {

		if(logger.isTraceEnabled())
			logger.trace(getName()+" :"+attribute);
			
		if (attribute == propsFileParameter) {
			try {
				File asFile = propsFileParameter.asFile();
				if (asFile != null) {
					String propsPath = asFile.getPath();
					propsFile = new File(propsPath);
					logger.debug("System Properties file changed to : " + propsPath);
				}
			} catch (NullPointerException e) {
				// Ignore. Means that path is not a valid URL.
			}
		} else 
			super.attributeChanged(attribute);

		if(logger.isTraceEnabled())
			logger.trace(getName());
	}
	
	public void initialize() throws IllegalActionException {
		if(logger.isTraceEnabled())
			logger.trace(getName()+" initialize() - entry");
		
		if(propsFile!=null) {
			try {
				InputStream propsInput = new FileInputStream(propsFile);
				System.getProperties().load(propsInput);
			} catch (FileNotFoundException e) {
				logger.error("", e);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		super.initialize();
		if(logger.isTraceEnabled())
			logger.trace(getName()+" initialize() - exit");
	}


	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Receiver newReceiver() {
		BlockingQueueReceiver receiver = new BlockingQueueReceiver();
		try {
			receiver.setCapacity(FIFOQueue.INFINITE_CAPACITY);
		} catch (IllegalActionException e) {
		}

		return receiver;
	}

  @Override
  public MessageQueue newMessageQueue(Actor actor) throws InitializationException {
    return new SimpleActorMessageQueue(actor);
  }


	/**
	 * 
	 * @return unmodifiable copy of all receivers managed by this Director,
	 * i.e. of the input ports of the actors in this Director's model.
	 */
	public Collection<BlockingQueueReceiver> getManagedReceivers() {
		return Collections.emptyList();
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IllegalActionException DOCUMENT ME!
	 */
	public boolean postfire() throws IllegalActionException {
		if(logger.isTraceEnabled())
			logger.trace(getName()+" postfire() - entry");
		
		boolean res = true;
        _notDone = super.postfire();
        
		//If the container has input ports and there are active processes
		//in the container, then the execution might restart on receiving
		// additional data.
		if (!((((CompositeActor) getContainer()).inputPortList()).isEmpty())
			&& (_getActiveThreadsCount() != 0)) {
			// System.out.println("DIRECTOR.POSTFIRE() returning " + _notDone);
            res = !_stopRequested;
		} else {
			//System.out.println("DIRECTOR.POSTFIRE() returning " + _notDone
			//	    + " again.");
			res = _notDone;
		}
		if(logger.isTraceEnabled())
			logger.trace(getName()+" postfire() - exit - returning :"+res);
		
		return res;
	}

	/* (non-Javadoc)
	 * @see ptolemy.actor.Executable#wrapup()
	 */
	public void wrapup() throws IllegalActionException {
		if(logger.isTraceEnabled())
			logger.trace(getName()+" wrapup() - entry");
		
		super.wrapup();
		if(logger.isTraceEnabled())
			logger.trace(getName()+" wrapup() - exit");
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IllegalActionException DOCUMENT ME!
	 */
	protected boolean _resolveInternalDeadlock()
		throws IllegalActionException {
		if (_getActiveThreadsCount() == 0) {
			return false;
		} else {
			return true;
		}
	}
}