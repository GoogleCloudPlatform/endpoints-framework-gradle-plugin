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

package com.google.cloud.tools.gradle.endpoints.client.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

public class GetDiscoveryDocsFromDependenciesTask extends DefaultTask {

  private Collection<File> discoveryDocZips;
  private File discoveryDocsDir;

  @InputFiles
  public Collection<File> getDiscoveryDocZips() {
    return discoveryDocZips;
  }

  public void setDiscoveryDocZips(Collection<File> discoveryDocZips) {
    this.discoveryDocZips = discoveryDocZips;
  }

  @OutputDirectory
  public File getDiscoveryDocsDir() {
    return discoveryDocsDir;
  }

  public void setDiscoveryDocsDir(File discoveryDocsDir) {
    this.discoveryDocsDir = discoveryDocsDir;
  }

  @TaskAction
  public void extractDicoveryDocs() {
    getProject().delete(discoveryDocsDir);
    discoveryDocsDir.mkdirs();

    for (final File discoveryDocZip : discoveryDocZips) {
      //final File tmpDir = new File(getTemporaryDir(), discoveryDocZip.getName());
      getAnt().invokeMethod("unzip", new HashMap<String, String>() {{
        put("src", discoveryDocZip.getPath());
        put("dest", discoveryDocsDir.getPath());
      }});
    }
  }
}
