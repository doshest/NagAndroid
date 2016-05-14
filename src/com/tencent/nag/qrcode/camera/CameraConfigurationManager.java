package com.tencent.nag.qrcode.camera;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


public final class CameraConfigurationManager {
    
    private static final String TAG = CameraConfigurationManager.class.getSimpleName();

    private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
    private static final int MAX_PREVIEW_PIXELS = 1920 * 1080;
    
    public static String PREFERENCE_AUTO_FOCUS = "preferences_auto_focus";

    // ��󳤿�Ȳ�ֵ
    private static final double MAX_ASPECT_DISTORTION = 0.15;

    // private final float SMALLEST_ZOOM_FACTOR = 1.1f;//�������ʱ��С�ķŴ���
    private final float LARGEST_ZOOM_FACTOR = 1.2f;// �������ʱ���ķŴ���
    private float zoomValue;// ���ű���

    public float getZoomValue() {
        return zoomValue;
    }

    @SuppressWarnings("unused")
    private static final int DESIRED_SHARPNESS = 30;

    // ��Ļ����״̬����TitleBar��ռ�õĸ߶ȣ���ʵ�ڴ���ͼƬʱû���õ��������titleBar�����͸��
    // public static int TOP_OFFSET = 0;

    private static int ORG_SCREEN_W;
    private static int ORG_SCREEN_H;

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private final Context context;
    private Point screenResolution;
    private Point cameraResolution;
    private int previewFormat;
    private String previewFormatString;
    private static Point largestSize;

    CameraConfigurationManager(Context context) {
        this.context = context;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        ORG_SCREEN_W =width ;
        ORG_SCREEN_H = height;
    }

    /**
     * ��ȡӦ����Ҫ�����ֵ��ֻ��ȡһ��.
     */
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        previewFormat = parameters.getPreviewFormat();
        previewFormatString = parameters.get("preview-format"); // Returns the image format for
                                                                // preview frames got from
        cameraResolution = getCameraResolution(parameters, getScreenResolution());
    }

    /**
     * �������ɨ���ά����ʱ�Ĳ�������Խ����ع⡢��ƽ��Ȳ���
     */
    void setDesiredCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);// ���յ���Ļ��С
        setZoom(parameters);

        // �۽�ģʽ����
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String focusMode = null;
        if (prefs.getBoolean("preferences_auto_focus", true)) {
            if (prefs.getBoolean(PREFERENCE_AUTO_FOCUS, true)) {
                focusMode = findSettableValue(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO);
            } else {
                focusMode = findSettableValue(parameters.getSupportedFocusModes(),
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE, Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                        Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
        // Maybe selected auto-focus but not available, so fall through here:
        if (focusMode == null) {
            focusMode = findSettableValue(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_MACRO,
                    Camera.Parameters.FOCUS_MODE_EDOF);
        }
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }

        // ��ɫЧӦ����
        if (prefs.getBoolean("preferences_invert_scan", false)) {
            String colorMode = findSettableValue(parameters.getSupportedColorEffects(),
                    Camera.Parameters.EFFECT_NEGATIVE);
            if (colorMode != null) {
                parameters.setColorEffect(colorMode);
            }
        }

        // ���ó���ģʽ
        if (!prefs.getBoolean("preferences_disable_barcode_scene_mode", true)) {
            String sceneMode = findSettableValue(parameters.getSupportedSceneModes(),
                    Camera.Parameters.SCENE_MODE_BARCODE);
            if (sceneMode != null) {
                parameters.setSceneMode(sceneMode);
            }
        }

        /**
         * ���öԽ�������ع������ʵ�����ò����ԣ��������豸����������,�ȹرոù���
         * @author tiantong
         * @since 2014/11/03
         */
        // String phoneModeString = android.os.Build.MODEL;
        // if (phoneModeString.equals("GT-N5100")) {
        // // ��������GT-N5100��������������عⲹ���ͻᵼ���ֻ�����
        // android.util.Log.e("qrcode", parameters.flatten());
        // } else {
        // MeteringInterface.setFocusArea(parameters);
        // MeteringInterface.setMetering(parameters);
        // }

        // ��ƽ��ģʽ����
        String whiteBalanceMode = null;
        whiteBalanceMode = findSettableValue(parameters.getSupportedWhiteBalance(),
                Camera.Parameters.WHITE_BALANCE_AUTO);
        if (whiteBalanceMode != null) {
            parameters.setWhiteBalance(whiteBalanceMode);
        }

        setDisplayOrientation(camera);
        // camera.setParameters(parameters);

        camera.setParameters(parameters);

    }

    private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        return result;
    }

    // @TargetApi(8)
    void setDisplayOrientation(Camera camera) {
        setDisplayOrientation(camera, 90);
    }

    /**
     * �����������ʱ��Ļ��ת����
     * */
    protected void setDisplayOrientation(Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        } catch (Exception e1) {
        }
    }

   public Point getCameraResolution() {
        return cameraResolution;
    }

    /**
     * ��ȡ���ɨ���ķֱ���
     * */
    public Point getScreenResolution() {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (screenResolution == null) {
            screenResolution = new Point(displayMetrics.widthPixels, displayMetrics.heightPixels);
        } else {
            screenResolution.set(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return screenResolution;
    }

    int getPreviewFormat() {
        return previewFormat;
    }

    String getPreviewFormatString() {
        return previewFormatString;
    }

    /**
     * ��ȡ���ķֱ���
     * */
    private static Point getCameraResolution(Camera.Parameters parameters, Point screenResolution) {

        String previewSizeValueString = parameters.get("preview-size-values");

        // saw this on Xperia
        if (previewSizeValueString == null) {
            previewSizeValueString = parameters.get("preview-size-value");
        }

        Point cameraResolution = null;

        Log.d(TAG, "previewSizeValueString:"+previewSizeValueString);
        if (previewSizeValueString != null) {
            // LogUtil.d(TAG, "preview-size-values parameter: " + previewSizeValueString);
            cameraResolution = findBestPreviewSizeValue(parameters, previewSizeValueString, screenResolution);
        }

        if (cameraResolution == null) {
            cameraResolution = new Point((screenResolution.x >> 3) << 3, (screenResolution.y >> 3) << 3);
        }

        return cameraResolution;
    }

    /**
     * �������ɨ���ά��ķֱ��ʣ� step1:�Ȼ�ȡ���֧�ֵ����зֱ��� step2:�����֧�ֵ����зֱ������ֻ�ķֱ��ʶԱȣ�����ҵ������ȵģ����ֱ�������ֻ�ķֱ��ʣ�
     * ���û�ҵ��������ó��������ֱ��ʣ� ������û��֧�ֵķֱ��ʣ���������ɨ���ķֱ���
     */
    private static Point findBestPreviewSizeValue(Camera.Parameters parameters, CharSequence previewSizeValueString,
            Point screenResolution) {

        // ���û�к��ʵĻ���û���ֻ�û��֧�ֵķֱ��ʣ���������ɨ����С
        Camera.Size defaultPreview = parameters.getPreviewSize();
        
        Point exactPoint = null;
        // Initialize the largestSize with default value,in case that invalid point will be returned.
        largestSize = new Point(defaultPreview.width, defaultPreview.height);
        // ��Ļ�����
        double screenAspectRatio = (double) screenResolution.y / (double) screenResolution.x;
        if (previewSizeValueString != null) {
            int pixels = 0;
            int maxPixels = 0;
            for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {
                previewSize = previewSize.trim();
                int dimPosition = previewSize.indexOf('x');
                if (dimPosition < 0) {
                    continue;
                }

                int realWidth = 0;
                int realHeight = 0;
                try {
                    realWidth = Integer.parseInt(previewSize.substring(0, dimPosition));
                    realHeight = Integer.parseInt(previewSize.substring(dimPosition + 1));
                } catch (NumberFormatException nfe) {
                    continue;
                }
                pixels = realWidth * realHeight;

                if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                    continue;
                }

                // ����ѡ�����
                boolean isCandidatePortrait = realWidth < realHeight;
                int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
                int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
                double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
                double distortion = Math.abs(aspectRatio - screenAspectRatio);
                // ����ѡ�������Ļ����Ȳ���ϴ�Ͳ�ѡ�ø�ֵ
                if (distortion > MAX_ASPECT_DISTORTION) {
                    continue;
                }

                // find largest point
                if (pixels > maxPixels) {
                    maxPixels = pixels;
                    largestSize.x = realWidth;
                    largestSize.y = realHeight;
                }

                if (maybeFlippedWidth == ORG_SCREEN_W && maybeFlippedHeight == ORG_SCREEN_H) {
                    exactPoint = new Point(realWidth, realHeight);
                }
            }
            // �����ֱ���ֱ��ȡ���֧�ֵ����ֱ���
            if (exactPoint != null) {
                Log.i(TAG, "findBestPreviewSizeValue return exactPoint");
                return exactPoint;
            } else {
                Log.i(TAG, "findBestPreviewSizeValue return largestSize");
                return largestSize;
            }
        }

        Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
        Log.i(TAG, "findBestPreviewSizeValue return defaultSize");
        return defaultSize;
    }

    /**
     * �����������ʱ�����ű���
     * */
    private void setZoom(Camera.Parameters parameters) {

        String zoomSupportedString = parameters.get("zoom-supported"); // �ж��ֻ�����ʱ�Ƿ�֧������

        if (zoomSupportedString == null || (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString))) {
            return;
        }

        List<Integer> zoomList = parameters.getZoomRatios();// ��ȡ������ű����С������
        zoomValue = LARGEST_ZOOM_FACTOR;
        // zoomValue = largestSize.y / (float)cameraResolution.y;
        // //�����֧�ֵ����ֱ���/�ֻ�����ʱ���õķֱ��ʣ��õ������������ʱ�ķŴ���
        // if (zoomValue < SMALLEST_ZOOM_FACTOR) {
        // zoomValue = SMALLEST_ZOOM_FACTOR;
        // }else if(zoomValue > LARGEST_ZOOM_FACTOR){
        // zoomValue = LARGEST_ZOOM_FACTOR;
        // }
        zoomValue *= 100;
        float minDiff = Float.POSITIVE_INFINITY;
        int minIndex = -1;
        if (zoomList != null && zoomList.size() > 0) {// ��ȡ�������ʱ���յķŴ����Ӧ��������������±�ֵ
            for (int i = 0; i < zoomList.size(); i++) {
                float dist = Math.abs(zoomValue - zoomList.get(i));
                if (dist > minDiff)
                    break;
                if (dist < minDiff) {
                    minIndex = i;
                    minDiff = dist;
                }
            }
        }

        if (zoomList != null && zoomList.size() > 0) {
            parameters.set("zoom", Integer.toString(minIndex));// ��������������
        }

    }
    
    public static boolean checkCameraPermissionByAPI(Context context) {
        String permission = "android.permission.CAMERA";
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    
    public static boolean chechIfPermissionException(Exception e) {
        if (e == null) {
            Log.d(TAG, "Exception is null");
            return false;
        }

        String errorMessage = e.getMessage();
        if (!TextUtils.isEmpty(errorMessage) && errorMessage.toLowerCase().contains("permission")) {
            Log.d(TAG, "Exception message contains permission");
            return true;
        }
        Log.d(TAG, "Exception do not contains permission");
        return false;
    }

}

