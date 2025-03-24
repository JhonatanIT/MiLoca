This is a Kotlin Multiplatform project targeting Android, Web, Desktop.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [GitHub](https://github.com/JetBrains/compose-multiplatform/issues).

You can open the web application by running the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task.

## Troubleshooting

- Execute ./gradlew kotlinUpgradeYarnLock to update the yarn.lock file when run Web application.

## Gradle secrets properties

To use secrets in your project, you can create a `secrets.properties` file in the root of the project with the following content (replace `YOUR_MAPS_API_KEY` with your actual key):
```
MAPS_API_KEY=YOUR_MAPS_API_KEY
```

Create a `secrets.default.properties` file in the root of the project with the following content:
```
MAPS_API_KEY=DEFAULT_API_KEY
```

In the project's root `build.gradle.kts` file, add the following code to read the secrets:
```
plugins {
    ...
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}
``` 

In the app-level `build.gradle.kts` project file, add the following code to read the secrets:
```
  plugins {
    ...
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
  }

  dependencies {
    ...
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
  }

  secrets {
    // Change the properties file from the default "local.properties" in your root project
    // to another properties file in your root project.
    // To add your Maps API key to this project:
    // 1. If the secrets.properties file does not exist, create it in the same folder as the local.properties file.
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be checked in version
    // control.
    defaultPropertiesFileName = "secrets.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default.
    ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
  }
```

In the AndroidManifest.xml file, add the following code to read the secrets:
```
<application
  ....
  <meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}" />
</application>
```

Reference: [Gradle Secrets Plugin](https://github.com/google/secrets-gradle-plugin)

## KSP - Kotlin Symbol Processing

KSP is a tool for Kotlin developers that allows you to create custom annotations and process them at compile time. It is similar to Java's annotation processing but is designed specifically for Kotlin.

Reference: [Migrate from kapt to KSP](https://developer.android.com/build/migrate-to-ksp#add-ksp) 