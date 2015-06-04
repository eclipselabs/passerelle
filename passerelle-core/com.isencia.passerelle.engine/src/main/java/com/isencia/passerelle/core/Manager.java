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
package com.isencia.passerelle.core;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.ext.PausableResumable;

/**
 * Manager implementation with Passerelle extensions
 * 
 * @author erwin
 */
public class Manager extends ptolemy.actor.Manager {

	public Manager(Workspace workspace, String name) throws IllegalActionException {
		super(workspace, name);
	}

	@Override
	public void pause() {
		super.pause();
		_setState(PAUSED);
		// the invocation of stopFire/pauseFire is already done in the Manager base class
		
		// if super.pause is not done :
		// we explicitly invoke pauseFire on all contained actors (if they're passerelle actors)
//		NamedObj container = getContainer();
//		if(container instanceof CompositeActor) {
//			Director director = ((CompositeActor)container).getDirector();
//			if(director instanceof PausableResumable) {
//				((PausableResumable)director).pauseAllActors();
//			}
//		}
	}

	@Override
	public void resume() {
		// we explicitly invoke resumeFire on all contained actors (if they're passerelle actors)
		NamedObj container = getContainer();
		if(container instanceof CompositeActor) {
			Director director = ((CompositeActor)container).getDirector();
			if(director instanceof PausableResumable) {
				((PausableResumable)director).resumeAllActors();
			}
		}
		super.resume();
		_setState(ITERATING);
	}
}
