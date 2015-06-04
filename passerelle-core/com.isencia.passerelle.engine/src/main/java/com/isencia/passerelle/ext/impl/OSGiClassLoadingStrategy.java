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

package com.isencia.passerelle.ext.impl;

import java.util.HashSet;
import java.util.Set;
import ptolemy.kernel.CompositeEntity;
import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.ext.ClassLoadingStrategy;
import com.isencia.passerelle.ext.ModelElementClassProvider;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * This is the preferred <code>ClassLoadintStrategy</code> implementation in a full-blown Passerelle OSGi-based runtime.
 * It supports dynamic actor class updates through OSGi's great dynamism based on micro-services.
 * <p>
 * This dynamism is obtained by delegating the class loading to the registered implementations of <code>ModelElementClassProvider</code> and <code>MomlClassProvider</code>.
 * </p>
 * <p>
 * 
 * </p>
 * @author delerw
 *
 */
public class OSGiClassLoadingStrategy implements ClassLoadingStrategy {

  private ClassLoadingStrategy parentLoader;
  
  private Set<ModelElementClassProvider> modelElementClassProviders = new HashSet<ModelElementClassProvider>();
  private Set<ActorOrientedClassProvider> actorOrientedClassProviders = new HashSet<ActorOrientedClassProvider>();

  /**
   * 
   */
  public OSGiClassLoadingStrategy() {
  }

  /**
   * @param classLoader
   */
  public OSGiClassLoadingStrategy(ClassLoadingStrategy parentLoader) {
    this.parentLoader = parentLoader;
  }
  
  public Class loadJavaClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    Class result = null;
    
    for(ModelElementClassProvider classProvider : modelElementClassProviders) {
      try {
        result=classProvider.getClass(className, versionSpec);
        if(result!=null) {
          break;
        }
      } catch (ClassNotFoundException e) {
        // just means the provider doesn't know about this one
      }
    }
    if(result==null && parentLoader!=null) {
      return parentLoader.loadJavaClass(className, null);
    } else {
      return result;
    }
  }

  public CompositeEntity loadActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    CompositeEntity result = null;
    
    for(ActorOrientedClassProvider classProvider : actorOrientedClassProviders) {
      try {
        result=classProvider.getActorOrientedClass(className, versionSpec);
        if(result!=null) {
          break;
        }
      } catch (ClassNotFoundException e) {
        // just means the provider doesn't know about this one
      }
    }
    if(result==null && parentLoader!=null) {
      return parentLoader.loadActorOrientedClass(className, null);
    } else {
      return result;
    }
  }

  // provider registration mgmt stuff
  
  public boolean addModelElementClassProvider(ModelElementClassProvider classProvider) {
    return modelElementClassProviders.add(classProvider);
  }
  
  public boolean removeModelElementClassProvider(ModelElementClassProvider classProvider) {
    return modelElementClassProviders.remove(classProvider);
  }
  
  public void clearModelElementClassProviders() {
    modelElementClassProviders.clear();
  }
  
  public boolean addActorOrientedClassProvider(ActorOrientedClassProvider classProvider) {
    return actorOrientedClassProviders.add(classProvider);
  }

  public boolean removeActorOrientedClassProvider(ActorOrientedClassProvider classProvider) {
    return actorOrientedClassProviders.remove(classProvider);
  }

  public void clearActorOrientedClassProviders() {
    actorOrientedClassProviders.clear();
  }
  
}
