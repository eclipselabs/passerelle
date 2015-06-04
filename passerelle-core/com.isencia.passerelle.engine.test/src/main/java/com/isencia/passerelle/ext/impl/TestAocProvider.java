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

import java.net.URL;
import ptolemy.kernel.CompositeEntity;
import com.isencia.passerelle.engine.activator.Activator;
import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.model.util.MoMLParser;
import com.isencia.passerelle.validation.version.VersionSpecification;

/**
 * @author erwin
 */
public class TestAocProvider implements ActorOrientedClassProvider {

  private MoMLParser parser;

  public TestAocProvider() {
    initialize();
  }

  protected void initialize() {
    ClassLoader classLoader = null;
    try {
      classLoader = Activator.class.getClassLoader();
    } catch (final NoClassDefFoundError e) {
      // Activator class not found, so not inside an OSGi container
      classLoader = TestAocProvider.class.getClassLoader();
    }

    parser = new MoMLParser(null, classLoader);
  }

  public CompositeEntity getActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {

    if(versionSpec!=null) {
      className = className+"_"+versionSpec;
    }
    try {
      URL submodelResource = this.getClass().getResource(className+".xml");
      if(submodelResource==null) {
        // try with .moml extension
        submodelResource = this.getClass().getResource(className+".moml");
      }
      if(submodelResource==null) {
        throw new ClassNotFoundException("Could not find "+className);
      }
      return (CompositeEntity) parser.parse(null, submodelResource);
    } catch (Exception e) {
      throw new ClassNotFoundException("Could not parse "+className, e);
    }
  }

}
