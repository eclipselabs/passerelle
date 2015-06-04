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

package com.isencia.passerelle.validation;

import ptolemy.kernel.attributes.VersionAttribute;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.testsupport.actor.Const;
import com.isencia.passerelle.testsupport.actor.DevNullActor;
import com.isencia.passerelle.testsupport.actor.Delay;
import com.isencia.passerelle.validation.version.ActorVersionRegistry;
import com.isencia.passerelle.validation.version.VersionSpecification;
import junit.framework.TestCase;

/**
 * @author delerw
 *
 */
public class ModelValidationServiceTest extends TestCase {

  private Flow flow;

  protected void setUp() throws Exception {
    ActorVersionRegistry.getInstance().addActorVersion(Const.class.getName(), VersionSpecification.parse("1.2.3"));
    ActorVersionRegistry.getInstance().addActorVersion(Delay.class.getName(), VersionSpecification.parse("10.0.0-hello"));
    flow = new Flow("actor version validation unit test", null);
  }

  protected void tearDown() throws Exception {
    ActorVersionRegistry.getInstance().clear();
  }
  
  public void testAllVersionsIdentical() throws Exception {
    Const constActor = new Const(flow, "const");
    new VersionAttribute(constActor, "_version").setExpression("1.2.3");
    
    Delay workerActor = new Delay(flow, "worker");
    new VersionAttribute(workerActor, "_version").setExpression("10.0.0-hello");
    
    flow.connect(constActor, workerActor);
    
    ValidationContext context = new ValidationContext();
    ModelValidationService.getInstance().validate(flow, context );
    
    assertTrue("Actors with identical versions as registered, should be validated OK", context.isValid());
  }
  
  public void testMinorVersionsDifferent() throws Exception {
    Const constActor = new Const(flow, "const");
    new VersionAttribute(constActor, "_version").setExpression("1.6.3");
    
    Delay workerActor = new Delay(flow, "worker");
    new VersionAttribute(workerActor, "_version").setExpression("10.1.0-world");
    
    flow.connect(constActor, workerActor);
    
    ValidationContext context = new ValidationContext();
    ModelValidationService.getInstance().validate(flow, context );
    
    assertFalse("Actors with different minor versions as registered, should be validated NOK", context.isValid());
  }
  
  public void testAllVersionsIdenticalWithOneExtra() throws Exception {
    Const constActor = new Const(flow, "const");
    new VersionAttribute(constActor, "_version").setExpression("1.2.3");
    
    Delay workerActor = new Delay(flow, "worker");
    new VersionAttribute(workerActor, "_version").setExpression("10.0.0-hello");
    
    DevNullActor devNullActor = new DevNullActor(flow, "devNull");
    new VersionAttribute(devNullActor, "_version").setExpression("5.5.5");

    flow.connect(constActor, workerActor);
    flow.connect(workerActor, devNullActor);
    
    ValidationContext context = new ValidationContext();
    ModelValidationService.getInstance().validate(flow, context );
    
    assertTrue("Actors with identical versions as registered, and one extra with no registered version constraint, should be validated OK", context.isValid());
  }

  public void testMajorVersionTooHigh() throws Exception {
    Const constActor = new Const(flow, "const");
    new VersionAttribute(constActor, "_version").setExpression("2.6.3");
    
    Delay workerActor = new Delay(flow, "worker");
    new VersionAttribute(workerActor, "_version").setExpression("10.1.0-world");
    
    flow.connect(constActor, workerActor);
    
    ValidationContext context = new ValidationContext();
    ModelValidationService.getInstance().validate(flow, context );
    
    assertFalse("Actors with newer major versions as registered, should be validated NOK", context.isValid());
    assertTrue("Const actor should be marked with invalid version", !context.getErrors(constActor).isEmpty());
  }
  
  public void testMajorVersionTooLow() throws Exception {
    Const constActor = new Const(flow, "const");
    new VersionAttribute(constActor, "_version").setExpression("1.6.3");
    
    Delay workerActor = new Delay(flow, "worker");
    new VersionAttribute(workerActor, "_version").setExpression("9.1.0-world");
    
    flow.connect(constActor, workerActor);
    
    ValidationContext context = new ValidationContext();
    ModelValidationService.getInstance().validate(flow, context );
    
    assertFalse("Actors with newer major versions as registered, should be validated NOK", context.isValid());
    assertTrue("Delay actor should be marked with invalid version",  !context.getErrors(workerActor).isEmpty());
  }
  

}
