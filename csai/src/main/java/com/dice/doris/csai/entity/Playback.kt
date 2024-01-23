package com.dice.doris.csai.entity

data class VodPlayback(
    val annotations: Annotations,
    val dash: List<Dash>,
    val hls: List<Hl>,
    val skipMarkers: List<SkipMarker>,
    val smoothStreaming: List<SmoothStreaming>,
    val watermark: Watermark
)

data class Annotations(
    val thumbnails: String,
    val titles: String
)

data class Dash(
    val subtitles: List<Subtitle>,
    val url: String
)

data class Hl(
    val subtitles: List<Subtitle>,
    val url: String
)

data class SkipMarker(
    val skipMarkerType: String,
    val startTimeMs: Int,
    val stopTimeMs: Int
)

data class SmoothStreaming(
    val subtitles: List<Subtitle>,
    val url: String
)

data class Watermark(
    val display: Display,
    val imageUrl: String,
    val position: String
)

data class Subtitle(
    val format: String,
    val language: String,
    val url: String
)

data class Display(
    val dimension: String,
    val percentage: Double
)

// live
data class LivePlayback(
    val epg: Epg,
    val eventId: Int,
    val hls: Hls,
    val hlsUrl: String,
    val posterUrl: String,
    val startedAt: String,
    val statusCode: Int,
    val thirdPartyPlayback: List<Any>
)

data class Epg(
    val localisations: List<Localisation>
)

data class Hls(
    val containerType: String,
    val url: String
)

data class Localisation(
    val languageCode: String,
    val name: String,
    val updatedAt: String,
    val url: String
)