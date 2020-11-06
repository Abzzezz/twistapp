/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.tasks;

import android.util.Log;
import net.bplaced.abzzezz.twistapp.util.misc.StringHandler;
import net.bplaced.abzzezz.twistapp.util.misc.URLUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class AddItemTask extends TaskExecutor implements Callable<JSONObject> {

    private final String showName;
    private URL url;

    public AddItemTask(final String itemUrl) {
        this.showName = itemUrl.substring(itemUrl.indexOf("/a/") + 3, itemUrl.lastIndexOf("/"));
        try {
            this.url = new URL(StringHandler.API_URL + showName);
        } catch (MalformedURLException e) {
            Log.e("Add item task", "Loading show url");
            e.printStackTrace();
        }

    }

    public <R> void executeAsync(final Callback<JSONObject> callback) {
        super.executeAsync(this, callback);
    }

    @Override
    public JSONObject call() throws Exception {
        HttpsURLConnection connection = URLUtil.createHttpsConnection(url);
        connection.connect();
        final JSONObject fetchedDetails = new JSONObject(new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining()));

        this.url = new URL(StringHandler.API_URL + showName + "/sources/");
        connection = URLUtil.createHttpsConnection(url);
        connection.connect();

        final JSONArray fetchedSources = new JSONArray(new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining()));
        final JSONArray sources = new JSONArray();

        for (int i = 0; i < fetchedSources.length(); i++) {
            final JSONObject item = fetchedSources.getJSONObject(i);
            sources.put(item.getString("source"));
        }

        final JSONObject showDetails = new JSONObject();
        showDetails.put("url", showName)
                .put("title", fetchedDetails.getString("title"))
                .put("id", fetchedDetails.getString("id"))
                .put("sources", sources)
                .put("episode_count", sources.length())
                .put("description", fetchedDetails.getString("description"));
        return showDetails;
    }
}
