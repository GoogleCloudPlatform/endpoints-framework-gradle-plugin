package com.google.cloud.tools.gradle.endpoints.framework.server.task.scan;

import com.google.common.collect.ImmutableSet;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class ApiClassesLookup {

  private static final Class[] EMPTY_CLASS_ARRAY = {};
  private static final String JAVAX_WEB_INIT_PARAM = "javax.servlet.annotation.WebInitParam";
  private static final String JAVAX_WEB_SERVLET = "javax.servlet.annotation.WebServlet";

  private final Collection<Class<?>> classesToScan;
  private final Method nameMethod;
  private final Method valueMethod;

  /** The only one constructor. */
  public ApiClassesLookup(Collection<Class<?>> classesToScan, ClassLoader classLoader)
      throws ClassNotFoundException, NoSuchMethodException {
    this.classesToScan = classesToScan;
    Class<?> webInitParamAnnotationClass = classLoader.loadClass(JAVAX_WEB_INIT_PARAM);
    nameMethod = webInitParamAnnotationClass.getDeclaredMethod("name", EMPTY_CLASS_ARRAY);
    valueMethod = webInitParamAnnotationClass.getDeclaredMethod("value", EMPTY_CLASS_ARRAY);
  }

  /** Returns list of API class names found during classpath scan. */
  public Collection<String> apiClassNames()
      throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException,
          InvocationTargetException, ClassNotFoundException {

    ImmutableSet.Builder<String> resultBuilder = ImmutableSet.builder();
    for (Class<?> servletClass : classesToScan) {
      for (Annotation annotation : servletClass.getAnnotations()) {
        Class<? extends Annotation> webServletAnnotationClass = annotation.annotationType();
        if (JAVAX_WEB_SERVLET.equals(webServletAnnotationClass.getName())) {
          Object[] initParamsValue =
              getInitParamsFromWebServletAnnotation(servletClass, webServletAnnotationClass);

          for (Object webInitParam : initParamsValue) {
            if ("services".equals(nameMethod.invoke(webInitParam))) {
              String declaredServiceClassNames = valueMethod.invoke(webInitParam).toString();
              resultBuilder.add(declaredServiceClassNames.split(","));
            }
          }
        }
      }
    }

    return resultBuilder.build();
  }

  private Object[] getInitParamsFromWebServletAnnotation(
      Class<?> servletClass, Class<? extends Annotation> webServletAnnotationClass)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Annotation webServletAnnotation = servletClass.getAnnotation(webServletAnnotationClass);
    Method initParams =
        webServletAnnotationClass.getDeclaredMethod("initParams", EMPTY_CLASS_ARRAY);
    return (Object[]) initParams.invoke(webServletAnnotation);
  }
}
