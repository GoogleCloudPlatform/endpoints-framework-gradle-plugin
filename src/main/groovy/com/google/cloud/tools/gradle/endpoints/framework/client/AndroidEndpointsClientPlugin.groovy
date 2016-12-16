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

package com.google.cloud.tools.gradle.endpoints.framework.client

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Client side extensions for android projects, written in groovy because
 * it lets us do the crazy things we do in here
 */
public class AndroidEndpointsClientPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def endpointsClient = project.extensions.findByType(EndpointsClientExtension)
    def android = project.extensions.getByName("android");

    // up till android studio 2.2, any generated source MUST be specifically in the
    // generated/source directory or the IDE wont recognize it.
    endpointsClient.genSrcDir = new File(project.buildDir, "generated/source/endpoints");

    // register our source generating task and outputs with the android model
    def genSrcTask = project.tasks.getByName(EndpointsClientPlugin.GENERATE_CLIENT_LIBRARY_SRC_TASK)
    android.applicationVariants.all { variant ->
      variant.registerJavaGeneratingTask(genSrcTask, endpointsClient.genSrcDir)
    }
  }
}
