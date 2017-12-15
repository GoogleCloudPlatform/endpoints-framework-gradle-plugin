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

import java.io.File;
import java.util.List;

public interface EndpointsTaskConfiguration {

  /**
   * Specify if the tasks needs to clean the output directory ({@code true}) or not ({@code false}).
   */
  boolean needsClean();

  /**
   * Returns a list of action specific parameters that must contain "action name", "output target"
   * and may optionally contain other flags specific to the action.
   */
  List<String> getActionSpecificParams(File outputDirectory);
}
