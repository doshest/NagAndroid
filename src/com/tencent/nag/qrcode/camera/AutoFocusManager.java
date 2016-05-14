package com.tencent.nag.qrcode.camera;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;


public final class AutoFocusManager implements Camera.AutoFocusCallback {

    private static final String TAG = AutoFocusManager.class.getSimpleName();

    private static long AUTO_FOCUS_INTERVAL_MS = 1500L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;
    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private boolean stopped;
    private boolean focusing;
    private final boolean useAutoFocus;
    private final Camera camera;
    private AsyncTask<?,?,?> outstandingTask;

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    AutoFocusManager(Context context, Camera camera) {
        this.camera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        // TODO �����Ǵ�preference�л�õ�
        useAutoFocus = true && FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        Log.i(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        focusing = false;
        autoFocusAgainLater();
    }

    private synchronized void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                newTask.execute();
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                // LogUtil.w(TAG, "Could not request auto focus", ree);
            }
        }
    }

    synchronized void start() {
        if (useAutoFocus) {
            outstandingTask = null;
            if (!stopped && !focusing) {
                try {
                    camera.autoFocus(this);
                    focusing = true;
                } catch (RuntimeException re) {
                    // Have heard RuntimeException reported in Android 4.0.x+;
                    // continue?
                    Log.i(TAG, "Unexpected exception while focusing", re);
                    // Try again later to keep cycle going
                    autoFocusAgainLater();
                }
            }
        }
    }

    public synchronized void startIntime() {
        if (!stopped && !focusing) {
            cancelOutstandingTask();
            start();
        }
    }

    private synchronized void cancelOutstandingTask() {
        if (outstandingTask != null) {
            if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                outstandingTask.cancel(true);
            }
            outstandingTask = null;
        }
    }

    synchronized void stop() {
        stopped = true;
        if (useAutoFocus) {
            cancelOutstandingTask();
            
            try {
                try {
                	Class[] argsClass = new Class[0];    
                	Method method = camera.getClass().getMethod( "cancelAutoFocus", argsClass);
                	method.invoke(camera, new Object[]{});
                } catch (Exception e) {
                }

            } catch (Exception e) {

            }
        }
    }

    private final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... voids) {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
            } catch (InterruptedException e) {
                // continue
            }
            start();
            return null;
        }
    }

}

