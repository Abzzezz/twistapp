/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.tasks;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import ga.abzzezz.util.logging.Logger;
import net.bplaced.abzzezz.twistapp.R;
import net.bplaced.abzzezz.twistapp.TwistAppMain;
import net.bplaced.abzzezz.twistapp.ui.home.SelectedShow;
import net.bplaced.abzzezz.twistapp.util.misc.IntentHelper;
import net.bplaced.abzzezz.twistapp.util.misc.StringHandler;
import net.bplaced.abzzezz.twistapp.util.receiver.StopDownloadingReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

public class DownloadTask extends TaskExecutor implements Callable<String>, TaskExecutor.Callback<String> {

    private final SelectedShow application;
    private final URL url;
    private final int episode;
    private final String showName;
    private final int[] count;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder notification;
    private int notifyID;
    private boolean cancel;
    private FileOutputStream fileOutputStream;
    private File outFile;

    public DownloadTask(final SelectedShow application, final URL url, final int episode, final String showName, final int[] count) {
        this.application = application;
        this.url = url;
        this.episode = episode;
        this.showName = showName;
        this.count = count;
    }


    public <R> void executeAsync() {
        super.executeAsync(this, this);
    }

    /**
     * Call method downloads file.
     *
     * @return
     * @throws Exception
     */
    @Override
    public String call() throws Exception {
        Logger.log("New download thread started" + notifyID, Logger.LogType.INFO);
        final File outDir = new File(application.getFilesDir(), showName);
        if (!outDir.exists()) outDir.mkdir();
        this.outFile = new File(outDir, episode + ".mp4");

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", StringHandler.USER_AGENT);
        connection.addRequestProperty("Range", "f'bytes={pos}-");
        connection.addRequestProperty("Referer", "https://twist.moe/a/");
        connection.connect();

        //Open Stream
        this.fileOutputStream = new FileOutputStream(outFile);
        final ReadableByteChannel readableByteChannel = Channels.newChannel(connection.getInputStream());
        //Copy from channel to channel
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

        //Close stream
        Logger.log("Done copying streams, closing stream", Logger.LogType.INFO);
        fileOutputStream.close();
        return StringHandler.formatToShowString(showName, episode);
    }

    @Override
    public void onComplete(String result) {
        //Cancel notification
        notificationManagerCompat.cancel(notifyID);
        //Add to download tracker
        //Make toast text
        Toast.makeText(application, isCancelled() ? "Download cancelled" : "Done downloading anime episode: " + result, Toast.LENGTH_SHORT).show();
        this.notification = new NotificationCompat.Builder(application, TwistAppMain.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.information).setColor(Color.GREEN).setContentText("Episode-download done")
                .setContentTitle("Done downloading episode: " + result)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        //Notify, reuse old id
        if (!isCancelled()) notificationManagerCompat.notify(notifyID, notification.build());

        application.refreshAdapter();
        //Reset adapter

        //Delay and start if not cancelled
        if (!isCancelled()) {
            //Track download
            TwistAppMain.getInstance().getDownloadTracker().submitTrack("Downloaded: " + result);
            //Start new task
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (count[0] < count[2]) {
                    count[0]++;
                    count[1]++;
                    application.download(count[1], count[2], count[0]);
                }
            }, Long.parseLong(PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext()).getString("download_delay", "0")) * 1000);
        }

        /*
         * Check if stopped
         */
        if (isCancelled()) {
            Logger.log("Threading was stopped. Cancelled stop, after further downloading was stopped", Logger.LogType.INFO);
            cancel = false;
        }
    }

    /**
     * Pre execute method. Creates notifications and task id
     */
    @Override
    public void preExecute() {
        //Create notification
        this.notifyID = (int) System.currentTimeMillis() % 10000;
        this.notificationManagerCompat = NotificationManagerCompat.from(application);
        //Create intent and pass id through
        final Intent notificationActionIntent = new Intent(application, StopDownloadingReceiver.class);
        notificationActionIntent.setData(Uri.parse("" + notifyID));
        Logger.log("Assigned thread id:" + notifyID, Logger.LogType.INFO);
        //Put object key
        IntentHelper.addObjectForKey(this, String.valueOf(notifyID));
        //Bind notification and action receiver
        final PendingIntent stopDownloadingPendingIntent = PendingIntent.getBroadcast(application, 1, notificationActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.notification = new NotificationCompat.Builder(application, TwistAppMain.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.download)
                .setContentText("Currently downloading episode: " + StringHandler.formatToShowString(showName, episode))
                .setContentTitle("Episode Download")
                .setPriority(NotificationCompat.PRIORITY_HIGH).addAction(R.drawable.ic_cancel, "Stop downloading", stopDownloadingPendingIntent)
                .setOngoing(true).setProgress(100, 50, true);
        //Notify
        this.notificationManagerCompat.notify(notifyID, notification.build());
    }

    /**
     * @return task cancelled
     */
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Cancel task
     * Flush streams, delete file, refresh adapter, cancel...
     */
    public void cancel() {
        if (fileOutputStream == null) return;
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
            outFile.delete();
            application.refreshAdapter();
        } catch (IOException e) {
            Logger.log("Error closing task stream", Logger.LogType.ERROR);
            e.printStackTrace();
        }
        //Set canceled true
        this.cancel = true;
        Logger.log("Task cancelled, Streams flushed", Logger.LogType.INFO);
    }
}
