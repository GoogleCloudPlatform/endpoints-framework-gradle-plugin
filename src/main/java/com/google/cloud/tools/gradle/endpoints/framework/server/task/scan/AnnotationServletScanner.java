package com.google.cloud.tools.gradle.endpoints.framework.server.task.scan;

import static java.util.Collections.emptySet;
import static org.gradle.api.plugins.JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.specs.Spec;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

/**
 * Scans compile classpath and returns list of endpoints API classes mentioned
 * in @WebServlet\@WebInitParam annotations.
 */
public class AnnotationServletScanner {

  private final Project project;
  private final Configuration compileConfiguration;

  /** The only one constructor. */
  public AnnotationServletScanner(Project project) {
    this.project = project;
    compileConfiguration =
        project.getConfigurations().getByName(COMPILE_CLASSPATH_CONFIGURATION_NAME);
  }

  /** Performs real classpath scan. */
  public Collection<String> findApiClassesInSourceAnnotations() {
    String servletApiVersion = getLatestServletApiVersion();
    if (StringUtils.isEmpty(servletApiVersion)) {
      return emptySet();
    }

    URL projectClassesUrl = toUrl(new File(project.getBuildDir().getPath() + "/classes/java/main"));
    URL servletJarUrl = toUrl(getServletApiDependencyJar(servletApiVersion));
    URL endpointsUrl = toUrl(getEndpointsJar());
    URLClassLoader classLoader =
        URLClassLoader.newInstance(new URL[] {projectClassesUrl, servletJarUrl, endpointsUrl});
    Set<Class<?>> servletClasses = getServletClasses(projectClassesUrl, classLoader);
    if (servletClasses.isEmpty()) {
      return emptySet();
    }
    return new ApiClassesLookup(servletClasses, classLoader).apiClassNames();
  }

  @SuppressWarnings("unchecked")
  private Set<Class<?>> getServletClasses(URL projectClassesLocation, URLClassLoader classLoader) {
    Class<?> httpServletClass;
    try {
      httpServletClass = classLoader.loadClass("com.google.api.server.spi.EndpointsServlet");
    } catch (ClassNotFoundException e) {
      throw new GradleException("Cannot load class com.google.api.server.spi.EndpointsServlet", e);
    }

    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .addClassLoader(classLoader)
                .setScanners(new SubTypesScanner(true))
                .addUrls(projectClassesLocation));

    return (Set<Class<?>>) reflections.getSubTypesOf(httpServletClass);
  }

  private File getServletApiDependencyJar(final String version) {
    Set<File> files =
        compileConfiguration.files(
            new Spec<Dependency>() {
              @Override
              public boolean isSatisfiedBy(Dependency element) {
                return version.equals(element.getVersion());
              }
            });
    if (files.isEmpty()) {
      throw new GradleException("Cannot find servlet-api jar on classpath");
    }
    return files.iterator().next();
  }

  private File getEndpointsJar() {
    return compileConfiguration.files(new EndpointsFrameworkDependencySpec()).iterator().next();
  }

  /** Returns latest version of Servlet API available in app compile-time classpath. */
  private String getLatestServletApiVersion() {
    DomainObjectSet<DefaultExternalModuleDependency> servletApiDependencies =
        project
            .getConfigurations()
            .getByName(COMPILE_CLASSPATH_CONFIGURATION_NAME)
            .getAllDependencies()
            .withType(DefaultExternalModuleDependency.class)
            .matching(new ServletApiDependencyScan());
    SortedSet<DefaultArtifactVersion> foundServletApiVersions = new TreeSet<>();
    for (DefaultExternalModuleDependency dependency : servletApiDependencies) {
      foundServletApiVersions.add(new DefaultArtifactVersion(dependency.getVersion()));
    }
    return foundServletApiVersions.last().toString();
  }

  private static URL toUrl(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new GradleException("Cannot compute URL for file");
    }
  }
}