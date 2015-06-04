package com.isencia.passerelle.workbench.model.editor.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.isencia.passerelle.editor.common.model.IMomlClassService;
import com.isencia.passerelle.project.repository.api.RepositoryService;

public class MomlClassService implements IMomlClassService {

  public MomlClassService() {

  }

  public List<String> getAllActorClasses() throws Exception{
    RepositoryService repoService = Activator.getDefault().getRepositoryService();
    if (repoService == null) {
      throw new Exception("Not project service defined");
    }
    return Arrays.asList(repoService.getAllSubmodels());
  }

}
