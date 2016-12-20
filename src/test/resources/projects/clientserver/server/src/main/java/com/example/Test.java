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

package com.example;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import javax.inject.Named;

@Api(
    name = "testApi",
    version = "v1",
    namespace = @ApiNamespace(ownerDomain = "example.com", ownerName = "example.com", packagePath = "")
)
public class Test {

  @ApiMethod(name = "echo")
  public MyBean echo(@Named("name") String name) {
    MyBean response = new MyBean();
    response.setString("ECHO " + name);

    return response;
  }
}
