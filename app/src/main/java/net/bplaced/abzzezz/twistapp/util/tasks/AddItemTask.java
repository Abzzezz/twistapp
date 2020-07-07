/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.tasks;

import android.util.Log;
import net.bplaced.abzzezz.twistapp.util.StringHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class AddItemTask extends TaskExecutor implements Callable<String[]> {

    private final String showName;
    private URL url;

    public AddItemTask(final String item) {
        this.showName = item.substring(item.indexOf("/a/") + 3, item.lastIndexOf("/"));
        try {
            this.url = new URL(StringHandler.API_URL + showName);
        } catch (MalformedURLException e) {
            Log.e("Add item task", "Loading show url");
            e.printStackTrace();
        }

    }

    public <R> void executeAsync(Callback<String[]> callback) {
        super.executeAsync(this, callback);
    }

    @Override
    public String[] call() throws Exception {
        HttpsURLConnection connection = createHttpsConnection(url);
        connection.connect();
        final String details = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining());

        this.url = new URL(StringHandler.API_URL + showName + "/sources/");
        connection = createHttpsConnection(url);
        connection.connect();

        final String source = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining());
        final JSONArray arrays = new JSONArray(source);
        final JSONObject jsonSource = new JSONObject();
        for (int i = 0; i < arrays.length(); i++) {
            JSONObject item = arrays.getJSONObject(i);
            jsonSource.put(String.valueOf(i), item.getString("source"));
        }

        return new String[]{details, jsonSource.toString(), showName};
    }


    private HttpsURLConnection createHttpsConnection(final URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        connection.addRequestProperty("x-access-token", StringHandler.getRequestToken());
        return connection;
    }
}
