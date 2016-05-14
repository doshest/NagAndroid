package com.tencent.nag.qrcode;

import java.util.concurrent.CountDownLatch;

import android.os.Handler;
import android.os.Looper;

/**
 * 解码过程中用于处理图片的费时操作.
 * 
 * @author 
 */
final class DecodeThread extends Thread {

    public static final String BARCODE_BITMAP = "barcode_bitmap";

    private final CaptureActivity activity;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(CaptureActivity activity) {
        this.activity = activity;
        handlerInitLatch = new CountDownLatch(1);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
        	
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
