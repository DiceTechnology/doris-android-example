package com.dice.doris.csai.entity

data class Detail(
    val accessLevel: String,
    val adsConfiguration: AdsConfiguration,
    val categories: List<String>,
    val contentDownload: ContentDownload,
    val coverUrl: String,
    val description: String,
    val displayableTags: List<Any>,
    val duration: Int,
    val externalAssetId: String,
    val favourite: Boolean,
    val id: Int,
    val licences: List<Any>,
    val longDescription: String,
    val maxHeight: Int,
    val offlinePlaybackLanguages: List<Any>,
    val playerUrlCallback: String,
    val plugins: List<Plugin>,
    val subEvents: List<Any>,
    val thumbnailUrl: String,
    val thumbnailsPreview: String,
    val title: String,
    val type: String,
    val watchStatus: String
)

data class AdsConfiguration(
    val adUnits: List<AdUnit>
)

data class ContentDownload(
    val permission: String
)

data class Plugin(
    val configuration: Configuration,
    val id: Int,
    val name: String,
    val payload: String,
    val subtype: String,
    val type: String
)

data class AdUnit(
    val adFormat: String,
    val adTagUrl: String,
    val insertionType: String
)

class Configuration