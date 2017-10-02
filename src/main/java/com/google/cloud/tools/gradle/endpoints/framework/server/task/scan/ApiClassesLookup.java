package com.google.cloud.tools.gradle.endpoints.framework.server.task.scan;

import com.google.common.collect.ImmutableSet;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;

public class ApiClassesLookup {

  private static final Class[] EMPTY_CLASS_ARRAY = {};
  private static final String JAVAX_WEB_INIT_PARAM = "javax.servlet.annotation.WebInitParam";
  private static final String JAVAX_WEB_SERVLET = "javax.servlet.annotation.WebServlet";

  private final Collection<Class<?>> classesToScan;
  private final Method nameMethod;
  private final Method valueMethod;

  /** The only one constructor. */
  public ApiClassesLookup(Collection<Class<?>> classesToScan, ClassLoader classLoader) {
    this.classesToScan = classesToScan;
    try {
      Class<?> webInitParamAnnotationClass = classLoader.loadClass(JAVAX_WEB_INIT_PARAM);
      nameMethod = webInitParamAnnotationClass.getDeclaredMethod("name", EMPTY_CLASS_ARRAY);
      valueMethod = webInitParamAnnotationClass.getDeclaredMethod("value", EMPTY_CLASS_ARRAY);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new GradleException("Failed to read class metadata for " + JAVAX_WEB_INIT_PARAM, e);
    }
  }

  /** Returns list of API class names found during classpath scan. */
  public Collection<String> apiClassNames() {

    ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();
    for (Class<?> servletClass : classesToScan) {
      for (Annotation annotation : servletClass.getAnnotations()) {
        Class<? extends Annotation> webServletAnnotationClass = annotation.annotationType();
        if (JAVAX_WEB_SERVLET.equals(webServletAnnotationClass.getName())) {
          for (Object webInitParam :
              getDeclaredInitParams(servletClass, webServletAnnotationClass)) {
            if ("services".equals(invokeMethod(webInitParam, nameMethod))) {
              String declaredServiceClassNames = invokeMethod(webInitParam, valueMethod).toString();
              String[] split = StringUtils.split(declaredServiceClassNames, ",");
              for (String declaredClass : split) {
                resultBuilder.add(StringUtils.trim(declaredClass));
              }
            }
          }
        }
      }
    }
    return resultBuilder.build();
  }

  private static Object invokeMethod(Object webInitParam, Method nameMethod) {
    try {
      return nameMethod.invoke(webInitParam);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new GradleException("Failed to read init params metadata", e);
    }
  }

  private static Object[] getDeclaredInitParams(
      Class<?> servletClass, Class<? extends Annotation> webServletAnnotationClass) {
    Annotation webServletAnnotation = servletClass.getAnnotation(webServletAnnotationClass);
    try {
      Method initParams =
          webServletAnnotationClass.getDeclaredMethod("initParams", EMPTY_CLASS_ARRAY);
      return (Object[]) invokeMethod(webServletAnnotation, initParams);
    } catch (NoSuchMethodException e) {
      throw new GradleException(
          "Failed to read init params metadata for class " + servletClass.getName(), e);
    }
  }
}
