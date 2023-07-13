package com.dice.doris.video;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.Log;
import androidx.media3.exoplayer.util.EventLogger;

import android.net.Uri;
import android.os.Bundle;

import com.diceplatform.doris.ExoDoris;
import com.diceplatform.doris.ExoDorisBuilder;
import com.diceplatform.doris.entity.Source;
import com.diceplatform.doris.entity.SourceBuilder;
import com.diceplatform.doris.ui.ExoDorisPlayerView;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private ExoDoris player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Global configuration.
        Log.setLogLevel(Log.LOG_LEVEL_ALL);

        player = new ExoDorisBuilder(this)
                .setPlayWhenReady(true)
                .build();
        player.addAnalyticsListener(new EventLogger());

        ExoDorisPlayerView playerView = findViewById(R.id.playerView);

        playerView.setShowNextButton(false);
        playerView.setShowPreviousButton(false);
        playerView.setShowSubtitleButton(true);

        playerView.setPlayer(player.getExoPlayer());

        MediaItem.SubtitleConfiguration subsConfig =
                new MediaItem.SubtitleConfiguration.Builder(Uri.parse("https://storage.googleapis.com/exoplayer-test-media-1/webvtt/numeric-lines.vtt"))
                        .setMimeType(MimeTypes.TEXT_VTT)
                        .setLanguage("English")
                        .build();

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                .setUri("https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd")
                .setSubtitleConfigurations(Collections.singletonList(subsConfig));

        Source source = new SourceBuilder()
                .setMediaItemBuilder(mediaItemBuilder)
                .setId("videoId")
                .build();

        player.load(source);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}