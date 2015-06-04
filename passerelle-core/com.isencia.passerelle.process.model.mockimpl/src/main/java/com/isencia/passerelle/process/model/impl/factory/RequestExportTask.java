package com.isencia.passerelle.process.model.impl.factory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.io.FileUtils;
import com.isencia.passerelle.process.model.Attribute;
import com.isencia.passerelle.process.model.Context;
import com.isencia.passerelle.process.model.ContextEvent;
import com.isencia.passerelle.process.model.Request;
import com.isencia.passerelle.process.model.ResultBlock;
import com.isencia.passerelle.process.model.ResultItem;

public class RequestExportTask {

  public static final String NEWLINE = System.getProperty("line.separator");
  public static final String TIMEZONE = "Europe/Brussels";
  public static final String DATEFORMAT = "dd/MM/yyyy HH:mm:ss";
  public static final String CREATION_DATE_ATTRIBUTE = "request.creationTS";

  private File rootFolder;

  public RequestExportTask(File rootFolder) {
    if (!rootFolder.isDirectory()) {
      throw new IllegalArgumentException(rootFolder + "must be a directory");
    }
    this.rootFolder = rootFolder;
  }

  public void execute(Request request) throws IOException {
    if (!rootFolder.exists()) {
      if (!rootFolder.mkdir()) {
        throw new IOException("Unable to create " + rootFolder);
      }
    }

    String requestFolderName = "ref_" + request.getCase().getId() + "_req_" + request.getId() + "__" + request.getType();
    File requestFolder = new File(rootFolder, requestFolderName);
    if (requestFolder.exists()) {
      // maintain one backup of previous request dump
      File requestBuFolder = new File(rootFolder, requestFolderName + "____bu");
      if (requestBuFolder.exists()) {
        FileUtils.deleteDirectory(requestBuFolder);
      }
      FileUtils.moveDirectory(requestFolder, requestBuFolder);
    }

    saveRequestAndContext(requestFolder, request);
    
    for (com.isencia.passerelle.process.model.Task task : request.getProcessingContext().getTasks()) {
      String taskFolderName = "task_" + task.getId() + "__" + task.getType();
      File taskFolder = new File(requestFolder, taskFolderName);
      saveRequestAndContext(taskFolder, task);
      
      Collection<ResultBlock> resultBlocks = task.getResultBlocks();
      for (ResultBlock block : resultBlocks) {
        String blockFileName = "block_" + block.getId() + "__" + block.getType();
        File blockFile = new File(taskFolder, blockFileName + ".properties");
        FileUtils.writeStringToFile(blockFile,  buildResultBlockDump(block));
      }
    }
  }

  protected void saveRequestAndContext(File rootFolder, Request request) throws IOException {
    if (!rootFolder.mkdir()) {
      throw new IOException("Unable to create " + rootFolder);
    }
    File requestFile = new File(rootFolder, request.getType() + ".properties");
    FileUtils.writeStringToFile(requestFile,  buildRequestWithAttributesDump(request));
    File contextFile = new File(rootFolder, request.getType() + "__context.properties");
    FileUtils.writeStringToFile(contextFile,  buildContextWithEventsDump(request.getProcessingContext()));
  }

  public static String generateDate(Date date) {
    if (date == null) {
      return null;
    }
    try {
      TimeZone time = TimeZone.getTimeZone(TIMEZONE);
      SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT);
      formatter.setTimeZone(time);
      return formatter.format(date);
    } catch (Exception e) {
      return null;
    }
  }

  protected String buildContextWithEventsDump(Context context) {
    StringBuilder builder = new StringBuilder();
    builder.append("#id=" + context.getId() + NEWLINE);
    builder.append("#status=" + context.getStatus() + NEWLINE);
    builder.append("#creationTS=" + generateDate(context.getCreationTS()) + NEWLINE);
    builder.append("#EVENTS:" + NEWLINE);
    List<ContextEvent> ctxtEvents = context.getEvents();
    for (ContextEvent ctxtEvent : ctxtEvents) {
      builder.append("\n" + generateDate(context.getCreationTS()) + "|");
      builder.append(ctxtEvent.getId() + "|");
      builder.append(ctxtEvent.getTopic() + "|");
      builder.append(ctxtEvent.getMessage());
    }
    return builder.toString();
  }

  protected String buildRequestWithAttributesDump(Request request) {
    StringBuilder builder = new StringBuilder();
    builder.append("#id=" + request.getId() + NEWLINE);
    builder.append("#type=" + request.getType() + NEWLINE);
    builder.append("#initiator=" + request.getInitiator() + NEWLINE);
    builder.append("#executor=" + request.getExecutor() + NEWLINE);
    builder.append("#creationTS=" + generateDate(request.getProcessingContext().getCreationTS()) + NEWLINE);
    builder.append("#ATTRIBUTES:" + NEWLINE);
    for (Attribute attribute : request.getAttributes()) {
      builder.append(attribute.getName() + "=");
      builder.append(attribute.getValue() == null ? "" : attribute.getValue());
      builder.append(NEWLINE);
    }
    return builder.toString();
  }

  protected String buildResultBlockDump(ResultBlock currentBlock) {
    StringBuilder builder = new StringBuilder();
    builder.append("#id=" + currentBlock.getId() + NEWLINE);
    builder.append("#type=" + currentBlock.getType() + NEWLINE);
    builder.append("#creationTS=" + generateDate(currentBlock.getCreationTS()) + NEWLINE);
    builder.append("#task.id=" + currentBlock.getTask().getId() + NEWLINE);
    builder.append("#task.type=" + currentBlock.getTask().getType() + NEWLINE);
    if (currentBlock.getColour() != null) {
      builder.append("#analysis=" + currentBlock.getColour() + NEWLINE);
    }
    builder.append("#ITEMS:" + NEWLINE);
    List<ResultItem<?>> list = new ArrayList<ResultItem<?>>();
    list.addAll(currentBlock.getAllItems());
    Comparator<ResultItem<?>> comparator = new Comparator<ResultItem<?>>() {
      public int compare(ResultItem<?> arg0, ResultItem<?> arg1) {
        if (arg0.getName() == null) {
          return 0;
        }
        return arg0.getName().compareTo(arg1.getName());
      }
    };
    Collections.sort(list, comparator);
    for (ResultItem<?> item : list) {
      builder.append(item.getName().replace(" ", "\\ ").replace("=", "\\=") + "=" + getStringValue(item) + NEWLINE);
      if (item.getColour() != null) {
        builder.append(item.getName().replace(" ", "\\ ").replace("=", "\\=") + "|analysis=" + item.getColour() + NEWLINE);
      }
    }
    return builder.toString();
  }

  protected File writeFile(File file, String contents) throws IOException {
    FileWriter writer = null;
    try {
      writer = new FileWriter(file);
      writer.write(contents);
    } finally {
      if (writer != null)
        try {
          writer.close();
        } catch (IOException ioe) {
        }
    }
    return file;
  }

  protected String getItemValue(ResultItem<?> item) {
    if (item == null)
      return null;
    return item.getValueAsString();
  }

  protected String getStringValue(ResultItem<?> item) {
    if (item == null || item.getValueAsString() == null || item.getValueAsString().trim().length() == 0)
      return "";
    return item.getValueAsString();
  }
}
