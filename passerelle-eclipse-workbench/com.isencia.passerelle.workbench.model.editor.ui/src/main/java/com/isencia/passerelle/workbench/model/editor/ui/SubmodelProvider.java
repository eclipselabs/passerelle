package com.isencia.passerelle.workbench.model.editor.ui;

import org.slf4j.MDC;

import ptolemy.kernel.CompositeEntity;

import com.isencia.passerelle.ext.ActorOrientedClassProvider;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.project.repository.api.Project;
import com.isencia.passerelle.project.repository.api.RepositoryService;
import com.isencia.passerelle.validation.version.VersionSpecification;

public class SubmodelProvider implements ActorOrientedClassProvider {
  private static final String PROJECT_CODE = "projectCode";

  public CompositeEntity getActorOrientedClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException {
    RepositoryService repoSvc = Activator.getDefault().getRepositoryService();
    String projectCode = MDC.get(PROJECT_CODE);
    if (projectCode != null) {
      Project project = repoSvc.getProject(projectCode);
      if (project != null) {
        Flow flow = project.getFlow(className);
        if (flow.isClassDefinition()) {
          return flow;
        }
      }
    }

    try {
      return repoSvc.getSubmodel(className);
    } catch (Exception e) {

    }
    return null;
  }

}
