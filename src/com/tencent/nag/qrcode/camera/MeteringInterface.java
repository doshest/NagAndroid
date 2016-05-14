package com.tencent.nag.qrcode.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

public final class MeteringInterface {

    private static final String TAG = MeteringInterface.class.getSimpleName();
    private static final int AREA_PER_1000 = 400;

    public static final int METERING_POSITION_X = -300;
    public static final int METERING_POSITION_Y = 0;
    public static final int METERING_OFFSET_X = 250;
    public static final int METERING_OFFSET_Y = 350;
    public static final int METERING_WEIGHT = 999;

    private MeteringInterface() {
    }

    public static void setFocusArea(Camera.Parameters parameters) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (parameters.getMaxNumFocusAreas() > 0) {
                List<Camera.Area> middleArea = buildMiddleArea();
                parameters.setFocusAreas(middleArea);
            }
        } else {
            Log.i(TAG, "Device does not support focus areas");
        }
    }

    /**
     * ���ۺ���������metering����겻��(�������������Ͻ�<-1000,-1000>)<br/>
     * ����Ϊ����ʱ���˵��<br/>
     * (-1000,-1000)----------(1000,-1000)<br/>
     * -----------------------------------<br/>
     * -----------------------------------<br/>
     * ------------ ---(0,0)--------------<br/>
     * -----------------------------------<br/>
     * -----------------------------------<br/>
     * (-1000,1000)-----------(1000,1000)<br/>
     * @param parameters
     * @return
     */
    public static boolean setMetering(Camera.Parameters parameters) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> areas = new ArrayList<Camera.Area>();
                Rect area = new Rect(METERING_POSITION_X - METERING_OFFSET_X, METERING_POSITION_Y - METERING_OFFSET_Y,
                        METERING_POSITION_X + METERING_OFFSET_X, METERING_POSITION_Y + METERING_OFFSET_Y);
                areas.add(new Camera.Area(area, METERING_WEIGHT));
                parameters.setMeteringAreas(areas);
                return true;
            } else {
                Log.i(TAG + "setMetering", "Device does not support metering areas");
                return false;
            }
        } else {
            Log.i(TAG + "setMetering", "Device does not support metering areas");
            return false;
        }
    }

    private static List<Camera.Area> buildMiddleArea() {
        return Collections.singletonList(new Camera.Area(new Rect(-AREA_PER_1000, -AREA_PER_1000, AREA_PER_1000,
                AREA_PER_1000), 1));
    }
}

