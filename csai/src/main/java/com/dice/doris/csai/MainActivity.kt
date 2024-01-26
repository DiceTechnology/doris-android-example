package com.dice.doris.csai

import android.net.Uri
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.exoplayer.util.EventLogger
import com.dice.doris.csai.entity.AdsConfiguration
import com.dice.doris.csai.entity.VideoInfo
import com.dice.doris.csai.util.SourceUtils
import com.dice.doris.csai.util.SourceUtils.SourceCallback
import com.dice.shield.drm.entity.ActionToken
import com.diceplatform.doris.ExoDoris
import com.diceplatform.doris.ExoDorisBuilder
import com.diceplatform.doris.entity.DorisAdEvent
import com.diceplatform.doris.entity.ImaCsaiProperties
import com.diceplatform.doris.entity.Source
import com.diceplatform.doris.entity.SourceBuilder
import com.diceplatform.doris.entity.TextTrack
import com.diceplatform.doris.ext.imacsai.ExoDorisImaCsaiBuilder
import com.diceplatform.doris.ext.imacsailive.ExoDorisImaCsaiLiveBuilder
import com.diceplatform.doris.ui.ExoDorisPlayerView

class MainActivity : AppCompatActivity(), SourceCallback {
    private val progressBar: ProgressBar by lazy { findViewById(R.id.progress_bar) }
    private val playerView: ExoDorisPlayerView by lazy { findViewById(R.id.playerView) }
    private var player: ExoDoris? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Global configuration.
        Log.setLogLevel(Log.LOG_LEVEL_ALL)
        playerView.keepScreenOn = true  // Keep device screen always on
        playerView.setShowPreviousButton(false)
        playerView.setShowNextButton(false)
        playerView.setShowSubtitleButton(true)
        SourceUtils.getSource(this, this)
    }

    override fun onSourceCallback(videoInfo: VideoInfo, adsConfiguration: AdsConfiguration?) {
        val imaCsaiProperties = parseCsaiProperties(adsConfiguration)

        val src = SourceBuilder()
            .setId(CsaiConfig.videoId)
            .setUrl(videoInfo.url)
            .setDrmParams(videoInfo.drm?.let { drm -> ActionToken(CsaiConfig.videoId, videoInfo.url, drm.url, drm.jwtToken, "widevine") })
            .setImaCsaiProperties(imaCsaiProperties)
            .setTextTracks(parseTextTrack(videoInfo))
            .build()

        player = createPlayer(Source.getAdType(src))

        player?.addAnalyticsListener(EventLogger())

        player?.addListener(object : Player.Listener {
            override fun onRenderedFirstFrame() {
                progressBar.visibility = View.GONE
            }
        })

        playerView.player = player?.exoPlayer

        player?.load(src)
    }

    private fun createPlayer(adType: DorisAdEvent.AdType): ExoDoris {
        val builder = if (adType === DorisAdEvent.AdType.IMA_CSAI) {
            ExoDorisImaCsaiBuilder(this@MainActivity).apply {
                setAdViewProvider(playerView)
            }
        } else if (adType === DorisAdEvent.AdType.IMA_CSAI_LIVE) {
            ExoDorisImaCsaiLiveBuilder(this@MainActivity).apply {
                setLiveAdSurfaceView(playerView.videoSurfaceView as SurfaceView)
                setAdViewProvider(playerView)
            }
        } else {
            ExoDorisBuilder(this@MainActivity)
        }
        return builder.setPlayWhenReady(true).build()
    }

    private fun parseTextTrack(videoInfo: VideoInfo?): Array<TextTrack>? {
        return videoInfo?.subtitles
            ?.map { TextTrack(Uri.parse(it.url), it.language, it.language) }
            ?.toList()?.toTypedArray()
    }

    private fun parseCsaiProperties(adsConfiguration: AdsConfiguration?): ImaCsaiProperties? {
        if (adsConfiguration == null || adsConfiguration.adUnits.isNullOrEmpty()) return null
        val csaiAds = adsConfiguration.adUnits.filter { it.insertionType.equals("CSAI", ignoreCase = true) }
        if (csaiAds.isEmpty()) return null
        var preRollAdTagUri: Uri? = null
        var midRollAdTagUri: Uri? = null
        csaiAds.forEach {
            val adFormat = it.adFormat
            if ("PREROLL".equals(adFormat, ignoreCase = true) || "VOD_VMAP".equals(adFormat, ignoreCase = true)) {
                preRollAdTagUri = Uri.parse(it.adTagUrl)
            } else if ("MIDROLL".equals(adFormat, ignoreCase = true)) {
                midRollAdTagUri = Uri.parse(it.adTagUrl)
            }
        }
        return ImaCsaiProperties.from(preRollAdTagUri, midRollAdTagUri, null)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
