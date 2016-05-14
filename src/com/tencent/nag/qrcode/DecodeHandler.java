package com.tencent.nag.qrcode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.sina.barcode.BarCodeFormat;
import com.sina.barcode.BarCodeFormatMask;
import com.sina.barcode.BarCodeResult;
import com.sina.barcode.DecodeState;
import com.sina.barcode.PicBarCodeScanner;
import com.sina.barcode.QRcodeConst;
import com.sina.barcode.VideoBarCodeScanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;


/**
 * ����Handler��Message������JNI����C������н��봦�����
 * */
final class DecodeHandler extends Handler {

    private final CaptureActivity activity;

    DecodeHandler(CaptureActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
        case QRcodeConst.MSG_DECODE: // ��ʼ����
            DecodeState state = DecodeState.getDecodeStateFromBundle(message.getData());
            if (state == null) {
                return;
            }
            if (state.isCamera()) {
                // message.arg1ͼƬ�Ŀ�ȣ� message.arg2ͼƬ�ĸ߶�
                decodeFromCamera(state, (byte[]) message.obj, message.arg1, message.arg2);
            } else if (state.isGallery()) {
                Bundle b = message.getData();
                String path = "";
                if (b != null) {
                    path = b.getString(QRcodeConst.MSG_DECODE_PICTURE_PATH);
                    if (!TextUtils.isEmpty(path)) {
                        decodeFromGallery(state, path);
                    }
                }
            }
            break;
        case QRcodeConst.MSG_QUIT:
            Looper.myLooper().quit();
            break;
        }
    }

    private static boolean isQrcode(int format) {
        if (format == BarCodeFormat.QR_CODE) {
            return true;
        }
        return false;
    }

    private static boolean isBarcode(int format) {
        if (format == BarCodeFormat.QR_CODE//
                || format == BarCodeFormat.EAN_8 //
                || format == BarCodeFormat.EAN_13//
                || format == BarCodeFormat.UPC_A//
                || format == BarCodeFormat.UPC_E //
                || format == BarCodeFormat.CODE_39//
                || format == BarCodeFormat.CODABAR//
                || format == BarCodeFormat.CODE_93 //
                || format == BarCodeFormat.CODE_128 //
                || format == BarCodeFormat.ITF) {
            return true;
        }
        return false;
    }

    /**
     * ����QR����Bar��Ӧ��mask�����ص�mask�������վɴ�����isQrcode��isBarcode���ж�
     * 
     * @return
     */
    private static int getQrcodeMask() {
        return BarCodeFormatMask.ENABLE_QR_CODE;
    }

    private static int getAllCodeMask() {
        return BarCodeFormatMask.ENABLE_QR_CODE //
                | BarCodeFormatMask.ENABLE_CODABAR //
                | BarCodeFormatMask.ENABLE_CODE_39//
                | BarCodeFormatMask.ENABLE_CODE_93 //
                | BarCodeFormatMask.ENABLE_CODE_128 //
                | BarCodeFormatMask.ENABLE_EAN_8//
                | BarCodeFormatMask.ENABLE_EAN_13 //
                | BarCodeFormatMask.ENABLE_ITF //
                | BarCodeFormatMask.ENABLE_UPC_A //
                | BarCodeFormatMask.ENABLE_UPC_E//
                | BarCodeFormatMask.ENABLE_UPC_EAN_EXTENSION;
    }

    /**
     * ���ͼƬ·������ȡͼƬ��Bitmap��ʽ
     * @param path ͼƬ·��
     * @param rect ͼƬ�ֱ���
     * @return Bitmap
     */
    private static Bitmap decodePicture(String path) {
        final int MAX_IMAGE_RESOLUTION = 1280 * 720;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bitmap = null;

        // bitmap_piexls���ͼƬ�ķֱ���
        Long bitmap_piexls = null;

        // opts.inJustDecodeBounds����Ϊtrueʱ����ȡͼƬ��ͷ��������ȡbitmap������������ʡ�ڴ�
        opts.inJustDecodeBounds = true;
        attemptDecodeBitmap(path, opts);
        if (opts != null) {
            bitmap_piexls = (long) (opts.outWidth * opts.outHeight);
        }
        // Ϊ��ʡ�ڴ棬���ͼƬ�Ĵ�С��1920*1080�����ͼƬ���в����ȡ
        if (bitmap_piexls > MAX_IMAGE_RESOLUTION) {
            opts.inSampleSize = (int) Math.round(Math.sqrt(bitmap_piexls / MAX_IMAGE_RESOLUTION));
        }
        opts.inJustDecodeBounds = false;
        bitmap = attemptDecodeBitmap(path, opts);
        // }

        return bitmap;
    }

    private static Bitmap attemptDecodeBitmap(String path, BitmapFactory.Options opts) {
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeFile(path, opts);
        } catch (OutOfMemoryError e) {
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
            bmp = null;
        }
        return bmp;
    }

    /**
     * ������ĵ��Ķ�ά����룬ͨ��JNI����C�����еķ���
     * 
     * @param data YUVԤ��֡.
     * @param width Ԥ��֡�Ŀ��
     * @param height Ԥ��֡�ĸ߶�
     */
    private void decodeFromCamera(DecodeState state, byte[] data, int width, int height) {
        if (data == null) {
            return;
        }


        VideoBarCodeScanner decoder = activity.getVedioBarCodeScanner();
        BarCodeResult mResult = activity.getBarCodeResult();
        Rect roi = activity.getVedioDecodeROIRect();
        // roi = null;
        if (QRcodeConst.isSaveToSD) {
           // YuvImage image = new YuvImage(data, activity.getCameraManager().getCameraParameters().getPreviewFormat(),
              //      width, height, null);
         //   storeImage(image/* , roi */);
        	//save to SDCard abort this method
            QRcodeConst.isSaveToSD = false;
        }

        boolean success = false;
        try {
            if (mResult != null) {
            	 success = decoder.barCodeScanYUV(data, width, height, getAllCodeMask(), roi, mResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        invokeActivityHandler(success, state, mResult);
    }

    /**
     * 
     * ��JNI���صĶ���BarCodeResult��ֵ��ΪӦ����ʹ�õ�DecodeResult
     * 
     * @param success JNI�����Ƿ�ɹ�
     * @param isQrcode �ж��������뻹�Ƕ�ά��
     * @param state
     * @param mResult JNI���ص�Result����
     */
    private void invokeActivityHandler(boolean success, DecodeState state, BarCodeResult mResult) {

        DecodeResult rawResult = null;
        if (success && !TextUtils.isEmpty(mResult.getBarCodeString())) {

                if (isQrcode(mResult.getBarCodeformat())) {
                    rawResult = new DecodeResult(mResult.getBarCodeString(), DecodeResult.RESULT_FORMATE_QRCODE);
                } else if (isBarcode(mResult.getBarCodeformat())) {
                    rawResult = new DecodeResult(mResult.getBarCodeString(), DecodeResult.RESULT_FORMATE_BARCODE);
                }
        }
        if (rawResult != null && !TextUtils.isEmpty(rawResult.getText())) {
            Message message = Message.obtain(activity.getHandler(), QRcodeConst.MSG_DECODE_SUCCEEDED, rawResult);
            message.setData(DecodeState.buildBundle(state));
            message.sendToTarget();
        } else {
            Message message = Message.obtain(activity.getHandler(), QRcodeConst.MSG_DECODE_FAILED);
            message.setData(DecodeState.buildBundle(state));
            message.sendToTarget();
        }

    }

    /**
     * ������л�ȡͼƬ������
     * */
    private void decodeFromGallery(DecodeState state, String picPath) {
    //    boolean isQrcode = state.isQrcode();
        PicBarCodeScanner mBarCodeScanner = new PicBarCodeScanner();
        BarCodeResult mResult = new BarCodeResult();
        boolean success = false;
        Bitmap bitmap = decodePicture(picPath);
        if (bitmap != null) {
            try {
                if (mResult != null) {
                    success = mBarCodeScanner.barCodeScanBitmap(bitmap, getAllCodeMask(), mResult);
                }
            } catch (Exception re) {
            }
        }
        invokeActivityHandler(success, state, mResult);

    }

    /**
     * ��������ͼƬ��SD�������ֻΪ��ά����ߵ����õģ���������Ҫ�Ľ��ɾ��֮
     * */
    /*
    private void storeImage(YuvImage image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            Rect roi = new Rect();
            roi.top = 0;
            roi.left = 0;
            roi.bottom = image.getHeight();
            roi.right = image.getWidth();
            // }
            image.compressToJpeg(roi, 100, fos);
            fos.close();
            // Toast.makeText(activity,"ͼƬ����ɹ�",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

  
     * ����һ��ý���ļ�

    private File getOutputMediaFile() {
        // Create a media file name
        java.util.Date now = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(now);
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".bmp";
        String dir = Environment.getExternalStorageDirectory().toString() + "/qrcode/";
        if (!FileUtils.isDirectoryExist(dir)) {
            FileUtils.mkdirs(new File(dir));
        }
        mediaFile = new File(dir + mImageName);
        return mediaFile;
    }
*/
}

