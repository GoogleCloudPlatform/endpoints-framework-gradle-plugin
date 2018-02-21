/*
 *  Copyright (c) 2017 Google Inc. All Right Reserved.
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
import com.google.common.base.Strings;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

public class EndpointsArtifactTask extends DefaultTask {
  // classesDir is only for detecting that the project has changed
  private FileCollection classesDirs;

  // internal parameters for task configuration
  private String command;
  private boolean cleanBeforeRun;
  private String outputLanguage;
  private String outputBuildSystem;
  private String outputFileName;

  // user facing options
  private File outputDirectory;
  private String hostname;
  private String basePath;
  private List<String> serviceClasses;
  private File webAppDir;

  @InputFiles
  public FileCollection getClassesDirs() {
    return classesDirs;
  }

  public void setClassesDirs(FileCollection classesDirs) {
    this.classesDirs = classesDirs;
  }

  @Internal
  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  @OutputDirectory
  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
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

  @Optional
  @Input
  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @Optional
  @Input
  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  @Internal
  public boolean isCleanBeforeRun() {
    return cleanBeforeRun;
  }

  public void setCleanBeforeRun(boolean cleanBeforeRun) {
    this.cleanBeforeRun = cleanBeforeRun;
  }

  @Internal
  public String getOutputLanguage() {
    return outputLanguage;
  }

  public void setOutputLanguage(String outputLanguage) {
    this.outputLanguage = outputLanguage;
  }

  @Internal
  public String getOutputBuildSystem() {
    return outputBuildSystem;
  }

  public void setOutputBuildSystem(String outputBuildSystem) {
    this.outputBuildSystem = outputBuildSystem;
  }

  @Internal
  public String getOutputFileName() {
    return outputFileName;
  }

  public void setOutputFileName(String outputFileName) {
    this.outputFileName = outputFileName;
  }

  /** Task entry point. */
  @TaskAction
  void generateEndpointsArtifact() throws Exception {

    if (cleanBeforeRun) {
      getProject().delete(outputDirectory);
      getProject().mkdir(outputDirectory);
    }

    List<String> params = new ArrayList<>();

    params.add(command);

    params.add("-o");
    if (!Strings.isNullOrEmpty(outputFileName)) {
      params.add(new File(outputDirectory, outputFileName).getAbsolutePath());
    } else {
      params.add(outputDirectory.getAbsolutePath());
    }

    String classpath =
        getProject()
            .getConvention()
            .getPlugin(JavaPluginConvention.class)
            .getSourceSets()
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            .getRuntimeClasspath()
            .getAsPath();

    params.add("-cp");
    params.add(classpath);

    params.add("-w");
    params.add(webAppDir.getAbsolutePath());

    if (!Strings.isNullOrEmpty(outputBuildSystem)) {
      params.add("-bs");
      params.add(outputBuildSystem);
    }
    if (!Strings.isNullOrEmpty(outputLanguage)) {
      params.add("-l");
      params.add(outputLanguage);
    }
    if (!Strings.isNullOrEmpty(hostname)) {
      params.add("-h");
      params.add(hostname);
    }
    if (!Strings.isNullOrEmpty(basePath)) {
      params.add("-p");
      params.add(basePath);
    }
    params.addAll(serviceClasses);

    new EndpointsTool().execute(params.toArray(new String[params.size()]));
  }
}
