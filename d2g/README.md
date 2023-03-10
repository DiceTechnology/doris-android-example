# Download To Go (D2G)
The Download To Go feature allows you to download VODs and play them back offline. This module contains a working example of this feature.

## Setup
The following section describes how to setup the dependencies.
### Permission
Add the following permission to `AndroidManifest.xml`:
```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
```
### Dependencies
Add the following repositories to the project level `build.gradle`:
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
Add the following dependencies to the module level `build.gradle`.

In case of latest `ExoDoris` version (2.2.13+), only the `ExoDoris` dependency needs to be added:
```groovy
implementation "com.github.DiceTechnology.doris-android:doris:$dorisVersion"
implementation "com.github.DiceTechnology.doris-android:doris-ui:$dorisVersion"
```
For older `ExoDoris` versions, the `dice-shield-android` (0.18.2) also needs to be added:
```groovy
implementation ("com.github.DiceTechnology.doris-android:doris:$dorisVersion") {
    exclude group: 'com.github.DiceTechnology', module: 'dice-shield-android'
}
implementation ("com.github.DiceTechnology.doris-android:doris-ui:$dorisVersion") {
    exclude group: 'com.github.DiceTechnology', module: 'dice-shield-android'
}
implementation("com.github.DiceTechnology:dice-shield-android:0.18.2") {
    exclude group: "com.google.android.exoplayer", module: "exoplayer"
}
```
RxJava dependencies:
```groovy
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
```

### How to use it
#### Configuration
Add the proper values to these constants in `Config.java`:
```java
BASE_URL   = "https://dce-frontoffice.imggaming.com";
DEVICE_ID  = ""; // Persisted unique identifier of the current device.
REALM      = ""; // Realm (e.g. dce.mwh)
API_KEY    = ""; // Your API key
AUTH_TOKEN = ""; // Your authorization token
```
#### DownloadProvider
The `DownloadProvider` encapsulates all the functionality of the D2G feature. As a first step, we need to configure it by calling `setup(..)` and `setToken(..)` on it. The `setToken` method has a callback which will be called whenever the token is expired, in this case, we need to call `setToken` again with a valid token.

```java
/**
 * Initialize API keys that are required to renew License.
 *
 * @param api      Url to API backend.
 * @param realm    The application realm.
 * @param key      The API-key.
 * @param deviceId The persisted id of current device.
 * @param headers  Set default request headers.
 */
void setup(
    @NonNull String api, 
    @NonNull String realm, 
    @NonNull String key, 
    @NonNull String deviceId, 
    @Nullable Map<String, String> headers);
```

```java
/**
 * Set the API token that is required to renew License and request Media information.
 *
 * @param token    Actual API token.
 * @param callback that will be called if the API token expired. 
                    You should provide a new token calling this method again.
 */
void setToken(String token, Callback callback);
```
#### Add a download
```java
/**
 * Adds download to the download queue.
 *
 * @param id                     VOD id to download.
 * @param quality                Rendition quality to download.
 * @param language               Language to download.
 * @param extraData              Any extra data to store (e.g. in a JSON).
 * @param drmScheme              The DRM scheme.
 * @param title                  Video title to store in downloads.
 * @param images                 Map of thumbnail images to download, where the key is an id
 *                               and value is a URL.
 * @param externalSubtitleFormat Format of external subtitles to download if available.
 *                               Could be "srt", "vtt" or null to ignore.
 * @return Observable<Ok>
 */
Observable<Ok> addDownload(
    @NonNull String id, 
    @NonNull VideoQuality quality, 
    @NonNull Locale language, 
    @NonNull String drmScheme, 
    @Nullable String title, 
    @Nullable String extraData, 
    @Nullable Map<String, String> images, 
    @Nullable String externalSubtitleFormat);
```
#### Get back the downloads
```java
/**
 * Returns list of all downloads.
 *
 * @return Observable of a list of {@link AssetData}.
 */
Observable<List<AssetData>> getAllDownloads();

/**
 * Returns the {@link AssetData} for the specified download id.
 *
 * @param id The download id.
 * @return Observable of {@link AssetData}.
 */
Observable<AssetData> getDownload(String id);

/**
 * Returns the downloads for specified download state
 *
 * @param state The download state of download to be returned, see {@link DownloadState}.
 * @return Observable of a list of {@link AssetData}.
 */
Observable<List<AssetData>> getDownloads(DownloadState state);
```
#### Listen for download state changes
```java
/**
 * Returns the observable which emits download progress updates.
 *
 * @return Observable<DownloadUpdateInfo>.
 */
Observable<DownloadUpdateInfo> getDownloadsObservable();
```
The `DownloadUpdateInfo` contains the id of the asset which belongs to, the current state and the progress of the download.
#### Pause downloads
```java
/**
 * Pause downloading the asset with the given id.
 *
 * @param id the id of the asset to be paused.
 * @return Observable<Ok>.
 */
Observable<Ok> pauseDownload(String id);

/**
 * Pause all unfinished downloads.
 *
 * @return Observable<Ok>
 */
Observable<Ok> pauseAllDownloads();
```
Note. you need to subscribe to these actions to be executed, see `MainActivity.java` for example.

#### Resume downloads
```java
/**
 * Resume downloading the asset with the given id.
 *
 * @param id the id of the asset to be resumed.
 * @return Observable<Ok>.
 */
Observable<Ok> resumeDownload(String id);

/**
 * Resume all unfinished downloads.
 *
 * @return Observable<Ok>.
 */
Observable<Ok> resumeAllDownloads();
```
Note. you need to subscribe to these actions to be executed, see `MainActivity.java` for example.

#### Remove or cancel downloads
```java
/**
 * Remove the asset with the given id.
 *
 * @param id the id of the asset to be removed.
 * @return Observable<Ok>.
 */
Observable<Ok> removeDownload(String id);

/**
 * Cancel and remove all unfinished downloads.
 *
 * @return Observable<Ok>.
 */
Observable<Ok> cancelAllDownloads();
```
Note. you need to subscribe to these actions to be executed, see `MainActivity.java` for example.

#### Renew license
Whenever an asset is downloaded, it comes with a license which is valid for only a certain time. After this license expires, the asset can't be played back. In this case, the license needs to be renewed. In the sample app, we are checking whether any of the licenses are expired whenever we list out the downloads for the first time, see `MainActivity.java`.

```java
 /**
 * Renew license for the asset with the given id.
 *
 * @param id The id of the asset to renew its license.
 * @return Observable<Ok>.
 */
Observable<Ok> renewLicense(@NonNull String id);
```

#### WiFi-only downloads
```java
/**
 * Indicate that only WiFi networks should be used for downloads.
 *
 * @param wiFiOnlyDownloads if set to true downloads will work on WiFi networks only.
 */
void setWiFiOnlyDownloads(boolean wiFiOnlyDownloads);
```

### Offline playback of a downloaded asset
```java
ExoDoris player = new ExoDorisBuilder(this)
        .setPlayWhenReady(true)
        .build();
MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
        .setUri(videoItem.getUrl());
Source source = new SourceBuilder()
        .setMediaItem(mediaItemBuilder.build())
        .setId(videoItem.getId())
        .setShouldPlayOffline(true)
        .setDrmParams(new ActionToken(Utils.DRM_SCHEME, videoItem.getOfflineLicense()))
        .setOfflineLicense(videoItem.getOfflineLicense())
        .build();
player.load(source);
```


