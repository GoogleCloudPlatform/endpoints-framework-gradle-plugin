![project status image](https://img.shields.io/badge/stability-stable-brightgreen.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.google.cloud.tools/endpoints-framework-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.google.cloud.tools/endpoints-framework-gradle-plugin)
# Endpoints Framework Gradle Plugin

This Gradle plugin provides tasks and configurations to build and connect Endpoints Framework projects.

Android Studio users see [transition guide](ANDROID_README.md).

# Requirements

[Gradle](http://gradle.org) is required to build and run the plugin. The table below shows the compatibility with Gradle version.

<!-- TODO: Remove "(not yet released)" once we release a new version -->

| Gradle version | endpoints-framework-gradle-plugin version |
| ------------- | ------------- |
| 7.x - | 2.2.0 or higher (not yet released) |
| 4.x - 6.x | 2.1.0 or lower |

# How to use

The plugin JAR needs to be defined in the classpath of your build script. Alternatively, you can download it from GitHub and deploy it to your local repository. The following code snippet shows an example on how to retrieve it from maven central :

```Groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.2'
  }
}
```

### Server
In your Gradle App Engine Java app, add the following plugin to your build.gradle:

```Groovy
apply plugin: 'com.google.cloud.tools.endpoints-framework-server'
```

The plugin exposes the following server side goals
* `endpointsClientLibs` - generate client libraries
* `endpointsDiscoveryDocs` - generate discovery documents
* `endpointsOpenApiDocs` - generate Open Api documents

The plugin exposes server side configuration through the `endpointsServer` extension
* `serviceClasses` - List of service classes (optional), this can be inferred from web.xml
* `clientLibDir` - Output directory for generated client libraries
* `hostname` - To set the root url for discovery docs and client libs (ex: `hostname = myapp.appspot.com` will result in a default root url of `https://myapp.appspot.com/_ah/api`)

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


### Client
In your client Java app, add the following plugin to your build.gradle:

```Groovy
// apply this plugin after you have applied other plugins
// because it uses the state of other plugins
apply plugin: 'com.google.cloud.tools.endpoints-framework-client'
```

The plugin exposes **no tasks**. Applying the plugin will generate sources according
to your configuration

The plugin exposes client side configuration through the `endpointsClient` extension
* `discoveryDocs` - List of discovery docs to generate source from

The plugin exposes intermodule endpoints configuration through a custom dependency
* `endpointsServer` - Configure generation of source from another module in the project

#### Usage (from discovery docs)
In your build.gradle define the location of the discovery document in the
`endpointsClient` configuration closure.

```Groovy
endpointsClient {
  discoveryDocs = ['src/endpoints/myApi-v1-rest.discovery']
}
```

building your project should inject the correct generated source into your compile path.

#### Usage (from server module in project)
In your build.gradle define the correct project dependency, the server project must be
an `endpoints-framework-server` module for this to work.

```Groovy
dependencies {
  endpointsServer project(path: ":server", configuration: "endpoints")
}
```

building your project should inject the correct generated source into your compile path.

You can use a combination of discovery doc files and server dependencies when building
a client module, make sure you include all the necessary dependencies for building your
endpoints client

```Groovy
dependencies {
  compile 'com.google.api-client:google-api-client:<version>' // for standard java projects
  compile 'com.google.api-client:google-api-client-android:<version>' exclude module: 'httpclient' // for android projects
}
```

## Contributing

If you wish to build this plugin from source, please see the [contributor instructions](CONTRIBUTING.md).
