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

package com.google.cloud.tools.gradle.endpoints.framework;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProjectTests {
  private static final String DEFAULT_HOSTNAME = "myapi.appspot.com";
  private static final String DEFAULT_URL = "https://" + DEFAULT_HOSTNAME + "/_ah/api";
  private static final String DEFAULT_URL_PREFIX = "public static final String DEFAULT_ROOT_URL = ";
  private static final String DEFAULT_URL_VARIABLE =
      DEFAULT_URL_PREFIX + "\"https://myapi.appspot.com/_ah/api/\";";
  private static final String CLIENT_LIB_PATH = "build/endpointsClientLibs/testApi-v1-java.zip";
  private static final String DISC_DOC_PATH =
      "build/endpointsDiscoveryDocs/testApi-v1-rest.discovery";
  private static final String API_JAVA_FILE_PATH =
      "testApi/src/main/java/com/example/testApi/TestApi.java";
  private static final String OPEN_API_DOC_PATH = "build/endpointsOpenApiDocs/openapi.json";

  @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();

  @Test
  public void testClientServerIntegrationBuilds() throws IOException, URISyntaxException {
    FileUtils.copyDirectory(
        new File(getClass().getClassLoader().getResource("projects/clientserver").toURI()),
        testProjectDir.getRoot());
    BuildResult buildResult =
        GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withPluginClasspath()
            .withArguments("assemble")
            .build();
  }

  @Test
  public void testClientBuilds() throws IOException, URISyntaxException {
    FileUtils.copyDirectory(
        new File(getClass().getClassLoader().getResource("projects/client").toURI()),
        testProjectDir.getRoot());
    BuildResult buildResult =
        GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withPluginClasspath()
            .withArguments("assemble")
            .build();
  }

  @Test
  public void testServer() throws IOException, URISyntaxException {
    FileUtils.copyDirectory(
        new File(getClass().getClassLoader().getResource("projects/server").toURI()),
        testProjectDir.getRoot());
    BuildResult buildResult =
        GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withPluginClasspath()
            .withArguments("endpointsClientLibs", "endpointsDiscoveryDocs", "endpointsOpenApi")
            .build();

    File discoveryDoc = new File(testProjectDir.getRoot(), DISC_DOC_PATH);
    Assert.assertTrue(discoveryDoc.exists());
    Assert.assertEquals(1, discoveryDoc.getParentFile().listFiles().length);
    String discovery = Files.toString(discoveryDoc, Charsets.UTF_8);
    Assert.assertThat(discovery, CoreMatchers.containsString(DEFAULT_URL));

    File clientLib = new File(testProjectDir.getRoot(), CLIENT_LIB_PATH);
    Assert.assertTrue(clientLib.exists());
    Assert.assertEquals(1, clientLib.getParentFile().listFiles().length);
    String apiJavaFile = getFileContentsInZip(clientLib, API_JAVA_FILE_PATH);
    Assert.assertThat(apiJavaFile, CoreMatchers.containsString(DEFAULT_URL_VARIABLE));

    File openApiDoc = new File(testProjectDir.getRoot(), OPEN_API_DOC_PATH);
    Assert.assertTrue(openApiDoc.exists());
    Assert.assertEquals(1, openApiDoc.getParentFile().listFiles().length);
    String openApi = Files.toString(openApiDoc, Charsets.UTF_8);
    Assert.assertThat(openApi, CoreMatchers.containsString(DEFAULT_HOSTNAME));
  }

  @Test
  public void testServerWithHostname() throws IOException, URISyntaxException {
    FileUtils.copyDirectory(
        new File(getClass().getClassLoader().getResource("projects/server").toURI()),
        testProjectDir.getRoot());
    injectConfiguration(testProjectDir.getRoot(), "endpointsServer.hostname = 'my.hostname.com'");
    BuildResult buildResult =
        GradleRunner.create()
            .withProjectDir(testProjectDir.getRoot())
            .withPluginClasspath()
            .withArguments("endpointsClientLibs", "endpointsDiscoveryDocs", "endpointsOpenApiDocs")
            .build();

    File discoveryDoc = new File(testProjectDir.getRoot(), DISC_DOC_PATH);
    String discovery = Files.toString(discoveryDoc, Charsets.UTF_8);
    Assert.assertThat(discovery, CoreMatchers.not(CoreMatchers.containsString(DEFAULT_URL)));
    Assert.assertThat(discovery, CoreMatchers.containsString("https://my.hostname.com/_ah/api"));

    File clientLib = new File(testProjectDir.getRoot(), CLIENT_LIB_PATH);
    String apiJavaFile = getFileContentsInZip(clientLib, API_JAVA_FILE_PATH);
    Assert.assertThat(
        apiJavaFile, CoreMatchers.not(CoreMatchers.containsString(DEFAULT_URL_VARIABLE)));
    Assert.assertThat(
        apiJavaFile,
        CoreMatchers.containsString(DEFAULT_URL_PREFIX + "\"https://my.hostname.com/_ah/api/\";"));

    File openApiDoc = new File(testProjectDir.getRoot(), OPEN_API_DOC_PATH);
    String openApi = Files.toString(openApiDoc, Charsets.UTF_8);
    Assert.assertThat(openApi, CoreMatchers.not(CoreMatchers.containsString(DEFAULT_HOSTNAME)));
    Assert.assertThat(openApi, CoreMatchers.containsString("my.hostname.com"));
  }

  // inject a endpoints plugin configuration into the pom.xml
  private File injectConfiguration(File root, String configuration) throws IOException {
    File pom = new File(root, "build.gradle");
    String pomContents = FileUtils.readFileToString(pom);
    pomContents = pomContents.replaceAll("/\\*endpoints-plugin-configuration\\*/", configuration);
    FileUtils.writeStringToFile(pom, pomContents);
    return root;
  }

  // inject an application tag into the appengine-web.xml
  private File injectApplicationId(File root, String application) throws IOException {
    File app = new File(root, "src/main/webapp/WEB-INF/appengine-web.xml");
    String appContents = FileUtils.readFileToString(app);
    appContents = appContents.replaceAll("<!--application-->", application);
    FileUtils.writeStringToFile(app, appContents);
    return root;
  }

  private String getFileContentsInZip(File zipFile, String path) throws IOException {
    ZipFile zip = new ZipFile(zipFile);
    InputStream is = zip.getInputStream(zip.getEntry(path));
    return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
  }
}
