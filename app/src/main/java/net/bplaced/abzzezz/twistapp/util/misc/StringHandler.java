/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.misc;

import java.util.Random;

public class StringHandler {

    /**
     * Often used Strings
     */
    public static final String API_URL = "https://twist.moe/api/anime/";
    public static final String KEY = "LXgIVP&PorO68Rq7dTx8N^lP!Fa5sGJ^*XK";
    public static final String STREAM_URL = "https://twistcdn.bunny.sh";
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit/601.3.9 (KHTML, like Gecko) Version/9.0.2 Safari/601.3.9";
    public static final int SOURCE_MODE = 1;

    /**
     * TODO add more tokens
     *
     * @return token to use
     */
    public static String getRequestToken() {
        String[] tokens = {"1rj2vRtegS8Y60B3w3qNZm5T2Q0TN2NR"};
        return tokens[new Random().nextInt(tokens.length)];
    }

    /**
     * Get api url
     *
     * @param show
     * @param mode
     * @return
     */
    public static String getApiUrl(final String show, final int mode) {
        String url = API_URL + show;
        if (mode == SOURCE_MODE) {
            url = url + "/sources";
        }

        return url;
    }

    /**
     * @param showName
     * @param episode
     * @return
     */
    public static String formatToShowString(final String showName, final int episode) {
        return showName + ":" + episode;
    }


}