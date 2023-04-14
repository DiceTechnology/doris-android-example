# Chromecast example
This module contains a simple example that demonstrates how to configure chromecast.

## Configuration
In order to make it work, all the missing values in `Constants.java` needs to be filled - which is realm specific.

```java
public static final String receiverId = "";
public static final String title = "";
public static final String videoId = "";
public static final String authorisationToken = "";
public static final String refreshToken = "";
public static final String realm = "";
public static final String cid = UUID.randomUUID().toString();
public static final String baseUrl = "https://dce-frontoffice.imggaming.com/api/v2";
public static final String endpoint = "https://guide.imggaming.com/prod";
```

### Dependencies
Add the following dependencies:

```groovy
dependencies {
    implementation 'com.google.android.gms:play-services-cast-framework:21.2.0'
    implementation 'androidx.mediarouter:mediarouter:1.3.1'
}
```