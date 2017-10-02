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
import com.google.api.server.spi.tools.GetDiscoveryDocAction;
import java.io.File;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/** Endpoints task to download a discovery document from the endpoints service. */
public class GenerateDiscoveryDocsTask extends AbstractEndpointsTask {

  private File discoveryDocDir;

  @OutputDirectory
  public File getDiscoveryDocDir() {
    return discoveryDocDir;
  }

  public void setDiscoveryDocDir(File discoveryDocDir) {
    this.discoveryDocDir = discoveryDocDir;
  }

  /** Task entry point. */
  @TaskAction
  void generateDiscoveryDocs() throws Exception {
    String[] execParameters = initDirsAndGetExecParams(true);
    new EndpointsTool().execute(execParameters);
  }

  @Override
  String actionName() {
    return GetDiscoveryDocAction.NAME;
  }

  @Override
  File getOutputDir() {
    return discoveryDocDir;
  }
}
