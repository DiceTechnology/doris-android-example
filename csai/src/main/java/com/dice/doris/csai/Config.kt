package com.dice.doris.csai


object CsaiConfig {
    const val AUTH_NAME = "" // TODO: put username here
    const val AUTH_PASSWORD = "" // TODO: put password here
    const val BASE_URL = "https://dce-frontoffice-stag.imggaming.com" // TODO: put base url here
    const val REALM = "" // TODO: put realm name here
    const val API_KEY = "" // TODO: put api key here

    const val CMP_TCF = ""  // TODO: put GDPR TCF string from Consent Provider here
    const val CMP_USP = ""  // TODO: put CCPA us privacy string from Consent Provider here
    const val SHOULD_TRACK_USER = false // TODO: set Should track flag from Consent Provider or device here

    const val videoId = ""
    const val isLive = false // VOD: false, Live: true

    val videoUrl: String
        get() {
            return "${BASE_URL}/api/v4/${if (isLive) "event" else "vod"}/${videoId}?includePlaybackDetails=URL"
        }
}

object CsaiHeader {
    const val AUTH = "authorization"
    const val API_KEY = "x-api-key"
    const val REALM = "realm"
    const val CONTENT_TYPE = "content-type"

    const val APP_NAME = "CM-APP-NAME"
    const val APP_VERSION = "CM-APP-VERSION"
    const val APP_BUNDLE = "CM-APP-BUNDLE"
    const val APP_STOREID = "CM-APP-STOREID"

    const val DVC_DNT = "CM-DVC-DNT"
    const val DVC_H = "CM-DVC-H"
    const val DVC_W = "CM-DVC-W"
    const val DVC_LANG = "CM-DVC-LANG"
    const val DVC_OS = "CM-DVC-OS"
    const val DVC_OSV = "CM-DVC-OSV"
    const val DVC_TYPE = "CM-DVC-TYPE"
    const val DVC_MAKE = "CM-DVC-MAKE"
    const val DVC_MODEL = "CM-DVC-MODEL"

    const val CST_TCF = "CM-CST-TCF"
    const val CST_USP = "CM-CST-USP"
}
