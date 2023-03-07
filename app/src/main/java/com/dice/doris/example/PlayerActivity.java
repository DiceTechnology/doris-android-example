package com.dice.doris.example;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.diceplatform.doris.ExoDoris;
import com.diceplatform.doris.ExoDorisBuilder;
import com.diceplatform.doris.entity.Source;
import com.diceplatform.doris.entity.SourceBuilder;
import com.diceplatform.doris.ui.ExoDorisPlayerView;
import com.google.android.exoplayer2.MediaItem;

public class PlayerActivity extends BasePlayerActivity {

    private ExoDoris player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        player = new ExoDorisBuilder(this)
                .setPlayWhenReady(true)
                .build();

        ExoDorisPlayerView playerView = findViewById(R.id.playerView);
        playerView.setPlayer(player.getExoPlayer());

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                .setUri(getString(R.string.dash_video_url));

        Source source = new SourceBuilder()
                .setMediaItem(mediaItemBuilder.build())
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
