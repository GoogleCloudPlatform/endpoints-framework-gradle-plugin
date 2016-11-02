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

package com.google.cloud.tools.gradle.endpoints.client;

import com.google.cloud.tools.gradle.endpoints.client.task.ExtractDiscoveryDocZipsTask;
import com.google.cloud.tools.gradle.endpoints.client.task.GenerateClientLibrariesTask;
import com.google.cloud.tools.gradle.endpoints.client.task.GenerateClientLibrarySourceTask;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * Plugin definition for Endpoints Clients. All tasks from this plugin are internal,
 * it will automatically generate source into build/endpointsGenSrc
 * (see {@link EndpointsClientExtension}) based on the user's configuration.
 * <p/>
 * Configuration of source discovery docs is from two ways:
 * <p/>
 * 1. specify the location of the discovery doc with the extension
 * <pre>
 * {@code
 * endpointsClient {
 *   discoveryDocs = [file(path/to/xyz.discovery)]
 * }
 * }
 * </pre>
 * 2. depend directly on another project that has the endpoints server plugin
 * <pre>
 * {@code
 * dependencies {
 *   endpointsServer project(path: ":server", configuration: "discovery-docs");
 * }
 * }
 * </pre>
 * Independent of what mechanism above is used, the user must still explicitly add
 * a dependency on the google api client library
 * <pre>
 * {@code
 * dependencies {
 *   compile "com.google.api-client:google-api-client:+"
 * }
 * }
 * </pre>
 */
public class EndpointsClientPlugin implements Plugin<Project> {

  private static final String GENERATE_CLIENT_LIBRARY_TASK = "_endpointsClientLibs";
  private static final String GENERATE_CLIENT_LIBRARY_SRC_TASK = "_endpointsClientGenSrc";
  private static final String EXTRACT_SERVER_DISCOVERY_DOCS_TASK = "_extractServerDiscoveryDocs";

  public static final String ENDPOINTS_CLIENT_EXTENSION = "endpointsClient";
  public static final String ENDPOINTS_SERVER_CONFIGURATION = "endpointsServer";

  private Project project;
  private EndpointsClientExtension extension;

  public void apply(Project project) {
    this.project = project;
    createExtension();
    createConfiguration();
    createExtractServerDiscoveryDocsTask();
    createGenerateClientLibTask();
    createGenerateClientLibSrcTask();
  }

  private void createExtension() {
    extension = project.getExtensions()
        .create(ENDPOINTS_CLIENT_EXTENSION, EndpointsClientExtension.class, project.getBuildDir());
  }

  private void createConfiguration() {
    project.getConfigurations().create(ENDPOINTS_SERVER_CONFIGURATION)
        .setDescription("endpointsServer project(path: ':xyz', configuration: 'discovery-docs')")
        .setVisible(false);

  }

  // extract discovery docs from "endpointsServer" configurations
  private void createExtractServerDiscoveryDocsTask() {
    project.getTasks().create(EXTRACT_SERVER_DISCOVERY_DOCS_TASK,
        ExtractDiscoveryDocZipsTask.class, new Action<ExtractDiscoveryDocZipsTask>() {
          @Override
          public void execute(final ExtractDiscoveryDocZipsTask extractDiscoveryDocs) {
            extractDiscoveryDocs.setDescription("_internal");
            // make sure we depend on the server configuration build tasks, so those get run
            // before we run our task to get the discovery docs
            extractDiscoveryDocs.dependsOn(
                project.getConfigurations().getByName(ENDPOINTS_SERVER_CONFIGURATION)
                    .getBuildDependencies());

            // iterate through the configuration and get all artifacts (discovery doc zips)
            project.afterEvaluate(new Action<Project>() {
              @Override
              public void execute(Project project) {
                Collection<File> files = project.getConfigurations()
                    .getByName(ENDPOINTS_SERVER_CONFIGURATION).getFiles();
                extractDiscoveryDocs.setDiscoveryDocZips(files);
                extractDiscoveryDocs.setDiscoveryDocsDir(extension.getGenDiscoveryDocsDir());
              }
            });
          }
        });
  }

  private void createGenerateClientLibTask() {
    project.getTasks().create(GENERATE_CLIENT_LIBRARY_TASK, GenerateClientLibrariesTask.class,
        new Action<GenerateClientLibrariesTask>() {
          @Override
          public void execute(final GenerateClientLibrariesTask genClientLibs) {
            genClientLibs.setDescription("_internal");
            genClientLibs.dependsOn(EXTRACT_SERVER_DISCOVERY_DOCS_TASK);

            project.afterEvaluate(new Action<Project>() {
              @Override
              public void execute(Project project) {
                genClientLibs.setClientLibraryDir(extension.getClientLibDir());
                genClientLibs.setDiscoveryDocs(getDiscoveryDocs());
                genClientLibs.setGeneratedDiscoveryDocs(extension.getGenDiscoveryDocsDir());
              }
            });
          }
        });
  }

  // find discovery docs from files and directories
  private List<File> getDiscoveryDocs() {
    List<File> discoveryDocs = new ArrayList<File>();
    for (File discoveryDoc : extension.getDiscoveryDocs()) {
      if (discoveryDoc.isDirectory()) {
        discoveryDocs.addAll(DiscoveryDocUtil.findDiscoveryDocsInDirectory(discoveryDoc));
      } else {
        discoveryDocs.add(discoveryDoc);
      }
    }
    return discoveryDocs;
  }

  private void createGenerateClientLibSrcTask() {
    project.getTasks()
        .create(GENERATE_CLIENT_LIBRARY_SRC_TASK, GenerateClientLibrarySourceTask.class,
            new Action<GenerateClientLibrarySourceTask>() {
              @Override
              public void execute(final GenerateClientLibrarySourceTask genClientLibSrc) {
                genClientLibSrc.setDescription("_internal");
                genClientLibSrc.dependsOn(GENERATE_CLIENT_LIBRARY_TASK);

                project.afterEvaluate(new Action<Project>() {
                  @Override
                  public void execute(Project project) {
                    genClientLibSrc.setClientLibDir(extension.getClientLibDir());
                    genClientLibSrc.setGeneratedSrcDir(extension.getGenSrcDir());
                  }
                });
              }
            });

    // since we are generating sources add the gen-src directory to the main java sourceset
    project.afterEvaluate(new Action<Project>() {
      @Override
      public void execute(Project project) {
        project.getTasks().getByPath(JavaPlugin.COMPILE_JAVA_TASK_NAME)
            .dependsOn(GENERATE_CLIENT_LIBRARY_SRC_TASK);

        JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
        SourceSetContainer sourceSets = java.getSourceSets();
        SourceSet mainSrc = sourceSets.getByName("main");
        mainSrc.getJava().srcDir(extension.getGenSrcDir());
      }
    });
  }
}

