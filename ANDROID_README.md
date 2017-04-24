![project status image](https://img.shields.io/badge/stability-experimental-orange.svg)
# Transitioning Android Projects

Moving legacy projects from Android Studio requires a few extra steps. This will guide you through
that process

Make sure you have the cloud SDK installed (see the app-gradle-plugin for more)

This guide starts from a basic android studio project with a cloud backend
File -> New Project (click through, using defaults)
File -> New Module -> Google Cloud Module  (click through, using defaults)


Make changes in the backend/appengine module

**`<project>/backend/build.gradle`**

Remove the old appengine plugin jar and add in the new appengine and endpoints jars
```gradle
buildscript {
  ...
  dependencies {
    // delete this
    // classpath 'com.google.appengine:gradle-appengine-plugin:1.9.48'
      
    // add these 
    classpath "com.google.cloud.tools:endpoints-framework-gradle-plugin:1.0.7-beta"
    classpath 'com.google.cloud.tools:appengine-gradle-plugin:1.3.0'
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
  // appengineSdk 'com.google.appengine:appengine-java-sdk:1.9.48'
  // compile 'com.google.appengine:appengine-endpoints:1.9.48'
  // compile 'com.google.appengine:appengine-endpoints-deps:1.9.48'
  ... 
  // add these (inject needs to be explicitly included now)
  compile 'com.google.endpoints:endpoints-framework:2.0.5'
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
    classpath "com.google.cloud.tools:endpoints-framework-gradle-plugin:1.3.0"
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


Finally, if you see this, to deal with a buildscript classpath issue (see: [gradle forums](https://discuss.gradle.org/t/version-is-root-build-gradle-buildscript-is-overriding-subproject-buildscript-dependency-versions/20746/2))
Make changes in the root project

**`<project>/build.gradle`**

```gradle
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    // add this
    classpath 'com.google.guava:guava:19.0'
    
    classpath 'com.android.tools.build:gradle:2.2.2'
  }
}
```

