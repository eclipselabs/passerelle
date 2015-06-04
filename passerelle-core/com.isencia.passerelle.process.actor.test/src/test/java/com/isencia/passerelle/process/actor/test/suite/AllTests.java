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
package com.isencia.passerelle.process.actor.test.suite;

import junit.framework.Test;
import junit.framework.TestSuite;
import com.isencia.passerelle.process.actor.test.ProcessActorTest;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(ProcessActorTest.class);
    suite.addTestSuite(com.isencia.passerelle.process.actor.v5.test.ProcessActorTest.class);
    //$JUnit-END$
    return suite;
  }

}
