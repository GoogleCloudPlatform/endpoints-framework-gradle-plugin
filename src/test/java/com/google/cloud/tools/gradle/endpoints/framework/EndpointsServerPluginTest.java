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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.zip.ZipFile;
import org.gradle.testkit.runner.BuildResult;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test endpoints server plugin builds. */
public class EndpointsServerPluginTest {

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

  @Rule public TemporaryFolder testProjectDir = new TemporaryFolder();

  @Test
  public void testClientLibs() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .gradleRunnerArguments("endpointsClientLibs")
            .build();

    assertClientLibGeneration(DEFAULT_URL_VARIABLE, null);
  }

  @Test
  public void testClientLibs_hostname() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .hostname("my.hostname.com")
            .gradleRunnerArguments("endpointsClientLibs")
            .build();

    assertClientLibGeneration(
        DEFAULT_URL_PREFIX + "\"https://my.hostname.com/_ah/api/\";", DEFAULT_URL_VARIABLE);
  }

  @Test
  public void testClientLibs_application() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .applicationId("gradle-test")
            .gradleRunnerArguments("endpointsClientLibs")
            .build();

    assertClientLibGeneration(
        DEFAULT_URL_PREFIX + "\"https://gradle-test.appspot.com/_ah/api/\";", DEFAULT_URL_VARIABLE);
  }

  private void assertClientLibGeneration(String expected, String unexpected) throws IOException {
    File clientLib = new File(testProjectDir.getRoot(), CLIENT_LIB_PATH);
    Assert.assertTrue(clientLib.exists());
    Assert.assertEquals(1, clientLib.getParentFile().listFiles().length);
    String apiJavaFile = getFileContentsInZip(clientLib, API_JAVA_FILE_PATH);
    Assert.assertThat(apiJavaFile, CoreMatchers.containsString(expected));
    if (unexpected != null) {
      Assert.assertThat(apiJavaFile, CoreMatchers.not(CoreMatchers.containsString(unexpected)));
    }
  }

  private String getFileContentsInZip(File zipFile, String path) throws IOException {
    ZipFile zip = new ZipFile(zipFile);
    InputStream is = zip.getInputStream(zip.getEntry(path));
    return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
  }

  @Test
  public void testDiscoveryDocs() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .gradleRunnerArguments("endpointsDiscoveryDocs")
            .build();

    assertDiscoveryDocGeneration(DEFAULT_URL, null);
  }

  @Test
  public void testDiscoveryDocs_hostname() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .hostname("my.hostname.com")
            .gradleRunnerArguments("endpointsDiscoveryDocs")
            .build();

    assertDiscoveryDocGeneration("https://my.hostname.com/_ah/api", DEFAULT_URL);
  }

  @Test
  public void testDiscoveryDocs_application() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .applicationId("gradle-test")
            .gradleRunnerArguments("endpointsDiscoveryDocs")
            .build();

    assertDiscoveryDocGeneration("https://gradle-test.appspot.com/_ah/api", DEFAULT_URL);
  }

  private void assertDiscoveryDocGeneration(String expected, String unexpected) throws IOException {
    File discoveryDoc = new File(testProjectDir.getRoot(), DISC_DOC_PATH);
    String discovery = Files.toString(discoveryDoc, Charsets.UTF_8);
    Assert.assertThat(discovery, CoreMatchers.containsString(expected));
    if (unexpected != null) {
      Assert.assertThat(discovery, CoreMatchers.not(CoreMatchers.containsString(unexpected)));
    }
  }

  @Test
  public void testOpenApiDocs() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .gradleRunnerArguments("endpointsOpenApiDocs")
            .build();

    assertOpenApiDocGeneration(DEFAULT_HOSTNAME, null);
  }

  @Test
  public void testOpenApiDocs_hostname() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .hostname("my.hostname.com")
            .gradleRunnerArguments("endpointsOpenApiDocs")
            .build();

    assertOpenApiDocGeneration("my.hostname.com", DEFAULT_HOSTNAME);
  }

  @Test
  public void testOpenApiDocs_application() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/server")
            .applicationId("gradle-test")
            .gradleRunnerArguments("endpointsOpenApiDocs")
            .build();

    assertOpenApiDocGeneration("gradle-test.appspot.com", DEFAULT_HOSTNAME);
  }

  private void assertOpenApiDocGeneration(String expected, String unexpected) throws IOException {
    File openApiDoc = new File(testProjectDir.getRoot(), OPEN_API_DOC_PATH);
    String openApi = Files.toString(openApiDoc, Charsets.UTF_8);
    Assert.assertThat(openApi, CoreMatchers.containsString(expected));
    if (unexpected != null) {
      Assert.assertThat(openApi, CoreMatchers.not(CoreMatchers.containsString(unexpected)));
    }
  }
}
