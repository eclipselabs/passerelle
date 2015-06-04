package com.isencia.passerelle.process.actor.trial;

import java.io.BufferedReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.isencia.passerelle.actor.ProcessingException;
import com.isencia.passerelle.process.actor.ProcessResponse;
import com.isencia.passerelle.process.actor.TaskBasedActor;
import com.isencia.passerelle.process.common.exception.ErrorCode;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.persist.ProcessPersister;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.util.ExecutionTracerService;

public class MockTaskActor extends TaskBasedActor {
  private static final long serialVersionUID = -1137521709390691137L;

  private static final Logger LOGGER = LoggerFactory.getLogger(MockTaskActor.class);

  public StringParameter resultItemsParameter; // NOSONAR

  public MockTaskActor(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
    super(container, name);
    resultItemsParameter = new StringParameter(this, "Result items");
    new TextStyle(resultItemsParameter, "paramsTextArea");
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void process(Task task, ProcessManager processManager, ProcessResponse processResponse) throws ProcessingException {
    SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    try {
      ProcessFactory entityFactory = processManager.getFactory();
      ResultBlock rb = entityFactory.createResultBlock(task, resultTypeParam.stringValue());
      String paramDefs = resultItemsParameter.stringValue();
      BufferedReader reader = new BufferedReader(new StringReader(paramDefs));
      String paramDef = null;
      while ((paramDef = reader.readLine()) != null) {
        String[] paramKeyValue = paramDef.split("=");
        if (paramKeyValue.length == 2) {
          String name = paramKeyValue[0];
          String[] valueParts = paramKeyValue[1].split(";");
          String value = valueParts[0];
          Date creationTS = rb.getCreationTS();
          if (valueParts.length > 1) {
            creationTS = fmt.parse(valueParts[1]);
          }
          entityFactory.createResultItem(rb, name, value, null, creationTS);
        } else {
          ExecutionTracerService.trace(this, "Invalid mapping definition: " + paramDef);
        }
      }
      ProcessPersister procPersister = processManager.getPersister();
      boolean shouldClose = false;
      try {
        shouldClose = procPersister.open(true);
        procPersister.persistResultBlocks(rb);
        processManager.notifyFinished(task);
      } finally {
        if (shouldClose) {
          procPersister.close();
        }
      }
    } catch (Exception e) {
      throw new ProcessingException(ErrorCode.TASK_ERROR, "Error mocking task", this, e);
    }
  }
}
