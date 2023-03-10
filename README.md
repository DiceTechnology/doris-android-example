# doris-android-example
A collection of sample apps to demonstrate the core features of ExoDoris.

### Setup
In order to resolve the `ExoDoris` dependency, you need to add your jitpack token to `settings.gradle`:
```
maven {
    url "https://jitpack.io"
    credentials { username authToken }
}
```
or add it to `$HOME/.gradle/gradle.properties`:
```
authToken=...
```

### Core features
* [Download To Go (D2G)](d2g/)

