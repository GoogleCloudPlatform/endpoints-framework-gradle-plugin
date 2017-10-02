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
import com.google.api.server.spi.tools.GetClientLibAction;
import java.io.File;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * Endpoints task to download a client library document from the endpoints service, useful for
 * projects not in the same gradle configuration. For clients and servers that are part of the same
 * gradle project, use EndpointsClientPlugin
 */
public class GenerateClientLibsTask extends AbstractEndpointsTask {

  private File clientLibDir;

  @OutputDirectory
  public File getClientLibDir() {
    return clientLibDir;
  }

  public void setClientLibDir(File clientLibDir) {
    this.clientLibDir = clientLibDir;
  }

  @TaskAction
  void generateClientLibs() throws Exception {

    // We do *not* delete the output directory for this task, and we do *not* ensure that is clean.
    // If the user specifies an output directory that is outside the gradle buildDir we don't want
    // to accidentally delete anything. Since this task is not a dependency for any other task,
    // having builds write new versions of client libraries to the output directory doesn't really
    // affect anything.
    String[] execParameters = initDirsAndGetExecParams(false, "-l", "java", "-bs", "gradle");
    new EndpointsTool().execute(execParameters);
  }

  @Override
  String actionName() {
    return GetClientLibAction.NAME;
  }

  @Override
  File getOutputDir() {
    return clientLibDir;
  }
}
