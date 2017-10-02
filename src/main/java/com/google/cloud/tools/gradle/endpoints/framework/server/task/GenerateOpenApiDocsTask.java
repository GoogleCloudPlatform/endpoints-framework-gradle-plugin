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
import java.io.File;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/** Endpoints task to download a openapi document from the endpoints service. */
public class GenerateOpenApiDocsTask extends AbstractEndpointsTask {

  private File openApiDocDir;

  @OutputDirectory
  public File getOpenApiDocDir() {
    return openApiDocDir;
  }

  public void setOpenApiDocDir(File openApiDocDir) {
    this.openApiDocDir = openApiDocDir;
  }

  /** Task entry point. */
  @TaskAction
  void generateOpenApiDocs() throws Exception {
    String[] execParams = initDirsAndGetExecParams(true);
    new EndpointsTool().execute(execParams);
  }

  @Override
  String actionName() {
    return GetOpenApiDocAction.NAME;
  }

  @Override
  File getOutputDir() {
    return new File(openApiDocDir, "openapi.json");
  }
}
