package com.dice.doris.csai.entity

// TODO: sample code, the field can be null! please check!!!

data class VodPlayback(
    val dash: List<VideoInfo>,
    val hls: List<VideoInfo>,
)

data class VideoInfo(
    val subtitles: List<Subtitle>?,
    val url: String,
    val drm: Drm?
)

data class Subtitle(
    val format: String,
    val language: String,
    val url: String
)

data class Drm(
    val containerType: String,
    val encryptionMode: String,
    val jwtToken: String,
    val keySystems: List<String>,
    val url: String
)

// live
data class LivePlayback(
    val eventId: Int,
    val dash: VideoInfo?,
    val hls: VideoInfo?,
    var hlsUrl: String?
)