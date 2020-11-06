/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.util.file.ShowSaver;
import net.bplaced.abzzezz.twistapp.util.tasks.AddItemTask;
import net.bplaced.abzzezz.twistapp.util.tasks.RefreshTask;
import net.bplaced.abzzezz.twistapp.util.tasks.TaskExecutor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ShowAdapter extends BaseAdapter {
    private final Context context;
    private final ShowSaver showSaver;
    private final List<String> shows;

    public ShowAdapter(final List<String> shows, final Context context) {
        this.context = context;
        this.showSaver = TwistAppMain.getInstance().getShowSaver();
        this.shows = shows;
    }

    @Override
    public int getCount() {
        return showSaver.getList().size();
    }

    @Override
    public Object getItem(int i) {
        return shows.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public final JSONObject getDetails(final int index) {
        try {
            return new JSONObject(shows.get(index));
        } catch (final JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        if (view == null) view = LayoutInflater.from(context).inflate(R.layout.show_layout, viewGroup, false);
        final JSONObject details = getDetails(pos);
        final TextView showName = view.findViewById(R.id.show_name);
        final TextView episodes = view.findViewById(R.id.show_episodes);
        final ImageView refreshButton = view.findViewById(R.id.refresh_button);

        refreshButton.setOnClickListener(view1 -> new RefreshTask(pos).executeAsync(new TaskExecutor.Callback<String>() {
            @Override
            public void preExecute() {
            }

            @Override
            public void onComplete(String result) {
                Toast.makeText(context, "Refreshed. Episodes fetched: " + result, Toast.LENGTH_SHORT).show();
            }
        }));

        try {
            showName.setText(details.getString("title"));
            episodes.append(details.getString("episode_count"));
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public void addItem(final String item) {
        new AddItemTask(item).executeAsync(new TaskExecutor.Callback<JSONObject>() {
            @Override
            public void preExecute() {
                Log.i("Add item task @ShowList", "Fetching data from host");
            }

            @Override
            public void onComplete(final JSONObject result) {
                try {
                    if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("check_existing", false) && TwistAppMain.getInstance().getShowSaver().containsShow(result.getString("id"))) {
                        Toast.makeText(context, "The show which you wanted to add already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    shows.add(result.toString());
                    TwistAppMain.getInstance().getShowSaver().addShow(result);
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
                notifyDataSetChanged();
            }
        });
    }

}
