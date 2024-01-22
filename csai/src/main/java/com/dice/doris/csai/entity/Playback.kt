package com.dice.doris.csai.entity

data class Playback(
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