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
package com.isencia.passerelle.workbench.model.editor.graphiti.model;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import com.isencia.passerelle.model.Flow;

public class DiagramFlowRepository {
  
  private final static Map<Diagram, Flow> diagramFlowMap = new HashMap<Diagram, Flow>();
  private final static Map<Flow, Diagram> flowDiagramMap = new HashMap<Flow,Diagram>();
  
  public static void registerDiagramAndFlow(Diagram d, Flow f) {
    diagramFlowMap.put(d, f);
    flowDiagramMap.put(f,d);
  }
  
  public static void unregisterDiagramAndFlow(Diagram d, Flow f) {
    diagramFlowMap.remove(d);
    flowDiagramMap.remove(f);
  }
  
  public static Flow getFlowForDiagram(Diagram d) {
    return diagramFlowMap.get(d);
  }
  
  public static Diagram getDiagramForFlow(Flow f) {
    return flowDiagramMap.get(f);
  }

}
