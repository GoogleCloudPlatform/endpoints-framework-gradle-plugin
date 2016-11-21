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

package com.google.cloud.tools.gradle.endpoints.server;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugin extension for endpoints server plugin
 */
public class EndpointsServerExtension {

  private final File discoveryDocDir;
  private final File clientLibDir;
  private List<String> serviceClasses;
  private String format;

  public EndpointsServerExtension(Project project) {
    discoveryDocDir = new File(project.getBuildDir(), "endpointsDiscoveryDocs");
    clientLibDir = new File(project.getBuildDir(), "endpointsClientLibs");
    format = "rest";
    serviceClasses = new ArrayList<>();
  }

  public File getDiscoveryDocDir() {
    return discoveryDocDir;
  }

  public File getClientLibDir() {
    return clientLibDir;
  }

  public List<String> getServiceClasses() {
    return serviceClasses;
  }

  public void setServiceClasses(List<String> serviceClasses) {
    this.serviceClasses = serviceClasses;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }
}
