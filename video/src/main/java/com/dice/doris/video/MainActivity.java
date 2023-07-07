package com.dice.doris.video;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.dice.shield.drm.entity.ActionToken;
import com.dice.shield.drm.utils.Utils;
import com.diceplatform.doris.ExoDoris;
import com.diceplatform.doris.ExoDorisBuilder;
import com.diceplatform.doris.entity.Source;
import com.diceplatform.doris.entity.SourceBuilder;
import com.diceplatform.doris.ui.ExoDorisPlayerView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ExoDoris player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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