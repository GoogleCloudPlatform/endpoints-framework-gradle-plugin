package com.google.cloud.tools.gradle.endpoints.framework.server.task.scan;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.specs.Spec;

public class EndpointsFrameworkDependencySpec implements Spec<Dependency> {
  @Override
  public boolean isSatisfiedBy(Dependency element) {
    return "com.google.endpoints".equals(element.getGroup())
        && "endpoints-framework".equals(element.getName());
  }
}
