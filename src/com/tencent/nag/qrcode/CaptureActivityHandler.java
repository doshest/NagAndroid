package com.tencent.nag.qrcode;

import com.sina.barcode.DecodeState;
import com.sina.barcode.QRcodeConst;
import com.tencent.nag.qrcode.camera.AutoFocusManager;
import com.tencent.nag.qrcode.camera.CameraManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * 处理二维码扫描的Handler类，解码结果后继处理
 * 
 * @author wenfeng3
 */
public final class CaptureActivityHandler extends Handler {

	private static final String TAG = "CaptureActivityHandler";
    private static final int METERING_DELAY = 100;

    private final CaptureActivity mCaptureActivity;
    private final DecodeThread mdecodeThread;

    CaptureActivityHandler(CaptureActivity activity) {
        this.mCaptureActivity = activity;
        mdecodeThread = new DecodeThread(activity);
        mdecodeThread.start();
    }

    /**
     * 开始扫描二维码和解码
     * */
    public void init() {
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
        case QRcodeConst.MSG_RESTART_PREVIEW:// 扫描和解码
            restartPreviewAndDecode();
            break;
        case QRcodeConst.MSG_DECODE_SUCCEEDED: {// 解码成功
            DecodeState state = DecodeState.getDecodeStateFromBundle(message.getData());
            if (state != null) {
                mCaptureActivity.handleDecodeSucc((DecodeResult) message.obj, state);
            }
            break;
        }
        case QRcodeConst.MSG_DECODE_FAILED: {// 解码失败
            DecodeState state = DecodeState.getDecodeStateFromBundle(message.getData());
            if (state != null) {
                mCaptureActivity.handleDecodeFailed(mdecodeThread.getHandler(), state);
            }
            break;
        }
        case QRcodeConst.MSG_DECODE_FROM_GALLERY: {// 从相册中解码
            Handler decodeHandler = mdecodeThread.getHandler();
            Message decodeMsg = decodeHandler.obtainMessage(QRcodeConst.MSG_DECODE);
            decodeMsg.setData(message.getData());
            decodeMsg.sendToTarget();
            break;
        }
        }
    }

    /**
     * 退出同步处理
     * */
    public void quitSynchronously(boolean destory) {
        CameraManager.get().stopPreview(destory);

        Message quit = Message.obtain(mdecodeThread.getHandler(), QRcodeConst.MSG_QUIT);
        quit.sendToTarget();
        try {
            mdecodeThread.join();
        } catch (InterruptedException e) {

        }
        removeMessages(QRcodeConst.MSG_DECODE_SUCCEEDED);
        removeMessages(QRcodeConst.MSG_DECODE_FAILED);
    }

    /**
     * 重新开始扫码
     * */
    private void restartPreviewAndDecode() {
        CameraManager.get().requestPreviewFrame(mdecodeThread.getHandler(), QRcodeConst.MSG_DECODE,
                DecodeState.buildBundle(mCaptureActivity.getCurrentDecodeState()));
        AutoFocusManager af = CameraManager.get().getAutoFocusManager();
        if (af != null) {
            af.startIntime();
        }
        mCaptureActivity.drawViewfinder();

    }

    /**
     * 几乎所有机型，都必须在preview之后设置metering才有效，有一些机型还需要等待一会会，为了兼容，全部等待100毫秒后设置metering
     */
    public void delaySetMetering() {
        Log.i(TAG, "delaySetMetering");
        postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraManager.get().setMeteraing();
            }
        }, METERING_DELAY);
    }
}
