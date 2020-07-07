/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.ui.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.ui.MainActivity;

import java.io.File;


public class PlayerActivity extends AppCompatActivity {

    private SimpleExoPlayer simpleExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", new File(getIntent().getStringExtra("path")));

        PlayerView playerView = findViewById(R.id.ep_video_view);
        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "Anime4you"));
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        playerView.setResizeMode(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("video_stretch_preference", "0")));
        playerView.setPlayer(simpleExoPlayer);
        simpleExoPlayer.setPlayWhenReady(true);
        simpleExoPlayer.prepare(videoSource);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        simpleExoPlayer.stop();
        simpleExoPlayer.release();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
