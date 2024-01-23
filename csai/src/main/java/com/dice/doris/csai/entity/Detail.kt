package com.dice.doris.csai.entity

//TODO: sample code, the field can be null! please check!!!
//TODO: sample code, the field can be null! please check!!!
//TODO: sample code, the field can be null! please check!!!
data class VodDetail(
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

// live
data class LiveDetail(
    val accessLevel: String,
    val adsConfiguration: AdsConfiguration,
    val audioOnly: Boolean,
    val audioTracks: List<Any>,
    val availableLicences: List<Any>,
    val closedCaption: Boolean,
    val closedCaptionLanguages: List<Any>,
    val description: String,
    val endDate: Long,
    val externalDataProvider: ExternalDataProvider,
    val externalId: String,
    val favouriteChannel: Boolean,
    val id: Int,
    val live: Boolean,
    val localBroadcasters: List<Any>,
    val location: String,
    val multiAudio: Boolean,
    val multiAudioLanguages: List<String>,
    val playerUrlCallback: String,
    val plugins: List<Plugin>,
    val programmingInfo: ProgrammingInfo,
    val startDate: Long,
    val subEvents: List<Any>,
    val thumbnailUrl: String,
    val title: String,
    val type: String
)

data class ExternalDataProvider(
    val id: String,
    val provider: String
)

data class ProgrammingInfo(
    val channelId: Int,
    val channelLogoUrl: String,
    val currentProgramme: CurrentProgramme,
    val nextProgramme: NextProgramme,
    val secondaryChannelLogoUrl: String
)

data class Configuration(
    val customProgramId: String
)

data class CurrentProgramme(
    val daiKeyValues: DaiKeyValues,
    val duration: Int,
    val endDate: String,
    val episode: String,
    val id: Int,
    val startDate: String,
    val thumbnailUrl: String
)

data class NextProgramme(
    val daiKeyValues: DaiKeyValues,
    val duration: Int,
    val endDate: String,
    val episode: String,
    val id: Int,
    val startDate: String,
    val thumbnailUrl: String
)

class DaiKeyValues