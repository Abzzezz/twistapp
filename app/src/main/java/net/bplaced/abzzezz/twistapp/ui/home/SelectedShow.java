/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ga.abzzezz.util.logging.Logger;
import id.ionbit.ionalert.IonAlert;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.ui.MainActivity;
import net.bplaced.abzzezz.twistapp.ui.player.PlayerActivity;
import net.bplaced.abzzezz.twistapp.util.tasks.DecodeTask;
import net.bplaced.abzzezz.twistapp.util.tasks.DownloadTask;
import net.bplaced.abzzezz.twistapp.util.tasks.TaskExecutor;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class SelectedShow extends AppCompatActivity {

    private String showName;
    private File showDir;
    private EpisodeAdapter episodeAdapter;
    private JSONObject episodes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_show);
        int showIndex = getIntent().getIntExtra("show_index", -1);

        JSONObject details = TwistAppMain.getINSTANCE().getShowSaver().getShowDetails(showIndex);
        try {

            this.showName = details.getString("title");
            this.showDir = new File(getFilesDir(), showName);

            final String episodeSum = details.getString("episode");
            final String description = details.getString("description");
            this.episodes = details.getJSONObject("episodes");

            final TextView showNameView = findViewById(R.id.show_name_view);
            final TextView showEpisodesView = findViewById(R.id.show_episodes_view);
            final TextView showDescriptionView = findViewById(R.id.show_episodes_view);

            showNameView.setText(showName);
            showEpisodesView.setText("Episodes" + episodeSum);

            //      showDescriptionView.setText(description);

            ListView episodeListView = findViewById(R.id.episodes_list_view);
            episodeListView.setAdapter(episodeAdapter = new EpisodeAdapter(episodes, getApplicationContext()));
            episodeListView.setOnItemClickListener((adapterView, view, i, l) -> {
                final boolean isDownloaded = episodeExists(i);

                new IonAlert(SelectedShow.this, IonAlert.NORMAL_TYPE)
                        .setConfirmText("Stream")
                        .setConfirmClickListener(ionAlert -> streamEpisode(i))
                        .setCancelText(isDownloaded ? "Play downloaded" : "Cancel")
                        .setCancelClickListener(ionAlert -> {
                            if (isDownloaded)
                                playDownloaded(i);
                            else
                                ionAlert.dismissWithAnimation();
                        }).show();
            });

            FloatingActionButton downloadShow = findViewById(R.id.download_button_show);
            downloadShow.setOnClickListener(view -> download(0, Integer.parseInt(episodeSum), 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void streamEpisode(final int index) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }


    private void playDownloaded(final int index) {
        File file = getEpisodeFile(index);
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        intent.putExtra("path", file.getAbsolutePath());
        startActivity(Objects.requireNonNull(intent));
        finish();
    }

    private File getEpisodeFile(final int index) {
        if (showDir.listFiles() != null) {
            return showDir.listFiles()[index];
        }
        return null;
    }


    public void refreshAdapter() {
        episodeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplication(), MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private boolean episodeExists(final int index) {
        if (showDir.list() != null) {
            return Arrays.stream(showDir.list()).anyMatch(s -> s.substring(0, s.lastIndexOf(".")).equals(String.valueOf(index)));
        }
        return false;
    }

    public void download(final int start, final int countMax, final int currentCount) {
        Logger.log("Next episode: " + start, Logger.LogType.INFO);
        int[] count = {currentCount, start};
        /*
         * Check if count is bigger than the max episodes to download
         */
        if (count[0] >= countMax) {
            Logger.log("current episode exceeds max / start exceeds max", Logger.LogType.ERROR);
            return;
        }
        try {
            String item = episodes.getString(String.valueOf(count[1]));
            new DecodeTask(item).executeAsync(new TaskExecutor.Callback<String>() {
                @Override
                public void preExecute() {
                    Log.i("Decoder", "Starting");
                }

                @Override
                public void onComplete(String result) {
                    Log.i("Decoder", "Done: " + result);
                    //Start new download thread
                    try {
                        new DownloadTask(SelectedShow.this, new URL(result), count[1], showName, new int[]{count[0], count[1], countMax}).executeAsync();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class EpisodeAdapter extends BaseAdapter {

        private final JSONObject episodes;
        private final Context context;

        public EpisodeAdapter(JSONObject episodes, Context context) {
            this.episodes = episodes;
            this.context = context;
        }

        @Override
        public int getCount() {
            return episodes.length();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            if (view == null) view = LayoutInflater.from(context).inflate(R.layout.episode_layout, viewGroup, false);
            final ImageView imageView = view.findViewById(R.id.download_button_episode);
            final TextView textView = view.findViewById(R.id.episode_name_view);
            textView.setText("Episode: " + index);
            if (episodeExists(index)) {
                textView.setTextColor(0xFFff6768);
                imageView.setImageResource(R.drawable.delete);
            } else {
                imageView.setImageResource(R.drawable.download);
                imageView.setOnClickListener(view1 -> download(index, 1, 0));
            }
            return view;
        }
    }

}
