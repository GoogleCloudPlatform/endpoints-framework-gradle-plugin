package com.google.cloud.tools.gradle.endpoints.framework.server.task.scan;

import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.specs.Spec;

public class ServletApiDependencyScan implements Spec<DefaultExternalModuleDependency> {
  @Override
  public boolean isSatisfiedBy(DefaultExternalModuleDependency dependency) {
    return "javax.servlet".equals(dependency.getGroup())
        && ("javax.servlet-api".equals(dependency.getName())
            || "servlet-api".equals(dependency.getName()));
  }
}
