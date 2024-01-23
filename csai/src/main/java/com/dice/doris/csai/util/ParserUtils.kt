package com.dice.doris.csai.util

import android.net.Uri
import com.dice.doris.csai.entity.AdsConfiguration
import com.dice.doris.csai.entity.VodDetail
import com.dice.doris.csai.entity.VodPlayback
import com.diceplatform.doris.entity.ImaCsaiProperties
import com.diceplatform.doris.entity.TextTrack
import com.google.ads.interactivemedia.v3.internal.it

object ParserUtils {
    fun parseTextTrack(playback: VodPlayback): Array<TextTrack>? {
        val subtitles = playback.hls.firstOrNull()?.subtitles ?: playback.dash.firstOrNull()?.subtitles
        return subtitles
            ?.map { TextTrack(Uri.parse(it.url), it.language, it.language) }
            ?.toList()?.toTypedArray()
    }

    fun parseCsaiProperties(adsConfiguration: AdsConfiguration): ImaCsaiProperties? {
        val csaiAds = adsConfiguration.adUnits.filter { it.insertionType.equals("CSAI", ignoreCase = true) }
        if (csaiAds.isEmpty()) return null
        var preRollAdTagUri: Uri? = null
        var midRollAdTagUri: Uri? = null
        csaiAds.forEach {
            val adType = it.adFormat
            if ("PREROLL".equals(adType, ignoreCase = true) || "VOD_VMAP".equals(adType, ignoreCase = true)) {
                preRollAdTagUri = Uri.parse(it.adTagUrl)
            } else if ("MIDROLL".equals(adType, ignoreCase = true)) {
                midRollAdTagUri = Uri.parse(it.adTagUrl)
            }
        }
        return ImaCsaiProperties.from(preRollAdTagUri, midRollAdTagUri, null)
    }
}