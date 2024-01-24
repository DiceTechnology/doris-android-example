# CSAI
The CSAI feature allows you to play dice streams with CSAI ads. And in order to get customized ads, parameters are needed to be passed to backend.
This module contains a working example of this feature.

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
const val AUTH_NAME = "" // TODO: put username here
const val AUTH_PASSWORD = "" // TODO: put password here
const val BASE_URL = "" // TODO: put base url here
const val REALM = "" // TODO: put realm name here
const val API_KEY = "" // TODO: put api key here

const val CMP_TCF = ""  // TODO: put GDPR TCF string from Consent Provider here
const val CMP_USP = ""  // TODO: put CCPA us privacy string from Consent Provider here
const val SHOULD_TRACK_USER = false // TODO: set Should track flag from Consent Provider or device here
```

#### 1. Get your auth token first.

#### 2. Get video detail (sample code use okhttp client.)

- Client macros needed for customized ads:

|**Header Name**  | **Parameter** |
|--|--|
|CM-APP-BUNDLE|App bundle description|
|CM-APP-NAME| App name (From store)|
|CM-APP-STOREID|App id (From store)|
|CM-APP-VERSION|App version (our version)|
|CM-DVC-IFA|Advertising Identifier: AAID or IDFA|
|CM-DVC-LAT|Limited Ad Tracking|
|CM-DVC-DNT|Do Not Track header sent with the request|
|CM-DVC-H|Absolute height of the creative in device independent pixels (DIP or DP)|
|CM-DVC-W|Absolute width of the creative in device independent pixels (DIP or DP)|
|CM-DVC-LANG|Device Language in ISO-639-1-alpha-2|
|CM-DVC-MAKE|Manufacturer name|
|CM-DVC-MODEL|Device Model|
|CM-DVC-OS|Device OS [Must derive the OS code from this list](https://github.com/InteractiveAdvertisingBureau/AdCOM/blob/master/AdCOM%20v1.0%20FINAL.md#list_operatingsystems)|
|CM-DVC-OSV|Device OS version|
|CM-DVC-TYPE|Device Type [Must derive the device type from this list](https://github.com/InteractiveAdvertisingBureau/AdCOM/blob/master/AdCOM%20v1.0%20FINAL.md#list--device-types-)|
|CM-CST-TCF|Terms and Conditions Framework String. This is an encoded string used in GDPR compliant territories.|
|CM-CST-USP|US Privacy. This is the String containing information regarding the consent for CCPA in US territory.|

```kotlin
Request.Builder()
    .url(CsaiConfig.videoUrl)
    .header(CsaiHeader.AUTH, "Bearer $authToken") // your auth token
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
    .header(CsaiHeader.DVC_OS, "2") // 2: android
    .header(CsaiHeader.DVC_OSV, Build.VERSION.SDK_INT.toString()) // OS version
    .header(CsaiHeader.DVC_TYPE, if (isPad(context)) "5" else "4") // 5:tablet, 4:phone
    .header(CsaiHeader.DVC_MAKE, Build.MANUFACTURER)
    .header(CsaiHeader.DVC_MODEL, Build.MODEL)
    .header(CsaiHeader.CST_TCF, CsaiConfig.CMP_TCF) // GDPR TCF string
    .header(CsaiHeader.CST_USP, CsaiConfig.CMP_USP) // CCPA us privacy string
    .build()
```

#### 3. Get video stream feed from request of 'playerUrlCallback' in the returned video detail.

#### 4. Get playback url
Choose the stream in the response.

#### 5. Parse `adsConfiguration` to get CSAI ad tag url.
```kotlin
val imaCsaiProperties = parseCsaiProperties(adsConfiguration)
```

#### 6. Create player
```kotlin
val player = createPlayer(imaCsaiProperties)
```

#### 7. init doris player and play
```kotlin
val src = SourceBuilder()
    .setId(CsaiConfig.videoId) // video id
    .setUrl(videoInfo.url) // video url
    .setDrmParams(videoInfo.drm?.let { drm -> ActionToken(CsaiConfig.videoId, videoInfo.url, drm.url, drm.jwtToken, "widevine") }) // drm config, you can get in the stream
    .setImaCsaiProperties(imaCsaiProperties) // ima csai config, you can get in detail feed.
    .setTextTracks(parseTextTrack(videoInfo)) // subtitles, you can in stream feed
    .build()

player.setLisener(...)
...
player.load(src) // start video playback
```
