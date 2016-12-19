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

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class ProjectTests {

  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder();

  @Test
  public void testClientServerIntegrationBuilds() throws IOException, URISyntaxException {
    FileUtils.copyDirectory(
        new File(getClass().getClassLoader().getResource("projects/clientserver").toURI()),
        testProjectDir.getRoot());
    BuildResult buildResult = GradleRunner.create()
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
    BuildResult buildResult = GradleRunner.create()
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
    BuildResult buildResult = GradleRunner.create()
        .withProjectDir(testProjectDir.getRoot())
        .withPluginClasspath()
        .withArguments("endpointsClientLibs", "endpointsDiscoveryDocs")
        .build();
    // client lib geneneration actually generates a discovery doc as well, so check the
    // above directory for that
    File clientLibRoot = new File(testProjectDir.getRoot(), "/build/endpointsClientLibs");
    Assert.assertEquals(2, clientLibRoot.listFiles().length);
    Assert.assertTrue(new File(clientLibRoot, "testApi-v1-rest.discovery").exists());
    Assert.assertTrue(new File(clientLibRoot, "testApi-v1-java.zip").exists());

    File discoveryDocRoot = new File(testProjectDir.getRoot(), "/build/endpointsDiscoveryDocs");
    Assert.assertEquals(1, discoveryDocRoot.listFiles().length);
    Assert.assertTrue(new File(discoveryDocRoot, "testApi-v1-rest.discovery").exists());
  }
}
