# doris-android-example
A collection of sample apps to demonstrate the core features of ExoDoris.

### Setup
In order to resolve the `ExoDoris` dependency, add the following to `settings.gradle`:

**For version `3.12.0` and higher**
```groovy
repositories {
    google()
    mavenCentral()
    maven {
        url("https://d1yvb7bfbv0w4t.cloudfront.net/")
        credentials { username authToken }
    }
    maven {
        url "https://muxinc.jfrog.io/artifactory/default-maven-release-local"
    }
}
```

In module-level `build.gradle:`
```groovy
implementation("com.endeavorstreaming.doris:doris:$dorisVersion") {
    exclude group: 'androidx.media3'
}
implementation("com.endeavorstreaming.doris:doris-ui:$dorisVersion") {
    exclude group: 'androidx.media3'
}
```

**For older versions**
```groovy
repositories {
    google()
    mavenCentral()
    maven {
        url "https://jitpack.io"
        credentials { username authToken }
    }
    maven {
        url "https://muxinc.jfrog.io/artifactory/default-maven-release-local"
    }
}
```
In module-level `build.gradle:`
```groovy
implementation("com.github.DiceTechnology.doris-android:doris:$dorisVersion") {
    exclude group: 'androidx.media3'
}
implementation("com.github.DiceTechnology.doris-android:doris-ui:$dorisVersion") {
    exclude group: 'androidx.media3'
}
```

Add your **Jitpack token** (version < `3.12.0`), or **ES Maven token** (>= `3.12.0`) to `$HOME/.gradle/gradle.properties`:
```
authToken=...
```

### Core features
* [Download To Go (D2G)](d2g/)
* [Chromecast configuration](chromecast/)
* [Video playback](video/)
* [CSAI](csai/)

