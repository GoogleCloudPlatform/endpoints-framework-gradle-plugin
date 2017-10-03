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

package com.google.cloud.tools.gradle.endpoints.framework.server.task;

import com.google.api.server.spi.tools.EndpointsTool;
import com.google.api.server.spi.tools.GetClientLibAction;
import com.google.cloud.tools.gradle.endpoints.framework.server.task.scan.AnnotationServletScanner;
import com.google.common.base.Strings;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

/**
 * Endpoints task to download a client library document from the endpoints service, useful for
 * projects not in the same gradle configuration. For clients and servers that are part of the same
 * gradle project, use EndpointsClientPlugin
 */
public class GenerateClientLibsTask extends DefaultTask {
  // classesDir is only for detecting that the project has changed
  private File classesDir;

  private File clientLibDir;
  private String hostname;
  private String basePath;
  private List<String> serviceClasses;
  private File webAppDir;

  @InputDirectory
  public File getClassesDir() {
    return classesDir;
  }

  public void setClassesDir(File classesDir) {
    this.classesDir = classesDir;
  }

  @OutputDirectory
  public File getClientLibDir() {
    return clientLibDir;
  }

  public void setClientLibDir(File clientLibDir) {
    this.clientLibDir = clientLibDir;
  }

  @InputDirectory
  public File getWebAppDir() {
    return webAppDir;
  }

  public void setWebAppDir(File webAppDir) {
    this.webAppDir = webAppDir;
  }

  @Input
  public List<String> getServiceClasses() {
    return serviceClasses;
  }

  public void setServiceClasses(List<String> serviceClasses) {
    this.serviceClasses = serviceClasses;
  }

  @Input
  @Optional
  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @Input
  @Optional
  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  @TaskAction
  void generateClientLibs() throws Exception {

    // We do *not* delete the output directory for this task, and we do *not* ensure that is clean.
    // If the user specifies an output directory that is outside the gradle buildDir we don't want
    // to accidentally delete anything. Since this task is not a dependency for any other task,
    // having builds write new versions of client libraries to the output directory doesn't really
    // affect anything.

    String classpath =
        getProject()
            .getConvention()
            .getPlugin(JavaPluginConvention.class)
            .getSourceSets()
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            .getRuntimeClasspath()
            .getAsPath();

    List<String> params =
        new ArrayList<>(
            Arrays.asList(
                GetClientLibAction.NAME,
                "-o",
                clientLibDir.getPath(),
                "-cp",
                classpath,
                "-l",
                "java",
                "-bs",
                "gradle",
                "-w",
                webAppDir.getPath()));
    if (!Strings.isNullOrEmpty(hostname)) {
      params.add("-h");
      params.add(hostname);
    }
    if (!Strings.isNullOrEmpty(basePath)) {
      params.add("-p");
      params.add(basePath);
    }
    params.addAll(serviceClasses);
    Collection<String> annotatedServiceClasses =
        new AnnotationServletScanner(getProject()).findApiClassesInSourceAnnotations();
    params.addAll(annotatedServiceClasses);

    new EndpointsTool().execute(params.toArray(new String[params.size()]));
  }
}
