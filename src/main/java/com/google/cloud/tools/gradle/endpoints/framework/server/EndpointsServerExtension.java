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

package com.google.cloud.tools.gradle.endpoints.framework.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Project;

/** Plugin extension for endpoints server plugin. */
public class EndpointsServerExtension {

  private final Project project;
  private final File discoveryDocDir;

  private File clientLibDir;
  private List<String> serviceClasses;
  private String hostname;

  /** Constructor. */
  public EndpointsServerExtension(Project project) {
    this.project = project;
    discoveryDocDir = new File(project.getBuildDir(), "endpointsDiscoveryDocs");
    clientLibDir = new File(project.getBuildDir(), "endpointsClientLibs");
    serviceClasses = new ArrayList<>();
  }

  public File getDiscoveryDocDir() {
    return discoveryDocDir;
  }

  public File getClientLibDir() {
    return clientLibDir;
  }

  public void setClientLibDir(Object clientLibDir) {
    this.clientLibDir = project.file(clientLibDir);
  }

  public List<String> getServiceClasses() {
    return serviceClasses;
  }

  public void setServiceClasses(List<String> serviceClasses) {
    this.serviceClasses = serviceClasses;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
}
