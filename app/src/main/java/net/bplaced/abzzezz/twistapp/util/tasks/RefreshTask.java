/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.tasks;

import android.util.Log;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.util.misc.StringHandler;
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
        JSONObject jsonObject = TwistAppMain.getINSTANCE().getShowSaver().getShowDetails(index);
        String showPath = jsonObject.getString("url");
        URL url = new URL(StringHandler.getApiUrl(showPath, 0));
        int episodes = jsonObject.getInt("episode");

        HttpsURLConnection connection = createHttpsConnection(url);
        connection.connect();

        JSONObject detailJSON = new JSONObject(new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining()));
        int newEpisodes = detailJSON.getJSONArray("episodes").length();
        String newEpisodesString = String.valueOf(newEpisodes);

        if (newEpisodes > episodes) {
            TwistAppMain.getINSTANCE().getShowSaver().updateEntry(index, new String[]{"episode", newEpisodesString});
            //Open URL to sources
            url = new URL(StringHandler.getApiUrl(showPath, StringHandler.SOURCE_MODE));
            connection = createHttpsConnection(url);
            connection.connect();

            final String source = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(Collectors.joining());
            final JSONArray arrays = new JSONArray(source);
            final JSONObject jsonSource = new JSONObject();

            for (int i = 0; i < arrays.length(); i++)
                jsonSource.put(String.valueOf(i), arrays.getJSONObject(i).getString("source"));

            Log.i("Refresh-Task", "Sources refreshed, episode count updated : " + jsonSource.toString());
            TwistAppMain.getINSTANCE().getShowSaver().updateEntry(index, "episodes", jsonSource);
        }

        return newEpisodesString;
    }

    private HttpsURLConnection createHttpsConnection(final URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        connection.addRequestProperty("x-access-token", StringHandler.getRequestToken());
        return connection;
    }

}
