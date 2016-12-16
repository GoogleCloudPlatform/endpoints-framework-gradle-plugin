![project status image](https://img.shields.io/badge/stability-experimental-orange.svg)
# Endpoints Framework Gradle Plugin

This Gradle plugin provides tasks and configurations to build and connect Endpoints Framework projects.

# Requirements

[Gradle](http://gradle.org) is required to build and run the plugin.

# How to use

The plugin JAR needs to be defined in the classpath of your build script. Alternatively, you can download it from GitHub and deploy it to your local repository. The following code snippet shows an example on how to retrieve it from maven central :

```Groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.0-beta'
  }
}
```

###Server
In your Gradle App Engine Java app, add the following plugin to your build.gradle:

```Groovy
apply plugin: 'com.google.cloud.tools.endpoints-framework-server'
```

The plugin exposes the following server side goals
* `endpointsClientLibs` - generate client libraries
* `endpointsDiscoveryDocs` - generate discovery documents

The plugin exposes server side configuration through the `endpointsServer` extension
* `format` - The output format of discovery documents [rest or rpc]
* `serviceClasses` - List of service classes (optional), this can be inferred from web.xml

#### Usage
Make sure your web.xml is [configured to expose your endpoints](https://cloud.google.com/endpoints/docs/frameworks/java/required_files) correctly.

No configuration paramters are required to run with default values
```
$> gradle endpointsClientLibs
```
Client libraries will be written to `build/endpointsClientLibs`

```
$> gradle endpointsDiscoveryDocs
```
Discovery documents will be written to `build/endpointsDiscoveryDocs`


###Client
In your client Java app, add the following plugin to your build.gradle:

```Groovy
apply plugin: 'com.google.cloud.tools.endpoints-framework-client'
```

The plugin exposes **no tasks**. Applying the plugin will generate sources according
to your configuration

The plugin exposes client side configuration through the `endpointsClient` extension
* `discoveryDocs` - List of discovery docs to generate source from

The plugin exposes intermodule endpoints configuration through a custom dependency
* `endpointsServer` - Configure generation of source from another module in the project

#### Usage (from discovery docs)
In your build.gradle define the location of the discovery document

```
endpointsClient {
  discoveryDocs = ['src/endpoints/myApi-v1-rest.discovery']
}
```

building your project should inject the correct generated source into your compile path

#### Usage (from server module in project)
In your build.gradle define the correct project dependency, the server project must be
an `endpoints-framework-server` module for this to work.

```
dependencies {
  endpointsServer project(path: ":server", configuration: "endpoints")
}
```

building your project should inject the correct generated source into your compile path

You can use a combination of discovery doc files and server dependencies when building
a client module

## Contributing

If you wish to build this plugin from source, please see the [contributor instructions](CONTRIBUTING.md).
