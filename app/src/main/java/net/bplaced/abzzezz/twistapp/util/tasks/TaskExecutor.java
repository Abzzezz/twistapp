/*
 * Copyright (c) 2020.
 * The code used in this project is entirely owned by Roman P.
 * Code snippets / templates / etc. are mentioned and credited.
 */

package net.bplaced.abzzezz.twistapp.util.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskExecutor {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
        callback.preExecute();
        executor.execute(() -> {
            try {
                R result = callable.call();
                handler.post(() -> callback.onComplete(result));
            } catch (Exception e) {
                Log.e("Task Executor", "Executing task");
                e.printStackTrace();
            }
        });
    }

    public interface Callback<R> {
        void preExecute();

        void onComplete(R result);
    }
}
