# Csai demo

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

### Dependencies
```groovy
implementation("com.github.DiceTechnology.doris-android:doris:$dorisVersion") {
    exclude group: 'androidx.media3'
}
implementation("com.github.DiceTechnology.doris-android:doris-ui:$dorisVersion") {
    exclude group: 'androidx.media3'
}
implementation("com.github.DiceTechnology.doris-android:extension-ima-csai:$dorisVersion") {
     exclude group: 'androidx.media3'
}
implementation("com.github.DiceTechnology.doris-android:extension-ima-csai-live:$dorisVersion") {
    exclude group: 'androidx.media3'
}
```

### How to use it
#### Configuration
Add the proper values to these constants in `CsaiConfig.kt`:
```kotlin
const val AUTH_NAME = "" //TODO: try to get auth token
const val AUTH_PASSWORD = "" //TODO: try to get auth token
const val BASE_URL = "https://dce-frontoffice-stag.imggaming.com/api"
const val REALM = "dce.sandbox" // sample
const val API_KEY = "4dc1e8df-5869-41ea-95c2-6f04c67459ed" // android
const val CMP_TCF = ""  // GDPR TCF string
const val CMP_USP = ""  // CCPA us privacy string
const val SHOULD_TRACK_USER = false // like one trust track.
```

[1] get your auth token first.

[2] get video detail feed, sample code use okhttp client.
```kotlin
Request.Builder()
    .url(CsaiConfig.videoUrl)
    .header(CsaiHeader.AUTH, "Bearer $authToken") //your auth token, you should set value in CSAIConfig.AUTH_NAME
    .header(CsaiHeader.API_KEY, CsaiConfig.API_KEY)
    .header(CsaiHeader.REALM, CsaiConfig.REALM)
    .header(CsaiHeader.CONTENT_TYPE, "application/json")
    .header(CsaiHeader.APP_NAME, context.getString(context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.labelRes))
    .header(CsaiHeader.APP_VERSION, context.packageManager.getPackageInfo(context.packageName, 0).versionName)
    .header(CsaiHeader.APP_BUNDLE, context.packageName)
    .header(CsaiHeader.APP_STOREID, context.packageName)
    .header(CsaiHeader.DVC_DNT, if (CsaiConfig.SHOULD_TRACK_USER) "0" else "1")
    .header(CsaiHeader.DVC_H, context.resources.displayMetrics.heightPixels.toString())
    .header(CsaiHeader.DVC_W, context.resources.displayMetrics.widthPixels.toString())
    .header(CsaiHeader.DVC_LANG, Locale.getDefault().language) // language
    .header(CsaiHeader.DVC_OS, "2") // 2:android
    .header(CsaiHeader.DVC_OSV, Build.VERSION.SDK_INT.toString()) // OS version
    .header(CsaiHeader.DVC_TYPE, if (isPad(context)) "5" else "4") // 5:tablet, 4:phone
    .header(CsaiHeader.DVC_MAKE, Build.MANUFACTURER)
    .header(CsaiHeader.DVC_MODEL, Build.MODEL)
    .header(CsaiHeader.CST_TCF, CsaiConfig.CMP_TCF) // GDPR TCF string
    .header(CsaiHeader.CST_USP, CsaiConfig.CMP_USP) // CCPA us privacy string
    .build()
```

[3] get video stream feed, you can get 'playerUrlCallback' in detail feed.
```kotlin
Request.Builder()
    .url(url)
    .build()
```

[4] init doris player and play, you can the param in detail and stream feed.
```kotlin
val imaCsaiProperties = parseCsaiProperties(adsConfiguration)
val src = SourceBuilder()
    .setId(CsaiConfig.videoId)  // video id
    .setUrl(videoInfo.url)      // video url
    .setDrmParams(videoInfo.drm?.let { drm -> ActionToken(CsaiConfig.videoId, videoInfo.url, drm.url, drm.jwtToken, "widevine") })              // drm config, you can get in stream feed
    .setImaCsaiProperties(imaCsaiProperties)    // ima csai config, you can get in detail feed.
    .setTextTracks(parseTextTrack(videoInfo))   // subtitles, you can in stream feed
    .build()
val player = createPlayer(imaCsaiProperties)
player.setLisener(...)
...
player.load(src) // start video
```
```kotlin
private fun createPlayer(...): ExoDoris {
val dorisBuilder = if (preRollAdTag) {
    ExoDorisImaCsaiBuilder(context) // preRollAd
} else if (midRollAdTag) {
    ExoDorisImaCsaiLiveBuilder(context) // midRollAd
} else {
    ExoDorisBuilder(context) // normal
}
return dorisBuilder.build()
}
```

[5] print log to find problems
```groovy
dependencies {
    implementation("com.squareup.okhttp3:logging-interceptor:4.x")
}
```
```kotlin
OkHttpClient
    .Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    })
    build()
```
you can see all http `request` and `response` information in AndroidStudio logcat.
