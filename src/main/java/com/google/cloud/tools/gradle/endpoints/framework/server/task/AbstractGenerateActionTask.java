package com.google.cloud.tools.gradle.endpoints.framework.server.task;

import com.google.api.server.spi.tools.EndpointsTool;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;

abstract class AbstractGenerateActionTask extends DefaultTask {

  // classesDir is only for detecting that the project has changed
  private File classesDir;
  private File webAppDir;
  private List<String> serviceClasses;
  private String hostname;
  private String basePath;

  private final String actionName;
  private final boolean shouldRecreateOutputDir;
  private final String[] extraCliParameters;

  AbstractGenerateActionTask(String actionName) {
    this.actionName = actionName;
    shouldRecreateOutputDir = true;
    extraCliParameters = new String[] {};
  }

  AbstractGenerateActionTask(
      String actionName, boolean shouldRecreateOutputDir, String... extraCliParameters) {
    this.actionName = actionName;
    this.shouldRecreateOutputDir = shouldRecreateOutputDir;
    this.extraCliParameters = extraCliParameters;
  }

  @InputDirectory
  public File getClassesDir() {
    return classesDir;
  }

  public void setClassesDir(File classesDir) {
    this.classesDir = classesDir;
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
  public void generateDocs() throws Exception {
    if (shouldRecreateOutputDir) {
      recreateOutputDir();
    }
    ImmutableList.Builder<String> paramsBuilder =
        ImmutableList.<String>builder()
            .add(actionName)
            .add("-o", getOutputDocLocation().getPath())
            .add("-cp", getMainSourcesClasspath())
            .add("-w", webAppDir.getPath())
            .add(extraCliParameters);
    if (!Strings.isNullOrEmpty(hostname)) {
      paramsBuilder.add("-h", hostname);
    }
    if (!Strings.isNullOrEmpty(basePath)) {
      paramsBuilder.add("-p", basePath);
    }
    String[] execParameters = paramsBuilder.build().toArray(new String[] {});
    new EndpointsTool().execute(execParameters);
  }

  private String getMainSourcesClasspath() {
    return getProject()
        .getConvention()
        .getPlugin(JavaPluginConvention.class)
        .getSourceSets()
        .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        .getRuntimeClasspath()
        .getAsPath();
  }

  /** If output location is a file, use it's parent dir as a target to recreate. */
  private void recreateOutputDir() {
    File outputDir = getOutputDocLocation();
    if (!outputDir.isDirectory()) {
      outputDir = outputDir.getParentFile();
    }
    getProject().delete(outputDir);
    getProject().mkdir(outputDir);
  }

  abstract File getOutputDocLocation();
}
