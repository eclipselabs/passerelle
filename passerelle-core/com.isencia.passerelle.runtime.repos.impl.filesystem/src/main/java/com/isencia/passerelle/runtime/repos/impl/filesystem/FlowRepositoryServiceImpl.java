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
package com.isencia.passerelle.runtime.repos.impl.filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.isencia.passerelle.core.ErrorCode;
import com.isencia.passerelle.model.Flow;
import com.isencia.passerelle.runtime.FlowHandle;
import com.isencia.passerelle.runtime.repository.DuplicateEntryException;
import com.isencia.passerelle.runtime.repository.EntryNotFoundException;
import com.isencia.passerelle.runtime.repository.FlowRepositoryService;
import com.isencia.passerelle.runtime.repository.ThreeDigitVersionSpecification;
import com.isencia.passerelle.runtime.repository.VersionSpecification;

/**
 * Stores flows on local disk in a configurable root folder.
 * <p>
 * Each flow is stored in a subdirectory with the flow's name. Within each flow's directory, separate subdirectories are maintained per version.
 * </p>
 * 
 * @author erwin
 */
public class FlowRepositoryServiceImpl implements FlowRepositoryService {

  private static final String VERSION_MOSTRECENT = "version.mostrecent";
  private static final String VERSION_ACTIVE = "version.active";
  private final static Logger LOGGER = LoggerFactory.getLogger(FlowRepositoryServiceImpl.class);

  private static final class DirectoryFilter implements FileFilter {
    @Override
    public boolean accept(File fileOrFolder) {
      return fileOrFolder.isDirectory();
    }
  }

  private File rootFolder;

  public FlowRepositoryServiceImpl(String rootFolderPath) {
    this(new File(rootFolderPath));
  }

  public FlowRepositoryServiceImpl(File rootFolder) {
    LOGGER.info("Creating FlowRepositoryService on folder {}", rootFolder);
    this.rootFolder = rootFolder;
    if (!rootFolder.exists()) {
      rootFolder.mkdirs();
    } else if (!rootFolder.isDirectory()) {
      throw new IllegalArgumentException(rootFolder.getPath() + " is not a folder");
    }
  }
  
  public void clearRepository() {
    try {
      FileUtils.deleteDirectory(rootFolder);
      rootFolder.mkdirs();
    } catch (IOException e) {
      LOGGER.error("Failed to clear repository directory", e);
    }
  }

  @Override
  public FlowHandle commit(Flow flow) throws DuplicateEntryException {
    return commit(flow.getName(), flow);
  }

  @Override
  public FlowHandle commit(String flowCode, Flow flow) throws DuplicateEntryException {
    File newFlowFolder = new File(rootFolder, flowCode);
    if (newFlowFolder.exists()) {
      throw new DuplicateEntryException(flowCode);
    } else {
      FlowHandle flowHandle = null;
      VersionSpecification vSpec = new ThreeDigitVersionSpecification(1, 0, 0);
      File versionFolder = new File(newFlowFolder, vSpec.toString());
      versionFolder.mkdirs();
      File destinationFile = new File(versionFolder, flow.getName() + ".moml");
      if ((!destinationFile.exists() || destinationFile.canWrite())) {
        BufferedWriter outputWriter = null;
        try {
          outputWriter = new BufferedWriter(new FileWriter(destinationFile));
          flow.exportMoML(outputWriter);
          flowHandle = new FlowHandleImpl(flowCode, destinationFile, vSpec);
          writeMetaData(flowCode, VERSION_ACTIVE, vSpec.toString());
          writeMetaData(flowCode, VERSION_MOSTRECENT, vSpec.toString());
        } catch (IOException e) {
          throw new RuntimeException(e);
        } catch (EntryNotFoundException e) {
          // should not happen
        } finally {
          if (outputWriter != null) {
            try {
              outputWriter.flush();
              outputWriter.close();
            } catch (Exception e) {
              // ignore
            }
          }
        }
        return flowHandle;
      } else {
        throw new RuntimeException(new IOException("File not writable " + destinationFile));
      }
    }
  }

  @Override
  public FlowHandle[] delete(String flowCode) throws EntryNotFoundException {
    FlowHandle[] results = getAllFlowRevisions(flowCode);
    try {
      FileUtils.deleteDirectory(new File(rootFolder, flowCode));
    } catch (IOException e) {
      LOGGER.error("Failed to delete " + flowCode, e);
    }
    return results;
  }

  @Override
  public FlowHandle update(FlowHandle handle, Flow updatedFlow, boolean activate) throws EntryNotFoundException {
    String flowCode = handle.getCode();
    File flowRootFolder = new File(rootFolder, flowCode);
    if (!flowRootFolder.isDirectory()) {
      throw new EntryNotFoundException(ErrorCode.FLOW_SAVING_ERROR_FUNC, "Flow code unknown " + flowCode, null);
    } else {
      FlowHandle flowHandle = null;
      ThreeDigitVersionSpecification vSpec = ((ThreeDigitVersionSpecification)handle.getVersion()).increaseMinor();
      File versionFolder = new File(flowRootFolder, vSpec.toString());
      while(versionFolder.exists()) {
        vSpec = vSpec.increaseMinor();
        versionFolder = new File(flowRootFolder, vSpec.toString());
      }
      versionFolder.mkdirs();
      File destinationFile = new File(versionFolder, updatedFlow.getName() + ".moml");
      if ((!destinationFile.exists() || destinationFile.canWrite())) {
        BufferedWriter outputWriter = null;
        try {
          outputWriter = new BufferedWriter(new FileWriter(destinationFile));
          updatedFlow.exportMoML(outputWriter);
          flowHandle = new FlowHandleImpl(flowCode, destinationFile, vSpec);
          writeMetaData(flowCode, VERSION_MOSTRECENT, vSpec.toString());
          if(activate) {
            activateFlowRevision(flowHandle);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        } finally {
          if (outputWriter != null) {
            try {
              outputWriter.flush();
              outputWriter.close();
            } catch (Exception e) {
              // ignore
            }
          }
        }
        return flowHandle;
      } else {
        throw new RuntimeException(new IOException("File not writable " + destinationFile));
      }
    }
  }

  @Override
  public FlowHandle getActiveFlow(String flowCode) throws EntryNotFoundException {
    File flowRootFolder = new File(rootFolder, flowCode);
    if (!flowRootFolder.isDirectory()) {
      throw new EntryNotFoundException("Invalid flow code " + flowCode);
    } else {
      FlowHandle flow = null;
      Properties metaData = readMetaData(flowCode);
      String activeVersion = metaData.getProperty(VERSION_ACTIVE);
      flow = readAndBuildFlowHandle(flowRootFolder.getName(), activeVersion);
      return flow;
    }
  }

  @Override
  public FlowHandle getMostRecentFlow(String flowCode) throws EntryNotFoundException {
    File flowRootFolder = new File(rootFolder, flowCode);
    if (!flowRootFolder.isDirectory()) {
      throw new EntryNotFoundException("Invalid flow code " + flowCode);
    } else {
      FlowHandle flow = null;
      Properties metaData = readMetaData(flowCode);
      String mostRecentVersion = metaData.getProperty(VERSION_MOSTRECENT);
      flow = readAndBuildFlowHandle(flowRootFolder.getName(), new File(flowRootFolder, mostRecentVersion));
      return flow;
    }
  }
  
  @Override
  public FlowHandle getFlowVersion(String flowCode, VersionSpecification version) throws EntryNotFoundException {
    File flowRootFolder = new File(rootFolder, flowCode);
    if (!flowRootFolder.isDirectory()) {
      throw new EntryNotFoundException("Invalid flow code " + flowCode);
    } else {
      FlowHandle flow = null;
      String requestedVersion = version.toString();
      flow = readAndBuildFlowHandle(flowRootFolder.getName(), new File(flowRootFolder, requestedVersion));
      if(flow==null) {
        throw new EntryNotFoundException("Version " + requestedVersion + " not found for flow code " + flowCode);
      } else {
        return flow;
      }
    }
  }
  
  @Override
  public FlowHandle loadFlowHandleWithContent(FlowHandle handle) throws EntryNotFoundException {
    return getFlowVersion(handle.getCode(), handle.getVersion());
  }

  @Override
  public String[] getAllFlowCodes() {
    File[] subFolders = rootFolder.listFiles(new DirectoryFilter());
    String[] flowCodes = new String[subFolders.length];
    for (int i = 0; i < subFolders.length; ++i) {
      flowCodes[i] = subFolders[i].getName();
    }
    return flowCodes;
  }

  @Override
  public FlowHandle[] getAllFlowRevisions(String flowCode) throws EntryNotFoundException {
    File codeFolder = new File(rootFolder, flowCode);
    if (!codeFolder.isDirectory()) {
      throw new EntryNotFoundException("Invalid flow code " + flowCode);
    } else {
      ArrayList<FlowHandle> results = new ArrayList<FlowHandle>();
      File[] versionFolders = codeFolder.listFiles(new DirectoryFilter());
      for (File versionFolder : versionFolders) {
        FlowHandle fh = readAndBuildFlowHandle(flowCode, versionFolder);
        if (fh != null) {
          results.add(fh);
        }
      }
      return results.toArray(new FlowHandle[results.size()]);
    }
  }

  @Override
  public FlowHandle activateFlowRevision(FlowHandle handle) throws EntryNotFoundException {
    try {
      return writeMetaData(handle.getCode(), VERSION_ACTIVE, handle.getVersion().toString());
    } catch (IOException e) {
      throw new RuntimeException("Error writing activation data", e);
    }
  }

  private FlowHandle readAndBuildFlowHandle(String code, String version) {
    return readAndBuildFlowHandle(code, new File(new File(rootFolder, code), version));
  }

  private FlowHandle readAndBuildFlowHandle(String code, File versionFolder) {
    if (!versionFolder.isDirectory()) {
      return null;
    } else {
      try {
        VersionSpecification vSpec = VersionSpecification.parse(versionFolder.getName());
        File[] modelFiles = versionFolder.listFiles(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("moml") || name.endsWith("xml");
          }
        });
        File modelFile = modelFiles[0];
        return new FlowHandleImpl(code, modelFile, vSpec);
      } catch (Exception e) {
        return null;
      }
    }
  }

  private FlowHandle writeMetaData(String flowCode, String dataItemName, String dataItemValue) throws IOException, EntryNotFoundException {
    FlowHandle previouslyActive = null;
    Properties flowMetaDataProps = readMetaData(flowCode);
    String activeVersion = flowMetaDataProps.getProperty(dataItemName);
    if (activeVersion != null) {
      previouslyActive = readAndBuildFlowHandle(flowCode, activeVersion);
    }
    flowMetaDataProps.setProperty(dataItemName, dataItemValue);
    writeMetaData(flowCode, flowMetaDataProps);
    return previouslyActive;
  }

  /**
   * @param flowCode
   * @param flowMetaDataProps
   * @throws IOException
   */
  private void writeMetaData(String flowCode, Properties flowMetaDataProps) throws IOException {
    File flowRootFolder = new File(rootFolder, flowCode);
    File metaDataFile2 = new File(flowRootFolder, ".metadata");
    Writer metaDataWriter = new FileWriter(metaDataFile2);
    try {
      flowMetaDataProps.store(metaDataWriter, flowCode);
    } finally {
      metaDataWriter.close();
    }
  }

  /**
   * @param flowCode
   * @return metadata props for the given flow; returns empty properties when no metadata is found
   * @throws EntryNotFoundException
   */
  private Properties readMetaData(String flowCode) throws EntryNotFoundException {
    Properties flowMetaDataProps = new Properties();
    File flowRootFolder = new File(rootFolder, flowCode);
    File metaDataFile = new File(flowRootFolder, ".metadata");
    if (!flowRootFolder.isDirectory()) {
      throw new EntryNotFoundException("Flow not managed by this repository " + flowCode);
    } else {
      if (metaDataFile.exists()) {
        Reader metaDataReader = null;
        try {
          metaDataReader = new FileReader(metaDataFile);
          flowMetaDataProps.load(metaDataReader);
        } catch (Exception e) {

        } finally {
          if (metaDataReader != null) {
            try {
              metaDataReader.close();
            } catch (Exception e) {
            }
          }
        }
      }
      return flowMetaDataProps;
    }
  }
}
