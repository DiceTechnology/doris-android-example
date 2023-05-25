package com.dice.doris.example;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dice.shield.drm.entity.ActionToken;
import com.dice.shield.drm.utils.Utils;
import com.diceplatform.doris.ExoDoris;
import com.diceplatform.doris.ExoDorisBuilder;
import com.diceplatform.doris.entity.Source;
import com.diceplatform.doris.entity.SourceBuilder;
import com.diceplatform.doris.ui.ExoDorisPlayerView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.util.EventLogger;

public class PlayerActivity extends AppCompatActivity {

    private ExoDoris player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Bundle extras = getIntent().getExtras();

        VideoItem videoItem = (VideoItem) extras.getSerializable("video");

        player = new ExoDorisBuilder(this)
                .setPlayWhenReady(true)
                .build();
        player.addAnalyticsListener(new EventLogger());

        ExoDorisPlayerView playerView = findViewById(R.id.playerView);
        playerView.setPlayer(player.getExoPlayer());

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                .setUri(videoItem.getUrl());

        Source source = new SourceBuilder()
                .setMediaItemBuilder(mediaItemBuilder)
                .setId(videoItem.getId())
                .setShouldPlayOffline(true)
                .setDrmParams(new ActionToken(Utils.DRM_SCHEME, videoItem.getOfflineLicense()))
                .setOfflineLicense(videoItem.getOfflineLicense())
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
