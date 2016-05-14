package com.tencent.nag.qrcode.camera;


import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


final class PreviewCallback implements Camera.PreviewCallback {

    private static final String TAG = "PreviewCallBack";

    private final CameraConfigurationManager configManager;
    private final boolean useOneShotPreviewCallback;
    private Handler previewHandler;
    private int previewMessage;
    private Bundle mDecodeBundle;

    PreviewCallback(CameraConfigurationManager configManager,
            boolean useOneShotPreviewCallback) {
        this.configManager = configManager;
        this.useOneShotPreviewCallback = useOneShotPreviewCallback;
    }

    void setHandler(Handler previewHandler, int previewMessage, Bundle decodeBundle) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
        this.mDecodeBundle = decodeBundle;
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = configManager.getCameraResolution();
        if (!useOneShotPreviewCallback) {
            camera.setPreviewCallback(null);
        }
        if (previewHandler != null) {
            Message message = previewHandler.obtainMessage(previewMessage,
                    cameraResolution.x, cameraResolution.y, data);
            if (mDecodeBundle != null) {
                message.setData(mDecodeBundle);
            }
            message.sendToTarget();
            previewHandler = null;
        } else {
            Log.d(TAG, "Got preview callback, but no handler for it");
        }
    }
}

