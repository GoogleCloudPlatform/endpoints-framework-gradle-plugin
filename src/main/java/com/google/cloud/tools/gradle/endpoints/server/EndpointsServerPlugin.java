/*
 * Copyright (c) 2016 Google Inc. All Right Reserved.
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
 *
 */

package com.google.cloud.tools.gradle.endpoints.server;

import com.google.cloud.tools.gradle.endpoints.server.task.GenerateClientLibsTask;
import com.google.cloud.tools.gradle.endpoints.server.task.GenerateDiscoveryDocsTask;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Zip;

import java.io.File;

/**
 * Plugin definition for Endpoints Servers (on App Engine) for generation of
 * client libraries and discovery docs.
 *
 * Also provides the artifact "discovery-docs" that is a zip of all the discovery
 * docs that this server exposes (as defined in web.xml)
 */
public class EndpointsServerPlugin implements Plugin<Project> {

  public static final String GENERATE_DISCOVERY_DOC_TASK = "endpointsDiscoveryDocs";
  public static final String GENERATE_CLINT_LIBS_TASK = "endpointsClientLibs";
  public static final String ENDPOINTS_SERVER_EXTENSION = "endpointsServer";

  private static final String APP_ENGINE_ENDPOINTS = "App Engine Endpoints";

  private Project project;
  private EndpointsServerExtension extension;

  public void apply(Project project) {
    this.project = project;

    createExtension();
    createDiscoverDocConfiguration();
    createGenerateDiscoveryDocsTask();
    createGenerateClientLibsTask();
  }

  private void createExtension() {
    extension = project.getExtensions()
        .create(ENDPOINTS_SERVER_EXTENSION, EndpointsServerExtension.class, project.getBuildDir());
  }

  private void createDiscoverDocConfiguration() {
    project.afterEvaluate(new Action<Project>() {
      @Override
      public void execute(Project project) {
        project.getConfigurations().create("discovery-docs");
        Zip discoveryDocArchive = project.getTasks().create("_zipDiscoveryDocs", Zip.class);
        discoveryDocArchive.dependsOn(GENERATE_DISCOVERY_DOC_TASK);
        discoveryDocArchive.from(extension.getDiscoveryDocDir());
        discoveryDocArchive.setArchiveName(project.getName() + "-" + "discoveryDocs.zip");
        project.getArtifacts().add("discovery-docs", discoveryDocArchive);
      }
    });

  }

  private void createGenerateDiscoveryDocsTask() {
    project.getTasks().create(GENERATE_DISCOVERY_DOC_TASK, GenerateDiscoveryDocsTask.class,
        new Action<GenerateDiscoveryDocsTask>() {
          @Override
          public void execute(final GenerateDiscoveryDocsTask genDiscoveryDocs) {
            genDiscoveryDocs.setDescription("Generate endpoints discovery documents");
            genDiscoveryDocs.setGroup(APP_ENGINE_ENDPOINTS);
            genDiscoveryDocs.dependsOn(JavaPlugin.CLASSES_TASK_NAME);

            project.afterEvaluate(new Action<Project>() {
              @Override
              public void execute(Project project) {
                File classesDir = project.getConvention().getPlugin(JavaPluginConvention.class)
                    .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput()
                    .getClassesDir();
                genDiscoveryDocs.setClassesDir(classesDir);
                genDiscoveryDocs.setDiscoveryDocDir(extension.getDiscoveryDocDir());
                genDiscoveryDocs.setServiceClasses(extension.getServiceClasses());
                genDiscoveryDocs.setFormat(extension.getFormat());
                genDiscoveryDocs.setWebAppDir(
                    project.getConvention().getPlugin(WarPluginConvention.class).getWebAppDir());
              }
            });
          }
        });
  }

  private void createGenerateClientLibsTask() {
    project.getTasks().create(GENERATE_CLINT_LIBS_TASK, GenerateClientLibsTask.class,
        new Action<GenerateClientLibsTask>() {
          @Override
          public void execute(final GenerateClientLibsTask genClientLibs) {
            genClientLibs.setDescription("Generate endpoints client libraries");
            genClientLibs.setGroup(APP_ENGINE_ENDPOINTS);
            genClientLibs.dependsOn(JavaPlugin.CLASSES_TASK_NAME);

            project.afterEvaluate(new Action<Project>() {
              @Override
              public void execute(Project project) {
                File classesDir = project.getConvention().getPlugin(JavaPluginConvention.class)
                    .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput()
                    .getClassesDir();
                genClientLibs.setClassesDir(classesDir);
                genClientLibs.setClientLibDir(extension.getClientLibDir());
                genClientLibs.setServiceClasses(extension.getServiceClasses());
                genClientLibs.setWebAppDir(
                    project.getConvention().getPlugin(WarPluginConvention.class).getWebAppDir());
              }
            });
          }
        });
  }
}

