![project status image](https://img.shields.io/badge/stability-experimental-orange.svg)
# Transitioning Android Projects

Moving legacy projects from Android Studio requires a few extra steps. This will guide you through
that process

Make sure you have the cloud SDK installed (see the [app-gradle-plugin](https://github.com/GoogleCloudPlatform/app-gradle-plugin) for more)

This guide starts from an existing android studio project with a cloud backend.
The expected structure is
```
<project>
├── app
├── backend
└── build.gradle
```

Make changes in the backend/appengine module

**`<project>/backend/build.gradle`**

Remove the old appengine plugin jar and add in the new appengine and endpoints jars
```gradle
buildscript {
  ...
  dependencies {
    // delete this
    // classpath 'com.google.appengine:gradle-appengine-plugin:1.9.51'
      
    // add these 
    classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.2'
    classpath 'com.google.cloud.tools:appengine-gradle-plugin:1.3.3'
  }
}
```

Remove the old plugin and apply the new ones
```gradle
// delete this
// apply plugin: 'appengine'

// add these
apply plugin: 'com.google.cloud.tools.appengine'
apply plugin: 'com.google.cloud.tools.endpoints-framework-server'
```

Remove all the old appengine and endpoints dependencies and add the 
new endpoints dependencies
```gradle
dependencies {
  // delete these, we are using the cloud SDK and new endpoints tooling now
  // appengineSdk 'com.google.appengine:appengine-java-sdk:1.9.51'
  // compile 'com.google.appengine:appengine-endpoints:1.9.51'
  // compile 'com.google.appengine:appengine-endpoints-deps:1.9.51'
  ... 
  // add these (inject needs to be explicitly included now)
  compile 'com.google.endpoints:endpoints-framework:2.0.7'
  compile 'javax.inject:javax.inject:1'
  ...
}

...
// delete this whole block, it's configuration for the older plugin
// appengine {
//   downloadSdk = true
//   appcfg {
//     oauth2 = true
//   }
//   endpoints {
//     getClientLibsOnBuild = true
//     getDiscoveryDocsOnBuild = true
//   }
// }
```

Make changes in the android client 

**`<project>/app/build.gradle`**

Add in the new endpoints jar
```gradle
buildscript {
  ...
  dependencies {
    // add this
    classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.2'
  }
}
```

Apply the endpoints plugin **after** the android plugin
```gradle
apply plugin: 'com.android.application'
// add this
apply plugin: 'com.google.cloud.tools.endpoints-framework-client'
```
Use the new endpointsServer dependency and remove the old compile dependency,
also explicitly add in the google-api-client dependency
```gradle
dependencies {
   ...
   // remove this
   // compile project(path: ':backend', configuration: 'android-endpoints')
   
   // add these
   endpointsServer project(path: ':backend', configuration: 'endpoints')
   compile "com.google.api-client:google-api-client:+"
}
```

If you have a particularly complicated buildscript classpath bringing in a lot of dependencies with cryptic error messages like
```
Execution failed for task ':{project-name}:endpointsDiscoveryDocs'.
> com.google.common.reflect.TypeToken.isSubtypeOf(Ljava/lang/reflect/Type;)Z
```
```
Execution failed for task ':backend:endpointsDiscoveryDocs'.
        > com.fasterxml.jackson.core.JsonFactory.requiresPropertyOrdering()Z
```
```
java.lang.NoClassDefFoundError: Could not initialize class com.google.api.server.spi.tools.GenClientLibAction
```
See [45](https://github.com/GoogleCloudPlatform/endpoints-framework-gradle-plugin/issues/45) or [52](https://github.com/GoogleCloudPlatform/endpoints-framework-gradle-plugin/issues/52) for more details. 
These are consequences of classpath resolution in multimodule builds (see: [gradle forums](https://discuss.gradle.org/t/version-is-root-build-gradle-buildscript-is-overriding-subproject-buildscript-dependency-versions/20746/2)).

In this case, the best option is take the advice from the gradle forums and move all the buildscript classpath imports out of `<project>/backend/build.gradle` into the root build file at `<project>/build.gradle`. This helps gradle handle versions of buildscript dependencies much better.

Make changes in the root, backend and android project.

**`<project>/build.gradle`**

```gradle
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'com.android.tools.build:gradle:2.2.2'
    
    // add this
    classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.2'
    classpath 'com.google.cloud.tools:appengine-gradle-plugin:1.3.3'
  }
}
```

**`<project>/backend/build.gradle`**

```gradle
// delete this whole buildscript block
// buildscript {
//  ...
//  dependencies {      
    // add these 
//    classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.2'
//    classpath 'com.google.cloud.tools:appengine-gradle-plugin:1.3.3'
//  }
// }
```

**`<project>/app/build.gradle`**

The endpoints jar is now brought in by the root, so remove it from here.
```gradle
buildscript {
  ...
  dependencies {
    // remove this
    // classpath 'com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.2'
  }
}
```


## Android Studio
Android Studio's App Engine tooling will no long *Gradle Sync* with these plugins, and while things may continue to work on stale configuration, it's not safe to depend on it to always work.

### Run
In Android Studio, you need to run the local development server using the gradle task `appengineStart` which starts the development server in non-blocking mode, output will be written to a file which you can monitor. It is not recommended to use `appengienRun` from within Android Studio. If you use `appengineRun` you may block Android Studio from using the gradle daemon to launch any gradle further related tasks.

### Deploy
For deploy, you must first [login using glcoud](https://cloud.google.com/sdk/gcloud/reference/auth/login) and then deploy using the gradle task `appengineDeploy`
```
$ ./gradlew appengineDeploy
```
