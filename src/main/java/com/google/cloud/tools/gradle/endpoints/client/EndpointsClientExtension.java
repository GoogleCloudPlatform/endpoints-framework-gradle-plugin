/*
 * Copyright (c) 2016 Google Inc. All Right Reserved.
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
 *
 */

package com.google.cloud.tools.gradle.endpoints.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin extension for endpoints client plugin
 */
public class EndpointsClientExtension {

  private final File genSrcDir;
  private final File clientLibDir;
  private final File genDiscoveryDocsDir;
  private List<File> discoveryDocs;

  public EndpointsClientExtension(File buildDir) {
    genSrcDir = new File(buildDir, "endpointsGenSrc");
    clientLibDir = new File(buildDir, "endpointsClientLibs");
    genDiscoveryDocsDir = new File(buildDir, "endpointsDiscoveryDocsFromDependencies");

    discoveryDocs = new ArrayList<>();
  }

  public File getGenSrcDir() {
    return genSrcDir;
  }

  public File getClientLibDir() {
    return clientLibDir;
  }

  public File getGenDiscoveryDocsDir() {
    return genDiscoveryDocsDir;
  }

  public List<File> getDiscoveryDocs() {
    return discoveryDocs;
  }

  public void setDiscoveryDocs(List<File> discoveryDocs) {
    this.discoveryDocs = discoveryDocs;
  }
}
