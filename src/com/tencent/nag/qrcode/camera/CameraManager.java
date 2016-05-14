package com.tencent.nag.qrcode.camera;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * �����������Ҫ�õ���װͼƬԤ��������Ȳ���
 * 
 */
public final class CameraManager {

    private static final String TAG = "CameraManager";

    private static CameraManager cameraManager;

    public static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT
    static {
        int sdkInt;
        try {
            sdkInt = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException nfe) {
            sdkInt = 10000;
        }
        SDK_INT = sdkInt;
    }

    private final Context context;
    private final CameraConfigurationManager configManager;



    private static Camera camera;

    private boolean initialized;
    private boolean previewing;
    private final boolean useOneShotPreviewCallback;
    private final PreviewCallback previewCallback;

    /**
     * ���������Ƿ��Ѵ�
     */
    private boolean mIsFlashLighting = false;

    private AutoFocusManager autoFocusManager;

    public AutoFocusManager getAutoFocusManager() {
        return autoFocusManager;
    }

    /**
     * ��ʼ��������������֤����.
     * 
     * @param context ��Ҫ����CameraManager��Activity.
     */
    public static void init(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }
    }

    /**
     * ��ȡ���������ĵ���.
     * 
     * @return �����.
     */
    public static CameraManager get() {
        return cameraManager;
    }

    public synchronized boolean isOpen() {
        return camera != null;
    }

    public CameraManager(Context context) {
        this.context = context.getApplicationContext();
        this.configManager = new CameraConfigurationManager(context);

        useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3; // 3
        previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);

    }

    /**
     * �������������ʼ�����Ӳ������.
     * 
     * @param holder ������ս����View
     * @throws IOException �����������ʧ��ʱ�׳����쳣
     * 
     */
    public synchronized void openDriver(SurfaceHolder holder) throws IOException, RuntimeException {
        if (camera == null) {
            camera = Camera.open();
            if (camera == null) {
                throw new IOException();
            }
        }

        camera.setPreviewDisplay(holder);

        if (!initialized) {// ��ֻ֤��ʼ�����һ��
            initialized = true;
            configManager.initFromCameraParameters(camera);
        }

        configManager.setDesiredCameraParameters(camera);

    }


    /**
     * �ر��������.
     */
    public synchronized void closeDriver() {
        if (camera != null) {
            mIsFlashLighting = false;
            camera.release();
            camera = null;
        }
    }

    /**
     * �������Ӳ������ʼɨ��.
     */
    public synchronized void startPreview() {
        if (camera != null && !previewing) {
            previewing = true;

            camera.startPreview();
            autoFocusManager = new AutoFocusManager(context, camera);
        }
    }

    /**
     * ֹͣɨ��.
     */
    public synchronized void stopPreview(boolean destory) {
        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }
        if (camera != null && previewing) {

            if (!useOneShotPreviewCallback) {
                camera.setPreviewCallback(null);
            }
            camera.stopPreview();
            previewCallback.setHandler(null, 0, null);
            if (destory) {
            }
            previewing = false;
        }
    }

    /**
     * ����Ԥ��֡��Handler�����ص���ݽ���ͨ��message.obj���ݣ���ȱ�����Ϊmessage.arg1���߶ȱ�����Ϊmessage.arg2
     * 
     * @param handler ������Ϣ��Handler
     * @param message ���͸�Handler����Ϣ
     * 
     */
    public void requestPreviewFrame(Handler handler, int message, Bundle bundle) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message, bundle);
            configManager.setDisplayOrientation(camera);
            if (useOneShotPreviewCallback) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    /**
     * �򿪡��ر�����ƣ�Ϊ�˼���SDK1.6 �����°汾�����÷�����ƽ������á�
     */
    public void toggleFlashLight() {
        try {
            Parameters p = camera.getParameters();

            String constant = null;
            if (!mIsFlashLighting) {
                constant = "FLASH_MODE_TORCH";
                mIsFlashLighting = true;

            } else {
                constant = "FLASH_MODE_OFF";
                mIsFlashLighting = false;
            }

            Field field = Camera.Parameters.class.getField(constant);
            Method method = Camera.Parameters.class.getMethod("setFlashMode", String.class);

            String property = (String) field.get(Camera.Parameters.class);
            method.invoke(p, property);

            camera.setParameters(p);

        } catch (Exception e) {

        }
    }

    public boolean isFlashLighting() {
        return mIsFlashLighting;
    }

    // public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
    // return new PlanarYUVLuminanceSource(data, width, height, 0, 0,
    // width, height, false);
    // }

    public Camera.Parameters getCameraParameters() {
        return camera.getParameters();
    }

    public void setMeteraing() {
        try {
            Camera.Parameters parameters = camera.getParameters();
            final boolean success = MeteringInterface.setMetering(parameters);
            if (success) {
                camera.setParameters(parameters);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Catch RuntimeException when setMeteraing", e);
        } catch (Exception e) {
            Log.e(TAG, "Catch Exception when setMeteraing", e);
        }
    }
    public  CameraConfigurationManager getConfigManager() {
        return this.configManager;
    }

}
