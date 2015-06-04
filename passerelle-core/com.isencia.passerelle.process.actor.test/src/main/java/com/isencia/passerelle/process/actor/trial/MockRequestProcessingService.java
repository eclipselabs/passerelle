package com.isencia.passerelle.process.actor.trial;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.Task;
import com.isencia.passerelle.process.model.factory.ProcessFactory;
import com.isencia.passerelle.process.model.factory.ProcessFactoryTracker;
import com.isencia.passerelle.process.service.ProcessCallback;
import com.isencia.passerelle.process.service.ProcessManager;
import com.isencia.passerelle.process.service.ProcessManagerServiceTracker;
import com.isencia.passerelle.process.service.RequestProcessingService;
import com.isencia.util.FutureValue;

public class MockRequestProcessingService implements RequestProcessingService {

  private String serviceType;
  private Map<String, String> resultItems;

  public MockRequestProcessingService(String serviceType, Map<String, String> resultItems) {
    this.serviceType = serviceType;
    this.resultItems = resultItems;
  }
  
  @Override
  public boolean canProcess(Request request) {
    return serviceType==null || serviceType.equalsIgnoreCase(request.getType());
  }

  @Override
  public Future<Task> process(Request request, Long timeout, TimeUnit unit, ProcessCallback... callbacks) {
    if (serviceType==null || !serviceType.equalsIgnoreCase(request.getType())) {
      return null;
    } else {
      ProcessManager processManager = ProcessManagerServiceTracker.getService().getProcessManager(request);
      Task task = (Task) request;
      try {
        ProcessFactory entityFactory = ProcessFactoryTracker.getService();
        ResultBlock rb = entityFactory.createResultBlock(task, serviceType);
        for (Entry<String, String> item : resultItems.entrySet()) {
          entityFactory.createResultItem(rb, item.getKey(), item.getValue(), null);
        }
      } catch (Exception e) {
        processManager.notifyError(task, e);
      }
      processManager.notifyFinished(task);
      return new FutureValue<Task>(task);
    }
  }

  @Override
  public String getName() {
    return serviceType;
  }
}
