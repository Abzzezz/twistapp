/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;
import ga.abzzezz.util.logging.Logger;
import net.bplaced.abzzezz.twistapp.util.file.DownloadTracker;
import net.bplaced.abzzezz.twistapp.util.file.ShowSaver;

public class TwistAppMain {

    public static final String NOTIFICATION_CHANNEL_ID = "Twist-app-notification-channel";
    private static final TwistAppMain INSTANCE = new TwistAppMain();
    private final int version;
    private Application root;
    private ShowSaver showSaver;
    private DownloadTracker downloadTracker;

    public TwistAppMain() {
        this.version = 1;
    }

    public static TwistAppMain getINSTANCE() {
        return INSTANCE;
    }

    public void start() {
        this.initHandlers();
        this.createNotificationChannel();
    }

    private void initHandlers() {
        this.showSaver = new ShowSaver(root);
        this.downloadTracker = new DownloadTracker(root);
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Logger.log("Creating new notification channel", Logger.LogType.INFO);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Twist-App-Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification channel to display download notification");
            NotificationManager notificationManager = root.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(root);
            managerCompat.cancelAll();
        }
    }

    public DownloadTracker getDownloadTracker() {
        return downloadTracker;
    }

    public ShowSaver getShowSaver() {
        return showSaver;
    }

    public Application getRoot() {
        return root;
    }

    public void setRoot(Application root) {
        this.root = root;
    }

    public int getVersion() {
        return version;
    }
}
