/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.file;

import android.annotation.SuppressLint;
import android.content.Context;
import ga.abzzezz.util.data.FileUtil;
import ga.abzzezz.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DownloadTracker {

    public final File trackerFile;

    public DownloadTracker(Context context) {
        this.trackerFile = new File(context.getDataDir(), "DownloadTrack.xml");
        if (!trackerFile.exists()) {
            try {
                trackerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Submit track to tracker list
     *
     * @param information string to add
     */
    public void submitTrack(final String information) {
        @SuppressLint("SimpleDateFormat") String time = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z").format(new Date(System.currentTimeMillis()));
        String track = time + "\n" + information + "\n";

        try (FileOutputStream fos = new FileOutputStream(trackerFile, true)) {
            fos.write(track.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear by deleting file
     */
    public void clearTrack() {
        Logger.log("Clearing track: " + trackerFile.delete(), Logger.LogType.INFO);
        try {
            trackerFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return List containing all logs
     */
    public ArrayList<String> getList() {
        try {
            return (ArrayList<String>) FileUtil.getFileContentAsList(trackerFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
