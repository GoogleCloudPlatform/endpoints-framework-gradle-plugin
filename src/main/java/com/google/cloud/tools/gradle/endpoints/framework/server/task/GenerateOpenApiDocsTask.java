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
import com.google.api.server.spi.tools.GetOpenApiDocAction;
import com.google.cloud.tools.gradle.endpoints.framework.server.task.scan.AnnotationServletScanner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

/** Endpoints task to download a openapi document from the endpoints service. */
public class GenerateOpenApiDocsTask extends DefaultTask {

  // classesDir is only for detecting that the project has changed
  private File classesDir;

  private File openApiDocDir;
  private File webAppDir;
  private List<String> serviceClasses;
  private String hostname;
  private String basePath;

  @InputDirectory
  public File getClassesDir() {
    return classesDir;
  }

  public void setClassesDir(File classesDir) {
    this.classesDir = classesDir;
  }

  @OutputDirectory
  public File getOpenApiDocDir() {
    return openApiDocDir;
  }

  public void setOpenApiDocDir(File openApiDocDir) {
    this.openApiDocDir = openApiDocDir;
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

  /** Task entry point. */
  @TaskAction
  void generateOpenApiDocs() throws Exception {
    getProject().delete(openApiDocDir);
    getProject().mkdir(openApiDocDir);

    String classpath =
        getProject()
            .getConvention()
            .getPlugin(JavaPluginConvention.class)
            .getSourceSets()
            .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            .getRuntimeClasspath()
            .getAsPath();

    ImmutableList.Builder<String> paramsBuilder =
        ImmutableList.<String>builder()
            .add(GetOpenApiDocAction.NAME)
            .add("-o", computeOpenApiDocPath())
            .add("-cp", classpath)
            .add("-w", webAppDir.getPath());
    if (!Strings.isNullOrEmpty(hostname)) {
      paramsBuilder.add("-h", hostname);
    }
    if (!Strings.isNullOrEmpty(basePath)) {
      paramsBuilder.add("-p", basePath);
    }
    Collection<String> allServiceClasses = new HashSet<>();
    allServiceClasses.addAll(getServiceClasses());
    Collection<String> apiClassesInSourceAnnotations =
        new AnnotationServletScanner(getProject()).findApiClassesInSourceAnnotations();
    allServiceClasses.addAll(apiClassesInSourceAnnotations);
    paramsBuilder.addAll(allServiceClasses);
    String[] execParams = paramsBuilder.build().toArray(new String[] {});
    new EndpointsTool().execute(execParams);
  }

  private String computeOpenApiDocPath() {
    return new File(openApiDocDir, "openapi.json").getPath();
  }
}
