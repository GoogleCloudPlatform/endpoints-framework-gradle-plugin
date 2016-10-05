/*
 *  Copyright (c) 2016 Google Inc. All Right Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.tools.gradle.endpoints.client.task;

import com.google.api.server.spi.tools.EndpointsTool;
import com.google.api.server.spi.tools.GenClientLibAction;
import com.google.cloud.tools.gradle.endpoints.client.DiscoveryDocUtil;
import com.google.common.collect.Lists;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Endpoints task to download a discovery document from the endpoints service
 */
public class GenerateClientLibrariesTask extends DefaultTask {
  private File clientLibraryDir;
  private List<File> discoveryDocs;
  private File linkedDiscoveryDocs;

  @OutputDirectory
  public File getClientLibraryDir() {
    return clientLibraryDir;
  }

  public void setClientLibraryDir(File clientLibraryDir) {
    this.clientLibraryDir = clientLibraryDir;
  }

  @InputFiles
  public List<File> getDiscoveryDocs() {
    return discoveryDocs;
  }

  public void setDiscoveryDocs(List<File> discoveryDocs) {
    this.discoveryDocs = discoveryDocs;
  }

  @InputDirectory
  public File getLinkedDiscoveryDocs() {
    return linkedDiscoveryDocs;
  }

  public void setGeneratedDiscoveryDocs(File linkedDiscoveryDocs) {
    this.linkedDiscoveryDocs = linkedDiscoveryDocs;
  }

  @TaskAction
  public void generateClientLibs() throws Exception {
    getProject().delete(clientLibraryDir);
    clientLibraryDir.mkdirs();

    for (File discoveryDoc : discoveryDocs) {
      runEndpointsTools(discoveryDoc);
    }

    for (File discoveryDoc : DiscoveryDocUtil.findDiscoveryDocsInDirectory(linkedDiscoveryDocs)) {
      runEndpointsTools(discoveryDoc);
    }

  }

  private void runEndpointsTools(File discoveryDoc) throws Exception {
    List<String> params = Lists.newArrayList(Arrays.asList(
        GenClientLibAction.NAME,
        "-l", "java",
        "-bs", "maven",
        "-o", clientLibraryDir.getAbsolutePath()));

    params.add(discoveryDoc.getAbsolutePath());
    new EndpointsTool().execute(params.toArray(new String[params.size()]));
  }
}