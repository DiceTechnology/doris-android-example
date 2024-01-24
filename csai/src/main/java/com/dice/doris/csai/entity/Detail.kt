package com.dice.doris.csai.entity

//TODO: sample code, the field can be null! please check!!!
data class VodDetail(
    val playerUrlCallback: String?,
    val adsConfiguration: AdsConfiguration?,
)

data class AdsConfiguration(
    val adUnits: List<AdUnit>?
)

data class AdUnit(
    val adFormat: String?,
    val adTagUrl: String?,
    val insertionType: String?
)
