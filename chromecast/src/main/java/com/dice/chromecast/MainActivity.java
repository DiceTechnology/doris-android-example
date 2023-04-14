package com.dice.chromecast;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteButton;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private MediaRouteButton mediaRouteButton;
    private CastContext castContext;
    private SessionManager sessionManager;
    private Button buttonLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonLoad = findViewById(R.id.button_load);

        // Init cast context and session manager
        mediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
        castContext = CastContext.getSharedInstance(this);
        sessionManager = castContext.getSessionManager();

        // Load video
        buttonLoad.setOnClickListener(view -> load());
    }

    private void load() {
        int position = 10;

        // Load Options
        MediaLoadOptions.Builder loadOptionsBuilder = new MediaLoadOptions.Builder()
                .setAutoplay(true);
        if (position > 0) {
            loadOptionsBuilder.setPlayPosition(position);
        }
        MediaLoadOptions loadOptions = loadOptionsBuilder.build();

        // Metadata
        MediaMetadata metadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        metadata.putString(MediaMetadata.KEY_TITLE, Constants.title);

        // Custom data
        JSONObject customData = new JSONObject();
        try {
            customData.put("sourceType", "resolvable");
            customData.put("resolutionType", "dice");
            customData.put("resolutionCustomData", new JSONObject().put("isLive", false));
            customData.put("chromecastSessionSerialized", new JSONObject()
                    .put("baseUrl", Constants.baseUrl)
                    .put("realm", Constants.realm)
                    .put("authorisationToken", Constants.authorisationToken)
                    .put("refreshToken", Constants.refreshToken)
                    .put("beacon", new JSONObject()
                            .put("action", 2)
                            .put("cid", Constants.cid)
                            .put("startedAt", System.currentTimeMillis())
                            .put("video", Integer.parseInt(Constants.videoId))
                            .put("progress", position)
                            .put("endpoint", Constants.endpoint)
                    )
                    .toString()
            );
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Unexpected JSON exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Configuration exception", e);
        }

        // Media Info
        MediaInfo mediaInfo = new MediaInfo.Builder(Constants.videoId)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(null)
                .setMetadata(metadata)
                .setCustomData(customData)
                .build();

        CastSession castSession = sessionManager.getCurrentCastSession();
        if (castSession == null) {
            return;
        }
        RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        if (remoteMediaClient != null) {
            remoteMediaClient.load(mediaInfo, loadOptions);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }
}