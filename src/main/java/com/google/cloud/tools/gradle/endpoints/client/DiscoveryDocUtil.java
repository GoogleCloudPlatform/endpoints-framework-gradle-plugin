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

import com.google.api.client.util.Preconditions;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for discovery docs
 */
public class DiscoveryDocUtil {

  /**
   * Find all discovery docs in a directory at root level ending with .discovery
   */
  public static List<File> findDiscoveryDocsInDirectory(File discoveryDocDirectory) {
    Preconditions.checkArgument(discoveryDocDirectory.isDirectory());

    File[] discoveryDocs = discoveryDocDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".discovery");
      }
    });
    return Arrays.asList(discoveryDocs);
  }

}
