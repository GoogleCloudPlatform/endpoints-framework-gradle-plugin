![project status image](https://img.shields.io/badge/stability-experimental-orange.svg)
# Endpoints Framework Gradle Plugin

This Gradle plugin provides tasks and configurations to build and connect Endpoints Framework projects.

# Requirements

[Gradle](http://gradle.org) is required to build and run the plugin.

# How to use

The plugin JAR needs to be defined in the classpath of your build script. It is directly available on Maven Central. Alternatively, you can download it from GitHub and deploy it to your local repository. The following code snippet shows an example on how to retrieve it from Maven Central:

```Groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:0.1.0'
  }
}
```

###Server
In your Gradle App Engine Java app, add the following plugin to your build.gradle:

```Groovy
apply plugin: 'com.google.cloud.tools.endpoints-framework-server'
```

###Client (optional)
In your client Java app, add the following plugin to your build.gradle:

```Groovy
apply plugin: 'com.google.cloud.tools.endpoints-framework-client'
```

You can now use the endpoints gradle plugin in your client and server projects.

## Contributing

If you wish to build this plugin from source, please see the [contributor instructions](CONTRIBUTING.md).
