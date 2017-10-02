package com.google.cloud.tools.gradle.endpoints.framework.server.task;

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
import org.gradle.api.tasks.SourceSet;

public abstract class AbstractEndpointsTask extends DefaultTask {

  // classesDir is only for detecting that the project has changed
  private File classesDir;
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

  String[] initDirsAndGetExecParams(boolean cleanOuptutDir, String... additionalExecParams) {
    if (cleanOuptutDir) {
      recreateOutputDir();
    }

    ImmutableList.Builder<String> paramsBuilder =
        ImmutableList.<String>builder()
            .add(actionName())
            .add("-o", getOutputDir().getPath())
            .add("-cp", getMainSourcesClasspath())
            .add(additionalExecParams)
            .add("-w", webAppDir.getPath());
    if (!Strings.isNullOrEmpty(hostname)) {
      paramsBuilder.add("-h", hostname);
    }
    if (!Strings.isNullOrEmpty(basePath)) {
      paramsBuilder.add("-p", basePath);
    }
    paramsBuilder.addAll(getMergedServiceClasses());
    return paramsBuilder.build().toArray(new String[] {});
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

  private void recreateOutputDir() {
    File outputDir = getOutputDir();
    if (!outputDir.isDirectory()) {
      outputDir = outputDir.getParentFile();
    }
    getProject().delete(outputDir);
    getProject().mkdir(outputDir);
  }

  private Collection<String> getMergedServiceClasses() {
    Collection<String> allServiceClasses = new HashSet<>();
    allServiceClasses.addAll(getServiceClasses());
    Collection<String> apiClassesInSourceAnnotations =
        new AnnotationServletScanner(getProject()).findApiClassesInSourceAnnotations();
    allServiceClasses.addAll(apiClassesInSourceAnnotations);
    return allServiceClasses;
  }

  abstract String actionName();

  abstract File getOutputDir();
}
