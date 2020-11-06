/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.misc;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

public class URLUtil {

    public static HttpsURLConnection createHttpsConnection(final URL url) throws IOException {
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("User-Agent", StringHandler.USER_AGENT);
        connection.addRequestProperty("x-access-token", StringHandler.getRequestToken());
        return connection;
    }
}
