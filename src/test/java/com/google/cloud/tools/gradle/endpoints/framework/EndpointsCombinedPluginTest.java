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
import org.gradle.testkit.runner.BuildResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test endpoints client-server plugin integration builds. */
public class EndpointsCombinedPluginTest {

  @Rule public final TemporaryFolder testProjectDir = new TemporaryFolder();

  @Test
  public void testClientServerIntegrationBuilds() throws IOException, URISyntaxException {
    BuildResult buildResult =
        new TestProject(testProjectDir.getRoot(), "projects/clientserver")
            .gradleRunnerArguments("assemble")
            .build();

    // the client project will NOT build if the integration is not working correctly because of the
    // dependency on generated source.

    File genSrcDir = new File(testProjectDir.getRoot(), "client/build/endpointsGenSrc");
    File genSrcFile = new File(genSrcDir, "com/example/testApi/TestApi.java");

    Assert.assertTrue(genSrcFile.exists());
  }
}
