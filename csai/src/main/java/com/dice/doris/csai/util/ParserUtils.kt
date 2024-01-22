package com.dice.doris.csai.util

import android.net.Uri
import com.dice.doris.csai.entity.Detail
import com.dice.doris.csai.entity.Playback
import com.diceplatform.doris.entity.ImaCsaiProperties
import com.diceplatform.doris.entity.TextTrack

object ParserUtils {
    fun parseTextTrack(playback: Playback): Array<TextTrack>? {
        return playback.hls.firstOrNull()?.subtitles
            ?.map { TextTrack(Uri.parse(it.url), it.language, it.language) }
            ?.toList()?.toTypedArray()
    }

    fun parseCsaiProperties(detail: Detail): ImaCsaiProperties? {
        val csaiAds = detail.adsConfiguration.adUnits.filter { it.insertionType.equals("CSAI", ignoreCase = true) }
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