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

package com.google.cloud.tools.gradle.endpoints.framework.client.task;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/** Task to populate a generated source folder of client lib code. */
public class GenerateClientLibrarySourceTask extends DefaultTask {
  private File clientLibDir;
  private File generatedSrcDir;

  @OutputDirectory
  public File getGeneratedSrcDir() {
    return generatedSrcDir;
  }

  public void setGeneratedSrcDir(File generatedSrcDir) {
    this.generatedSrcDir = generatedSrcDir;
  }

  @InputDirectory
  public File getClientLibDir() {
    return clientLibDir;
  }

  public void setClientLibDir(File clientLibDir) {
    this.clientLibDir = clientLibDir;
  }

  /** Task entry point. */
  @TaskAction
  public void generateSource() {
    boolean x = getProject().delete(generatedSrcDir);
    File[] zips =
        getClientLibDir()
            .listFiles(
                new FilenameFilter() {
                  @Override
                  public boolean accept(File dir, String name) {
                    return name.endsWith(".zip");
                  }
                });

    final File tmpDir = new File(getTemporaryDir(), "endpoints-tmp");
    getProject().delete(tmpDir);
    tmpDir.mkdir();
    for (final File zip : zips) {
      // Use ant unzip, gradle unzip is having issues
      // with strangely formed client libraries
      getAnt()
          .invokeMethod(
              "unzip",
              new HashMap<String, String>() {
                {
                  put("src", zip.getPath());
                  put("dest", tmpDir.getPath());
                }
              });
    }

    for (File unzippedDir : tmpDir.listFiles()) {
      final File srcDir = new File(unzippedDir, "src/main/java");
      if (srcDir.exists() && srcDir.isDirectory()) {
        getProject()
            .copy(
                new Action<CopySpec>() {
                  @Override
                  public void execute(CopySpec copySpec) {
                    copySpec.from(srcDir);
                    copySpec.into(generatedSrcDir);
                  }
                });
      }
    }
  }
}
