/* Copyright 2013 - iSencia Belgium NV

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
package com.isencia.passerelle.flow;

import java.io.InputStreamReader;
import java.io.Reader;
import junit.framework.TestCase;
import ptolemy.kernel.util.Workspace;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.model.FlowManager;
import com.isencia.passerelle.testsupport.FlowDefinitionAssertion;

public class FlowDefinitionTest extends TestCase {

  /**
   * A test with a simple model with 3 Const-to-DevNull connections :
   * <ul>
   * <li>1 : direct connection</li>
   * <li>2 : connection via 1 vertex/"diamond"</li>
   * <li>3 : connection via 2 vertices/"diamonds"</li>
   * </ul>
   * The test checks whether all ports are correctly connected via functioning relations, as defined in the moml.
   * I.e. that the output ports can reach the expected input ports via relations and receivers.
   */
  public void testConnectionsWithVertices() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/testDoubleVertices.moml"));
    Flow f = FlowManager.readMoml(in);

    new FlowDefinitionAssertion()
    .expectRelation("Constant_1.output", "Console_1.input")
    .expectRelation("Constant_2.output", "Console_2.input")
    .expectRelation("Constant_3.output", "Console_3.input")
    .assertFlow(f);
  }
  
  /**
   * This test illustrates an issue with cloning the model where ports are connected
   * via multiple vertices/"diamonds".
   * <p> 
   * It uses the same model as <code>testConnectionsWithVertices()</code>, but clones it before testing the flow definition.
   * </p> 
   * It seems that the end-2-end connectivity between the ports is lost with the cloning!?
   * (case for 3, i.e. between Constant_3 and Console_3)
   */
  public void _testConnectionsWithVerticesAfterClone() throws Exception {
    Reader in = new InputStreamReader(getClass().getResourceAsStream("/testDoubleVertices.moml"));
    Flow f = FlowManager.readMoml(in);
    f = (Flow) f.clone(new Workspace());
    f.setName("afterclone");

    new FlowDefinitionAssertion()
    .expectRelation("Constant_1.output", "Console_1.input")
    .expectRelation("Constant_2.output", "Console_2.input")
    .expectRelation("Constant_3.output", "Console_3.input")
    .assertFlow(f);
  }  
 }
