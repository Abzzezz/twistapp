/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import ga.abzzezz.util.logging.Logger;
import net.bplaced.abzzezz.twistapp.util.IntentHelper;
import net.bplaced.abzzezz.twistapp.util.tasks.DownloadTask;

public class StopDownloadingReceiver extends BroadcastReceiver {

    /**
     * Gets called if stop download is triggered
     *
     * @param context
     * @param intent
     */


    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadTask downloadTask = (DownloadTask) IntentHelper.getObjectForKey(intent.getData().toString());
        if (!downloadTask.isCancelled()) {
            Logger.log("Further downloading cancelled", Logger.LogType.INFO);
            Toast.makeText(context, "Download cancelled", Toast.LENGTH_SHORT).show();
            downloadTask.cancel();
        }
    }
}