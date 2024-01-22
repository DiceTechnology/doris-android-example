package com.dice.doris.csai.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import androidx.media3.exoplayer.util.EventLogger
import com.dice.doris.csai.R.id
import com.dice.doris.csai.R.layout
import com.diceplatform.doris.ExoDoris
import com.diceplatform.doris.ExoDorisBuilder
import com.diceplatform.doris.ext.imacsai.ExoDorisImaCsaiBuilder
import com.diceplatform.doris.ext.imacsailive.ExoDorisImaCsaiLiveBuilder
import com.diceplatform.doris.ui.ExoDorisPlayerView

class PlayerActivity : AppCompatActivity() {
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
        val source = SourceHolder.source!!
        val dorisBuilder = if (source.imaCsaiProperties != null && source.imaCsaiProperties!!.preRollAdTagUri != null) {
            ExoDorisImaCsaiBuilder(this).apply {
                setAdViewProvider(playerView)
            }
        } else if (source.imaCsaiProperties != null && source.imaCsaiProperties!!.midRollAdTagUri != null) {
            ExoDorisImaCsaiLiveBuilder(this).apply {
//                setLiveAdSurfaceView(playerView.getsec)
                setAdViewProvider(playerView)
            } //TODO
        } else {
            ExoDorisBuilder(this)
        }
        player = dorisBuilder.setPlayWhenReady(true).build().apply {
            addAnalyticsListener(EventLogger())
            playerView.player = exoPlayer
            load(source)
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