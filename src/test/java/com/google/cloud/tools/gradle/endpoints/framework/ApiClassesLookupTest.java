package com.google.cloud.tools.gradle.endpoints.framework;

import static org.junit.Assert.assertEquals;

import com.google.cloud.tools.gradle.endpoints.framework.server.task.scan.ApiClassesLookup;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import org.junit.Test;

public class ApiClassesLookupTest {

  private ApiClassesLookup testee =
      new ApiClassesLookup(
          ImmutableSet.of(TestAnnotatedServletAlpha.class, TestAnnotatedServletBeta.class),
          getClass().getClassLoader());

  private final Collection<String> expectedApiClasses =
      ImmutableSet.of("expectedApiClassAlpha", "expectedApiClassBeta", "expectedApiClassBeta2");

  public ApiClassesLookupTest() throws NoSuchMethodException, ClassNotFoundException {}

  @Test
  public void webServiceClassNames() throws Exception {
    Collection<String> foundApiClasses = testee.apiClassNames();
    assertEquals(expectedApiClasses, foundApiClasses);
  }

  @WebServlet(initParams = {@WebInitParam(name = "services", value = "expectedApiClassAlpha")})
  private static class TestAnnotatedServletAlpha {}

  @WebServlet(
      initParams = {
        @WebInitParam(name = "services", value = "expectedApiClassBeta,expectedApiClassBeta2")
      }
  )
  private static class TestAnnotatedServletBeta {}
}
