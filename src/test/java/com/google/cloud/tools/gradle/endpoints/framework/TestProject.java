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

package com.google.cloud.tools.gradle.endpoints.framework;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

/** Test project fixture. */
public class TestProject {

  private final File testDir;
  private final String projectPathInResources;

  private String hostname;
  private String basePath;
  private String application;
  private String[] gradleRunnerArgs = {"assemble"};

  public TestProject(File testDir, String projectPathInResources) {
    this.testDir = testDir;
    this.projectPathInResources = projectPathInResources;
  }

  public TestProject hostname(String hostname) {
    this.hostname = hostname;
    return this;
  }

  public TestProject basePath(String basePath) {
    this.basePath = basePath;
    return this;
  }

  public TestProject applicationId(String applicationId) {
    this.application = applicationId;
    return this;
  }

  public TestProject gradleRunnerArguments(String... args) {
    this.gradleRunnerArgs = args;
    return this;
  }

  /** Copy project and run gradle build. */
  public BuildResult build() throws URISyntaxException, IOException {
    FileUtils.copyDirectory(
        new File(getClass().getClassLoader().getResource(projectPathInResources).toURI()), testDir);
    if (application != null) {
      injectApplicationId(testDir, application);
    }
    if (hostname != null) {
      injectHostname(hostname);
    }
    if (basePath != null) {
      injectBasePath(basePath);
    }
    return GradleRunner.create()
        .withProjectDir(testDir)
        .withPluginClasspath()
        .withArguments(gradleRunnerArgs)
        .withDebug(true)
        .forwardOutput()
        .build();
  }

  // Inject an endpoints plugin hostname into the build.gradle file.
  private void injectHostname(String hostname) throws IOException {
    injectIntoBuildGradleFile(
        "/\\*endpoints-plugin-hostname\\*/", "endpointsServer.hostname = '" + hostname + "'");
  }

  // Inject an endpoints plugin basePath into the build.gradle file.
  private void injectBasePath(String basePath) throws IOException {
    injectIntoBuildGradleFile(
        "/\\*endpoints-plugin-basePath\\*/", "endpointsServer.basePath = '" + basePath + "'");
  }

  // Helper method to replace text inside the build.gradle file.
  private void injectIntoBuildGradleFile(String regex, String replacement) throws IOException {
    File buildGradle = new File(testDir, "build.gradle");
    String buildGradleContents = FileUtils.readFileToString(buildGradle);
    buildGradleContents = buildGradleContents.replaceAll(regex, replacement);
    FileUtils.writeStringToFile(buildGradle, buildGradleContents);
  }

  // inject an application tag into the appengine-web.xml
  private void injectApplicationId(File projectRoot, String application) throws IOException {
    File app = new File(testDir, "src/main/webapp/WEB-INF/appengine-web.xml");
    String appContents = FileUtils.readFileToString(app);
    appContents =
        appContents.replaceAll(
            "<!--application-->", "<application>" + application + "</application>");
    FileUtils.writeStringToFile(app, appContents);
  }
}
