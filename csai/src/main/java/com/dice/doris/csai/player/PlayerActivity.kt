package com.dice.doris.csai.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.exoplayer.util.EventLogger
import com.dice.doris.csai.R.id
import com.dice.doris.csai.R.layout
import com.diceplatform.doris.ExoDoris
import com.diceplatform.doris.ExoDorisBuilder
import com.diceplatform.doris.entity.Source
import com.diceplatform.doris.ext.imacsai.ExoDorisImaCsaiBuilder
import com.diceplatform.doris.ext.imacsailive.ExoDorisImaCsaiLiveBuilder
import com.diceplatform.doris.ui.ExoDorisPlayerView

class PlayerActivity : AppCompatActivity() {
    companion object {
        var source: Source? = null
        fun startActivity(context: Context, source: Source) {
            // this is demo, you can post all params to bundle or others.
            this.source = source
            context.startActivity(Intent(context, PlayerActivity::class.java))
        }
    }

    private var player: ExoDoris? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_player)
        // Global configuration.
        Log.setLogLevel(Log.LOG_LEVEL_ALL)

        val playerView = findViewById<ExoDorisPlayerView>(id.playerView)
        playerView.setShowNextButton(false)
        playerView.setShowPreviousButton(false)
        playerView.setShowSubtitleButton(true)
        source?.let { src ->
            val dorisBuilder = if (src.imaCsaiProperties != null && src.imaCsaiProperties!!.preRollAdTagUri != null) {
                ExoDorisImaCsaiBuilder(this).apply {
                    setAdViewProvider(playerView)
                }
            } else if (src.imaCsaiProperties != null && src.imaCsaiProperties!!.midRollAdTagUri != null) {
                ExoDorisImaCsaiLiveBuilder(this).apply {
                    setLiveAdSurfaceView(playerView.videoSurfaceView as SurfaceView)
                    setAdViewProvider(playerView)
                }
            } else {
                ExoDorisBuilder(this)
            }
            player = dorisBuilder.setPlayWhenReady(true).build()
            player?.addAnalyticsListener(EventLogger())
            playerView.player = player?.exoPlayer
            player?.load(src)
        }
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