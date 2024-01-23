package com.dice.doris.csai


object CsaiConfig {
    const val BASE_URL = "https://dce-frontoffice-stag.imggaming.com/api"
    const val REALM = "dce.sandbox"
    const val API_KEY = "4dc1e8df-5869-41ea-95c2-6f04c67459ed"
    const val CMP_TCF = ""
    const val CMP_USP = ""
    private const val VOD_CONTENT_ID = "85158"
    private const val LIVE_CONTENT_ID = "111449"

    var isLive = false

    val videoUrl: String
        get() {
            return if (isLive) {
                "${BASE_URL}/v4/event/${LIVE_CONTENT_ID}?includePlaybackDetails=URL&displayGeoblockedLive=false"
            } else {
                "${BASE_URL}/v4/vod/${VOD_CONTENT_ID}?includePlaybackDetails=URL"
            }
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
