/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.tasks;

import android.util.Log;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.util.misc.StringHandler;
import net.bplaced.abzzezz.twistapp.util.misc.URLUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class RefreshTask extends TaskExecutor implements Callable<String> {

    private final int index;

    public RefreshTask(final int index) {
        this.index = index;
    }


    public <R> void executeAsync(Callback<String> callback) {
        super.executeAsync(this, callback);
    }

    @Override
    public String call() throws Exception {
        final JSONObject showDetails = TwistAppMain.getInstance().getShowSaver().getShowDetails(index);
        final String showPath = showDetails.getString("url");
        final int episodes = showDetails.getInt("episode_count");

        URL apiUrl = new URL(StringHandler.getApiUrl(showPath, 0));


        HttpsURLConnection connection = URLUtil.createHttpsConnection(apiUrl);
        connection.connect();

        final JSONObject fetchedJSON = new JSONObject(new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining()));
        final int updatedCount = fetchedJSON.getJSONArray("episodes").length();
        final String updatedCountString = String.valueOf(updatedCount);

        if (updatedCount > episodes) {
            TwistAppMain.getInstance().getShowSaver().updateEntry(index, "episode_count", updatedCountString);
            //Open URL to sources
            apiUrl = new URL(StringHandler.getApiUrl(showPath, StringHandler.SOURCE_MODE));
            connection = URLUtil.createHttpsConnection(apiUrl);
            connection.connect();

            final JSONArray fetchedSources = new JSONArray(new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining()));
            final JSONArray sources = new JSONArray();

            for (int i = 0; i < fetchedSources.length(); i++) {
                final JSONObject item = fetchedSources.getJSONObject(i);
                sources.put(item.getString("source"));
            }

            Log.i("Refresh-Task", "Sources refreshed, episode count updated : " + sources);

            TwistAppMain.getInstance().getShowSaver().updateEntry(index, "sources", sources.toString());
        }

        return updatedCountString;
    }



}
