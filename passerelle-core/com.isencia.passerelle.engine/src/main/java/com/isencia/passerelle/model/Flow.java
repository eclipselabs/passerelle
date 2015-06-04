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
package com.isencia.passerelle.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.ext.DirectorAdapter;

/**
 * A Flow represents an assembly of interconnected Passerelle actors,
 * with a single Director.
 * <br>
 * Flows can contain sub-flows. Sub-flows do not have own Directors.
 *
 * @author erwin
 *
 */
public class Flow extends TypedCompositeActor {
  private static final long serialVersionUID = 1L;

  /**
	 * For a top-level Flow instance, this field contains the authorative resource location
	 * from which this Flow instance was constructed.
	 * 
	 * This is typically either a local moml file
	 * (with an URL with the file:// schema)
	 * or a moml managed on a Passerelle Manager server instance
	 * (with an URL corresponding to a REST resource id, with the http:// schema) 
	 */
	private URL authorativeResourceLocation;
	
	private FlowHandle handle;

	private ExecutionListener executionListener;
	
	/**
	 * property used to flag whether the Flow was loaded/parsed without issues.
	 * if there were issues, this may impact the supported actions on the flow,
	 * as it is probably incomplete/crippled then...
	 */
	private boolean loadedFaultless = true;
	
	public void reportLoadingError(Throwable t) {
		if(t!=null)
			loadedFaultless = false;
	}
	
	public boolean isLoadedFaultless() {
		return loadedFaultless;
	}

	public ExecutionListener getExecutionListener() {
		return executionListener;
	}

	public void setExecutionListener(final ExecutionListener executionListener) {
		this.executionListener = executionListener;
	}

	/**
	 * Create a top-level flow with the given name.
	 *
	 * @param name
	 * @param authorativeResourceLocation only filled in when flow is
	 * parsed from a file resource, either from a local moml
	 * or a moml obtained from a passerelle manager.
	 * 
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 *
	 */
	public Flow(String name, URL authorativeResourceLocation) throws IllegalActionException, NameDuplicationException {
		super(new Workspace(name));
		setName(name);
		this.authorativeResourceLocation = authorativeResourceLocation;
		handle = new FlowHandle(0L, this, authorativeResourceLocation);
	}


	/**
	 * Create a top-level flow in the given workspace.
	 * 
	 * @param workspace
	 * @param authorativeResourceLocation only filled in when flow is
	 * parsed from a file resource, either from a local moml
	 * or a moml obtained from a passerelle manager.
	 */
	public Flow(Workspace workspace, URL authorativeResourceLocation) {
		super(workspace);
		this.authorativeResourceLocation = authorativeResourceLocation;
		handle = new FlowHandle(0L, this, authorativeResourceLocation);
	}

  @Override
  public void setName(String name) throws IllegalActionException, NameDuplicationException {
    super.setName(name);
    if (handle != null) {
      handle.setName(name);
    }
  }
	
	/**
	 * 
	 * @return the authorative resource location
	 * from which this Flow instance was constructed.
	 * <p>
	 * This is typically either :
	 * <ul>
	 * <li>null, for a newly constructed flow instance that has
	 * not been parsed from a moml resource/file
	 * <li>a local moml file (with an URL with the file:// schema)
	 * <li>or a moml managed on a Passerelle Manager server instance<br/>
	 * (with an URL corresponding to a REST resource id, <br/>with the http:// schema) 
	 * </ul>
	 * </p>
	 */
	public URL getAuthorativeResourceLocation() {
		return authorativeResourceLocation;
	}

	public FlowHandle getHandle() {
		return handle;
	}

	public void setHandle(FlowHandle handle) {
		this.handle = handle;
		setAuthorativeResourceLocation(handle.getAuthorativeResourceLocation());
	}
	
	/**
	 * only for use by flowmanager
	 * @param authorativeResourceLocation the authorativeResourceLocation to set
	 */
	void setAuthorativeResourceLocation(URL authorativeResourceLocation) {
		this.authorativeResourceLocation = authorativeResourceLocation;
	}


	/**
	 *
	 * Create a sub-flow with the given name, in the given parent flow.
	 *
	 * @param parent
	 * @param name
	 * @throws IllegalActionException
	 * @throws NameDuplicationException
	 */
	public Flow(Flow parent, String name)
			throws IllegalActionException, NameDuplicationException {
		super(parent, name);
	}

	/**
	 * Builds a default connection between actor a1 and actor a2,
	 * i.e. connecting a1's output port with default name "output"
	 * with a2's input port with default name "input".
	 *
	 * @param a1
	 * @param a2
	 */
	public void connect(Actor a1, Actor a2) {
		IOPort a1Output = (IOPort) ((ComponentEntity)a1).getPort("output");
		IOPort a2Input = (IOPort) ((ComponentEntity)a2).getPort("input");
		try {
			super.connect(a1Output, a2Input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Builds a connection from the given output port
	 * to the given input port.
	 *
	 * @param output
	 * @param input
	 */
	public void connect(IOPort output, IOPort input) {
		try {
			super.connect(output, input);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Removes any existing connections between actors a1 and a2.
	 *
	 * @param a1
	 * @param a2
	 */
	@SuppressWarnings("unchecked")
	public void disconnect(final Actor a1, final Actor a2) {
		final List<IOPort> a1Ports = ((ComponentEntity) a1).portList();
		final List<IOPort> a2Ports = ((ComponentEntity) a2).portList();
		for (final IOPort port1 : a1Ports) {
			for (final IOPort port2 : a2Ports) {
				try {
					disconnect(port1, port2);
				} catch (final Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Removes any existing connections between the ports output and input.
	 *
	 * @param output
	 * @param input
	 */
	@SuppressWarnings("unchecked")
	public void disconnect(final IOPort output, final IOPort input) {
		final List<Relation> relations = output.linkedRelationList();
		for (final Relation relation : relations) {
			if(input.isLinked(relation)) {
				input.unlink(relation);
				output.unlink(relation);
			}
			if(relation.linkedPortList().isEmpty()) {
				// the relation was only between the given ports
				// so now it doesn't connect anything anymore
				// and we'll just remove it altogether
				try {
					((ComponentRelation)relation).setContainer(null);
				} catch (final Exception e) {
					// ignore
				}
			}
		}
	}
	
	public DirectorAdapter getDirectorAdapter() throws IllegalActionException {
      return (DirectorAdapter) getDirector().getAttribute(DirectorAdapter.DEFAULT_ADAPTER_NAME, DirectorAdapter.class);
	}

	/**
	 * Tries to create a new actor instance in this flow, with the given class and name.
	 *
	 * @param actorClass
	 * @param actorName
	 * @return
	 * @throws NameDuplicationException
	 * @throws Exception
	 */
	public Actor addActor(Class<? extends Actor> actorClass, String actorName) throws NameDuplicationException, Exception {
		Object[] constructorArgs = new Object[] { this, actorName };
		Actor result=null;
		for (Constructor<Actor> constructor : (Constructor<Actor>[])actorClass.getConstructors()) {
			Class<?>[] paramTypes = constructor.getParameterTypes();
			if (paramTypes.length == constructorArgs.length) {
				boolean itsTheGoodOne = true;
				for (int i = 0; i < paramTypes.length; i++) {
					if(!paramTypes[i].isInstance(constructorArgs[i])) {
						itsTheGoodOne=false;
						break;
					}
				}
				if (itsTheGoodOne) {
					try {
						result = constructor.newInstance(constructorArgs);
					} catch (InvocationTargetException e) {
						if (e.getCause() instanceof NameDuplicationException) {
							throw (NameDuplicationException) e.getCause();
						} else {
							throw e;
						}
					} catch (Exception e) {
						throw e;
					}
				}
			}
		}
		return result;
	}

	public Collection<Parameter> getAllParameters() {
		return getAllParameters(this);
	}

	@SuppressWarnings("unchecked")
	private Collection<Parameter> getAllParameters(CompositeActor composite) {
		Collection<Parameter> results = new HashSet<Parameter>();
		List<Entity> actors = composite.entityList();
		final Director director = composite.getDirector();
		if (director != null) {
			results.addAll(director.attributeList(Parameter.class));
		}

		results.addAll(composite.attributeList(Parameter.class));
		for (Entity actor : actors) {
			// add actor's own parameters
			results.addAll(actor.attributeList(Parameter.class));
			// if it's a composite, add the ones of the contained actors
			if(actor instanceof CompositeActor)
				results.addAll(getAllParameters((CompositeActor)actor));
		}
		return results;
	}

    /** Return a name that is guaranteed to not be the name of
     *  any contained attribute, port, class, entity, or relation.
     *  In this implementation, the argument
     *  is stripped of any numeric suffix, and then a numeric suffix
     *  is appended and incremented until a name is found that does not
     *  conflict with a contained attribute, port, class, entity, or relation.
     *  If this composite entity or any composite entity that it contains
     *  defers its MoML definition (i.e., it is an instance of a class or
     *  a subclass), then the prefix gets appended with "_<i>n</i>_",
     *  where <i>n</i> is the depth of this deferral. That is, if the object
     *  deferred to also defers, then <i>n</i> is incremented.
     *  @param prefix A prefix for the name.
     *  @return A unique name.
     */
	@Override
    public String uniqueName(String prefix) {
        if (prefix == null) {
            prefix = "null";
        }

        prefix =_stripNumericSuffix(prefix);

        String candidate = prefix;

        // NOTE: The list returned by getPrototypeList() has
        // length equal to the number of containers of this object
        // that return non-null to getParent(). That number is
        // assured to be at least one greater than the corresponding
        // number for any of the parents returned by getParent().
        // Hence, we can use that number to minimize the likelyhood
        // of inadvertent capture.
        try {
            int depth = getPrototypeList().size();

            if (depth > 0) {
                prefix = prefix + "_" + depth + "_";
            }
        } catch (IllegalActionException e) {
            // Derivation invariant is not satisified.
            throw new InternalErrorException(e);
        }

        int uniqueNameIndex = 2;

        while ((getAttribute(candidate) != null)
                || (getPort(candidate) != null)
                || (getEntity(candidate) != null)
                || (getRelation(candidate) != null)) {
            candidate = prefix + "_" + uniqueNameIndex++;
        }

        return candidate;
    }
    /** Return a string that is identical to the specified string
     *  except any trailing digits are removed which were preceded by a '_'.
     *  @param string The string to strip of its numeric suffix.
     *  @return A string with no numeric suffix.
     */
    protected static String _stripNumericSuffix(String string) {
        int length = string.length();
        char[] chars = string.toCharArray();

        boolean numericSuffixFound=false;
        
        for (int i = length - 1; i >= 0; i--) {
            char current = chars[i];

            if (Character.isDigit(current)) {
                length--;
            } else {
                if (current == '_') {
                	if(length<string.length()) {
                		numericSuffixFound=true;
                		length--;
                	}
                }
                // Found a non-numeric, so we are done.
                break;
            }
        }

        if (numericSuffixFound) {
            // Some stripping occurred.
            char[] result = new char[length];
            System.arraycopy(chars, 0, result, 0, length);
            return new String(result);
        } else {
            return string;
        }
    }

}
