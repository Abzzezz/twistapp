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
import androidx.core.content.FileProvider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import ga.abzzezz.util.logging.Logger;
import id.ionbit.ionalert.IonAlert;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.ui.MainActivity;
import net.bplaced.abzzezz.twistapp.util.tasks.DecodeTask;
import net.bplaced.abzzezz.twistapp.util.tasks.DownloadTask;
import net.bplaced.abzzezz.twistapp.util.tasks.TaskExecutor;
import org.json.JSONArray;
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
    private JSONArray sources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_show);
        final int showIndex = getIntent().getIntExtra("show_index", -1);
        final JSONObject details = TwistAppMain.getInstance().getShowSaver().getShowDetails(showIndex);

        try {
            this.showName = details.getString("title");
            this.showDir = new File(getFilesDir(), showName);
            this.sources = details.getJSONArray("sources");

            //   final String description = details.getString("description");

            final TextView showNameView = findViewById(R.id.show_name_view);
            final TextView showEpisodesView = findViewById(R.id.show_episodes_view);
            //  final TextView showDescriptionView = findViewById(R.id.show_episodes_view);

            showNameView.setText(showName);
            showEpisodesView.setText("Episodes" + sources.length());

            //      showDescriptionView.setText(description);

            final ListView episodeListView = findViewById(R.id.episodes_list_view);
            episodeListView.setAdapter(episodeAdapter = new EpisodeAdapter(sources, getApplicationContext()));
            episodeListView.setOnItemClickListener((adapterView, view, i, l) -> {
                final boolean isDownloaded = isEpisodeDownloaded(i);

                new IonAlert(SelectedShow.this, IonAlert.NORMAL_TYPE)
                        .setConfirmText("Stream")
                        .setConfirmClickListener(ionAlert -> streamEpisode(i))
                        .setCancelText(isDownloaded ? "Play downloaded" : "Cancel")
                        .setCancelClickListener(ionAlert -> {
                            if (isDownloaded) {
                                playDownloaded(i);
                                ionAlert.dismiss();
                            } else
                                ionAlert.dismissWithAnimation();
                        }).show();
            });

            final FloatingActionButton downloadShow = findViewById(R.id.download_button_show);
            downloadShow.setOnClickListener(view -> download(0, sources.length(), 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void streamEpisode(final int index) {
        Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
    }


    private void playDownloaded(final int index) {
        final File file = getEpisodeFile(index);
        //   final Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        // intent.putExtra("path", file.getAbsolutePath());

        //  startActivity(Objects.requireNonNull(intent));
        // finish();
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file), "video/mp4");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Objects.requireNonNull(intent));
        finish();
    }

    private File getEpisodeFile(final int index) {
        if (showDir.listFiles() != null) {
            return Arrays.stream(showDir.listFiles()).filter(file -> file.getName().substring(0, file.getName().lastIndexOf(".")).equals(String.valueOf(index))).findFirst().get();
        }
        return null;
    }


    public void refreshAdapter() {
        episodeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent(getApplication(), MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private boolean isEpisodeDownloaded(final int index) {
        if (showDir.list() != null) {
            return Arrays.stream(showDir.list()).anyMatch(s -> s.substring(0, s.lastIndexOf(".")).equals(String.valueOf(index)));
        }
        return false;
    }

    public void download(final int start, final int countMax, final int currentCount) {
        Logger.log("Next episode: " + start, Logger.LogType.INFO);
        final int[] count = {currentCount, start};
        /*
         * Check if count is bigger than the max episodes to download
         */
        if (count[0] >= countMax) {
            Logger.log("current episode exceeds max / start exceeds max", Logger.LogType.ERROR);
            return;
        }
        try {
            final String item = sources.getString(count[1]);
            new DecodeTask(item).executeAsync(new TaskExecutor.Callback<String>() {
                @Override
                public void preExecute() {
                    Log.i("Decoder", "Starting");
                }

                @Override
                public void onComplete(final String result) {
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

        private final JSONArray episodes;
        private final Context context;

        public EpisodeAdapter(final JSONArray episodes, final Context context) {
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

            if (isEpisodeDownloaded(index)) {
                textView.setTextColor(0xFFff6768);
                imageView.setImageResource(R.drawable.delete);
                imageView.setOnClickListener(view1 -> new IonAlert(SelectedShow.this, IonAlert.WARNING_TYPE).setTitleText("Delete file?").setConfirmText("Yes, delete").setConfirmClickListener(ionAlert -> {
                    getEpisodeFile(index).delete();
                    refreshAdapter();
                    ionAlert.dismissWithAnimation();
                }).setCancelText("Abort").setCancelClickListener(IonAlert::dismissWithAnimation).show());
            } else {
                textView.setTextColor(R.color.colorPrimary);
                imageView.setImageResource(R.drawable.download);
                imageView.setOnClickListener(view1 -> download(index, 1, 0));
            }
            return view;
        }
    }

}
