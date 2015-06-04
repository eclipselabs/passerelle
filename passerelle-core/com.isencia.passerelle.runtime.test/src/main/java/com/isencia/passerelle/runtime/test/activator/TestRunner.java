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
package com.isencia.passerelle.runtime.test.activator;

import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import com.isencia.passerelle.runtime.process.FlowProcessingService;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.test.FlowProcessingTest1;
import com.isencia.passerelle.runtime.test.FlowRepositoryTest1;

public class TestRunner implements CommandProvider {

  public String getHelp() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\n---Passerelle flow repos tests---\n");
    buffer.append("\trunFlowReposTests\n");
    buffer.append("\trunFlowProcTests\n");
    return buffer.toString();
  }

  public void _runFlowReposTests(CommandInterpreter ci) {
    List<FlowRepositoryService> reposSvcs = Activator.getInstance().getFlowReposSvc();
    
    for (FlowRepositoryService flowRepositoryService : reposSvcs) {
      FlowRepositoryTest1.repositoryService = flowRepositoryService;
      junit.textui.TestRunner.run(FlowRepositoryTest1.class);
    }
  }
  public void _runFlowProcTests(CommandInterpreter ci) {
    FlowProcessingTest1.repositoryService = Activator.getInstance().getLocalReposSvc();
    List<FlowProcessingService> procSvcs = Activator.getInstance().getFlowProcSvc();
    for (FlowProcessingService flowProcService : procSvcs) {
      FlowProcessingTest1.processingService = flowProcService;
      junit.textui.TestRunner.run(FlowProcessingTest1.class);
    }
  }
}
