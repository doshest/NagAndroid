package com.tencent.nag.qrcode;


import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;






import com.example.facedemo.R;
import com.sina.barcode.BarCodeResult;
import com.sina.barcode.DecodeState;
import com.sina.barcode.QRcodeConst;
import com.sina.barcode.VideoBarCodeScanner;
import com.tencent.nag.qrcode.camera.CameraConfigurationManager;
import com.tencent.nag.qrcode.camera.CameraManager;
import com.tencent.nag.qrcode.view.ViewFinderView;


@TargetApi(Build.VERSION_CODES.CUPCAKE)
@SuppressLint("NewApi")
/**
 * 此类是用来扫描二维、条形码
 * */
@SuppressWarnings("all")
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, OnClickListener {

    // 特殊场景下:如JsBridge调用,只需要初始结果, 不做任何处理
    public static final String REQUEST_RAW_RESULT = "request_raw_result";
    public static final String KEY_QR_RAW_RESULT = "key_qr_raw_result";
    //从相册选取
    public final static int REQUEST_CODE_PICK = 0x123;

    private static final int DELAY = 3000;
    private static final String TAG = "CaptureActivity";

    /** 相机被占用的错误信息 @see {@link CaptureActivity#displayFrameworkBugMessageAndExit(int)} */
    private static final int WRONG_WITH_CAMERA_OCCUPIED = 1;
    /** 相机权限问题的错误信息 @see {@link CaptureActivity#displayFrameworkBugMessageAndExit(int)} */
    private static final int WRONG_WITH_CAMERA_PERMISSION_DENIED = 2;
    /** 初始化相机成功 */
    private static final int INIT_CAMERA_SUCCESS = 0;
    /** 初始化相机失败 */
    private static final int INIT_CAMERA_FAILURE = -1;

    private volatile DecodeState mCurrentDecodeState = DecodeState.buildQrcodeCameraState();;
    /** 标识是否可以打开特殊逻辑的Dialog */
    private volatile boolean canShowDialog = true;
    /** 拍照的界面（画布）是否准备好 */
    private boolean hasSurface;
    /** 标识是否需要处理扫描结果，js调用不需要任何处理，直接将结果返回 */
    private boolean shouldReturnRawResult;
    /** 是否支持特殊逻辑 */
//    private boolean isSupportedSpecial = false;
    private int mSoundId;

    private RelativeLayout rlLightParent;
    private TextView tvLight;
    private int mTabBarTextColorNotSelected;
    private Drawable mQrcodeDrawable;
    private Drawable mQrcodeHighlightedDrawable, mBarcodeHighlightedDrawable;


    /** 通过拍照获取的二维码或是条形码处理类 */
    private VideoBarCodeScanner mVedioBarCodeScanner;
    /** 取景框（扫描框） */
    private Rect mRect;
    /** 扫描条码的结果 */
    private BarCodeResult mBarCodeResult;
    /** 二维码处理Handler */
    private CaptureActivityHandler mCaptureActivityHandler;
    /** 扫描框View */
    private ViewFinderView mViewfinderView;
    /** SurfaceView拥有独立的绘图表面，可以实现复杂高效的UI，且不会使用户输入得不到及时响应 */
    private SurfaceView mSurfaceView;

    private Timer mSpTimer;
    private InactivityTimer mInactivityTimer;
    /** 扫描二维码结束播放音乐的 */
    private SoundPool mSoundPool;

    /** 换成透明Titlebar，不采用父类的Titlebar,类似于话题等页面的Title */
    private RelativeLayout rlQrCodeTitleBar;
    private TextView tvQrCodeTitle, tvQrCodeRigthBtn;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	super.onCreate(savedInstanceState);
       // externalProcess();

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.qrcode_capture_activity);
        initSkin();

        CameraManager.init(getApplication());// 初始化相机
        initSoundPool();
       // setTvLigthVisuableOrNot(); // 是否显示闪光灯

        mCaptureActivityHandler = null;
        hasSurface = false;
        mInactivityTimer = new InactivityTimer(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        doResumeProcess();
    }

    @Override
    protected void onPause() {
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.quitSynchronously(false);
            mCaptureActivityHandler = null;
            CameraManager.get().closeDriver();
        }

        if (!hasSurface) {
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }

        mViewfinderView.setProcessBarInVisuable();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mInactivityTimer != null) {
            mInactivityTimer.shutdown();
        }
        if (mSoundPool != null) {
            try {
                mSoundPool.release();
            } catch (Exception e) {
            }
        }

        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.quitSynchronously(true);
            mCaptureActivityHandler = null;
            CameraManager.get().closeDriver();
        }

        mQrcodeDrawable = null;
        mQrcodeHighlightedDrawable = null;
        
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CODE_PICK:
        	
        	//TODO
            if (resultCode == RESULT_OK) {// 从相册选择完图片后进行处理
                if (data == null) {
                    return;
                }
               //uri
                
                Uri originalUri = data.getData(); 
                //System.out.println("pic uri "+originalUri.toString());
                String[] proj = {MediaStore.Images.Media.DATA};

                //好像是android多媒体数据库的封装接口，具体的看Android文档

                Cursor cursor = managedQuery(originalUri, proj, null, null, null); 
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
              //将光标移至开头 ，这个很重要，不小心很容易引起越界

                cursor.moveToFirst();
                //最后根据索引值获取图片路径
                String path = cursor.getString(column_index);
                //System.out.println("pic path "+path);
                File picFile = new File(path);
                if (!picFile.exists()) {
                    return;
                }

                doResumeProcess();

                mViewfinderView.setProcessBarVisuable();
                changeDecodeState(DecodeState.buildQrcodeGalleryState());

                Message message = mCaptureActivityHandler.obtainMessage(QRcodeConst.MSG_DECODE_FROM_GALLERY);
                Bundle b = DecodeState.buildBundle(getCurrentDecodeState());
                b.putString(QRcodeConst.MSG_DECODE_PICTURE_PATH, path);
                message.setData(b);
                mCaptureActivityHandler.sendMessage(message);
            }
            break;

        default:
            break;
        }
    }




    public void initSkin() {
      

        tvQrCodeRigthBtn = (TextView) findViewById(R.id.tvQrCodeRight);
        tvQrCodeRigthBtn.setVisibility(View.VISIBLE);
        tvQrCodeRigthBtn.setOnClickListener(this);
      
        mSurfaceView = (SurfaceView) findViewById(R.id.preview_view);// 整 个界面

       // = (TextView) findViewById(R.id.ivQrCodeBack);
  
      //  tvQrCodeBack.setOnClickListener(this);//关闭

        tvLight = (TextView) findViewById(R.id.lightTxt);
         tvLight.setOnClickListener(this);
        rlLightParent = (RelativeLayout) tvLight.getParent();

        mViewfinderView = (ViewFinderView) findViewById(R.id.viewfinder_view);
       
    }

    public DecodeState getCurrentDecodeState() {
        return this.mCurrentDecodeState;
    }

    public BarCodeResult getBarCodeResult() {
        return mBarCodeResult;
    }


    /** 获取通过拍照获取的二维码或是条形码的处理类的实例 */
    public VideoBarCodeScanner getVedioBarCodeScanner() {
        return mVedioBarCodeScanner;
    }

    /** 获取扫描框矩形框 */
    public Rect getVedioDecodeROIRect() {
        return mRect;
    }

    public Handler getHandler() {
        return mCaptureActivityHandler;
    }

    /** 获取相机管理类的实例 */
    public CameraManager getCameraManager() {
        return CameraManager.get();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    // Don't display the share menu item if the result overlay is showing.
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    /**
     * 创建拍照的画布
     * */
    public void surfaceCreated(SurfaceHolder holder) {
        int initCameraSuccess = INIT_CAMERA_SUCCESS;
        if (!hasSurface) {
            hasSurface = true;
            initCameraSuccess = initCamera(holder);
        }

        if (initCameraSuccess == INIT_CAMERA_FAILURE) {
            Log.d(TAG, "initCameraSuccess == -1 in surfaceCreated, initCamera filed just return");
            return;
        }
        
        try {
        	// 取景框代码添加 add version 460 modified by wenfeng3
            // 获取手机的分辨率和相机的分辨率 ，相机分辨率和手机分辨率x,y 是反着的
        	Point screenResolution = CameraManager.get().getConfigManager().getScreenResolution();
            Point cameraResolution = CameraManager.get().getConfigManager().getCameraResolution();

            // 计算相机分辨率和手机分辨率的比例
            float scaleX = cameraResolution.x / (float) screenResolution.y;
            float scaleY = cameraResolution.y / (float) screenResolution.x;

            // 获取取景框，就是中间扫描二维码的那个矩形框
            mRect = mViewfinderView.getFrameReleativeRect();

            // 获取取景框的中心坐标
            int centerY = (int) ((mRect.left + mRect.right) / 2 * scaleY);
            int centerX = (int) ((mRect.top + mRect.bottom) / 2 * scaleX);
            
            // 获取 标题栏高度和 状态栏的高度
            int titleBarHeight = this.getResources().getDimensionPixelSize(R.dimen.page_title_height);
            int statusBarHeight = 0;
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object obj = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = Integer.parseInt(field.get(obj).toString());

                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            // 状态栏和titleBar的 高度
            int offsetX0 = (int) ((titleBarHeight + statusBarHeight) * scaleX);
            // 获取底部标题栏的高度
            int bottomBarHeight = this.getResources().getDimensionPixelSize(R.dimen.qrcode_bottom_tab_height);
            int offsetX1 = (int) ((screenResolution.y - bottomBarHeight) * scaleX);

            // 下面获取的以取景框为中心，以32为步长，往上、下、左、右四个方向走，取到最大的框作为二维码处理的大小，分配内存也用这个
            final int step = 32;

            int left = centerX;
            for (; left > 0; left -= step)
                ;
            if (left != 0)
                left += step;
            int right = left + step - 1;
            for (; right < offsetX1; right += step)
                ;
            right -= step;

            int top = centerY;
            for (; top > 0; top -= step)
                ;
            if (top != 0)
                top += step;
            int bottom = top + step - 1;
            int height = (int) (screenResolution.x * scaleY);
            for (; bottom < height; bottom += step)
                ;
            bottom -= step;

            mRect.left = left;
            mRect.right = right;
            mRect.bottom = bottom;
            mRect.top = top;

            int rectWidth = (mRect.right - mRect.left + 1);
            int rectHeight = (mRect.bottom - mRect.top + 1);

            Log.d(TAG, "getJNIRect " + " left:" + left + " top:" + top + " right:" + right + " bottom:" + bottom);

            if (mVedioBarCodeScanner != null) {
                // 开放的内存大小,在OnStop操作中需要将内存释放
                if (rectWidth > 0 && rectHeight > 0) {
                    mVedioBarCodeScanner.initScanner(rectWidth * rectHeight);
                } else {
                    android.util.Log.e("qrcode", "rectwdith = " + rectWidth);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Catch exception in surfaceCreated, maybe permission problem existed.", e);
        }

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * 重新扫描
     */
    public void restartScan() {
        this.resetStatusView();
        if (mCaptureActivityHandler != null) {
            mCaptureActivityHandler.sendEmptyMessage(QRcodeConst.MSG_RESTART_PREVIEW);
        }
    }

    /**
     * 扫码成功,显示扫码结果
     * 
     * @param rawResult 识别的二维码的内容
     * @param state 解码出来的灰度图片
     * 
     */
    public void handleDecodeSucc(DecodeResult rawResult, DecodeState state) {
        DecodeState cpState = state.copy();
        if (onInterceptDecodeSucc(state) || rawResult == null) {
            return;
        }
        mViewfinderView.setProcessBarInVisuable();
        mInactivityTimer.onActivity();

        final String res = rawResult.getText();
        if (TextUtils.isEmpty(res)) {
            openAlertDialog(getNotIdentificationTitle(), null, null, false, " ", // 空
                    getResetRunnable(cpState), null);
        } else {
            onDecodeFinishing(rawResult, cpState);
        }
    }

    /**
     * 扫码失败处理函数
     * 
     * @param handler 重新开始任务的handler
     * @param state 解码出来的灰度图片
     * 
     */
    public void handleDecodeFailed(final Handler handler, DecodeState state) {
        DecodeState cpState = state.copy();
        mViewfinderView.setProcessBarInVisuable();
        if ((getCurrentDecodeState().equals(cpState)) && (cpState.isGallery())) {
            if (isFinishing()) {
                return;
            }
            //System.out.println("fail inner");
            AlertDialog.Builder builder = new Builder(this);
            builder.setMessage("没有可以识别的二维码");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					   doResumeProcess();
                       SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
                       initCamera(surfaceHolder);
                       dialog.dismiss();
				}
			});
            
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					  resetGalleryAction().run();
					  dialog.dismiss();
				}
			});
            builder.create().show();
            if (isFinishing()) {
                return;
            }
          

        } else {
            CameraManager.get().requestPreviewFrame(handler, QRcodeConst.MSG_DECODE,
                    DecodeState.buildBundle(getCurrentDecodeState()));
        }
    }

    /**
     * 开始绘制扫描线
     * */
    public void drawViewfinder() {
        mViewfinderView.startDrawViewfinder();
    }

    

    /**
     * 改变当前的解码状态
     * */
    public void changeDecodeState(DecodeState state) {
        if (state == null || mCurrentDecodeState.equals(state)) {
            return;
        }
        mCurrentDecodeState = state;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.lightTxt ) {
        //TODO 开闪光灯
             CameraManager cm = CameraManager.get();
             cm.toggleFlashLight();
             updateLight(cm.isFlashLighting());
        } //else if (v.getId() == R.id.ivQrCodeBack) {
         //   finish();
       // }
    else if (v.getId() == R.id.tvQrCodeRight) {
            startToPhotoAlbumActivity();
        }
    }


    /**
     * 扫码识别成功后处理操作
     * @param decodeResult 解码结果
     * @param state 解码状态
     * */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void onDecodeFinishing(final DecodeResult decodeResult, final DecodeState state) {
        if (onInterceptDecodeSucc(state) || decodeResult == null) {
            return;
        }
        String res = decodeResult.getText();
        playRings();
        if (decodeResult != null) {
            if (decodeResult.getFormat() == DecodeResult.RESULT_FORMATE_QRCODE) {
            	//System.out.println("gg qrcode "+res+"  res");
            	/*
            	Intent intent = new Intent();        
                intent.setAction("android.intent.action.VIEW");    
                Uri content_url = Uri.parse(res);   
                intent.setData(content_url);  
                startActivity(intent);
                finish();*/
            	Intent i = new Intent(CaptureActivity.this,StartActivity.class);
            	i.putExtra("result", decodeResult);
            	setResult(RESULT_OK,i);
            	//startActivity(i);
            	finish();
            } else if (decodeResult.getFormat() == DecodeResult.RESULT_FORMATE_BARCODE) {
            	//System.out.println("gg barcode "+res+"   res");
            	Intent i = new Intent(CaptureActivity.this,StartActivity.class);
            	i.putExtra("result", decodeResult);
            	setResult(RESULT_OK,i);
            	//startActivity(i);
            	finish();
            	//Toast.makeText(CaptureActivity.this, "条码："+res, 1000).show();
            	try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
            	restartScan();
            }
        }
        if (!TextUtils.isEmpty(res)) {

            if (shouldReturnRawResult) {
                Intent data = new Intent();
                data.putExtra(KEY_QR_RAW_RESULT, res);
                setResult(RESULT_OK, data);
                finish();
                return;
            }
          
        }
    }
 
    /**
     * 根据本地函数识别二维码或是条形码的结果，传至服务器进行处理
     * */
	private class QrcodeTask extends AsyncTask<Object, Void, Object> {
        private QrCodeResult qrres = null;
        private int resCode = -1;
        DecodeState state;
        String newUrl;

        public QrcodeTask(DecodeState state) {
            this.state = state.copy();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (onInterceptDecodeSucc(state)) {
                return;
            }
            mViewfinderView.setProcessBarVisuable();
        }

        @TargetApi(Build.VERSION_CODES.CUPCAKE)
		@Override
        protected Object doInBackground(Object... params) {
            newUrl = (String) params[0];
            final DecodeResult decodeResult = (DecodeResult) params[1];
            //System.out.println(decodeResult.getText()+"  format "+decodeResult.getFormat());

            if (qrres == null && resCode == -1) {
                resCode = 1;
            }
            return null;
        }

        @SuppressLint("NewApi")
		@Override
        protected void onCancelled() {
            super.onCancelled();
            if (onInterceptDecodeSucc(state)) {
                return;
            }
            mViewfinderView.setProcessBarInVisuable();
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (onInterceptDecodeSucc(state)) {
                return;
            }
            mViewfinderView.setProcessBarInVisuable();
            switch (resCode) {
            case 1:
            case 2: {
                openAlertDialog(getNotIdentificationTitle(), null, null, false, newUrl + "",
                        getResetRunnable(state), null);
                break;
            }
            case 3: {
             	Toast.makeText(CaptureActivity.this, "huoqu shibai ", Toast.LENGTH_LONG);
                break;
            }
            default:
                processQRCode(qrres, state);
                break;
            }
        }

    }

    /**
     * 根据服务器处理的二维码结果，进行最终展现处理函数
     * @param res 扫描的结果
     * @param state 当前的扫描状态
     * */
    private void processQRCode(final QrCodeResult res, final DecodeState state) {
        final String action = res.getAction();

        boolean urlResult = false;
        String codeUrl = res.getUrl();
      //  System.out.println("codeURL"+codeUrl); 
    }

    /**
     * 扫描结果的Action为Open时，不以http开头的URL时，靠Scheme来跳转
     * */
    private void stepByScheme(String url, boolean disableSinaUrl, Bundle bundle, String log, String title,
            DecodeState state, QrCodeResult res) {
        boolean re = true;//SchemeUtils.openScheme(CaptureActivity.this, url, null, disableSinaUrl, bundle);

        if (disableSinaUrl) {
         //   Utils.recordActCodeLog(UserActLogCenter.ACT_CODE_OPEN_URL_DIRECT, url, log, getStatisticInfoForServer());
        }

        if (re) {
            finish();
        } else {
            openAlertDialog(title, null, null, false, res.getQr() + "", getResetRunnable(state), null);
        }
    }

    /**
     * 拍照时只需重新扫描
     * */
    private Runnable resetAction() {
        return new Runnable() {
            @Override
            public void run() {
                restartScan();
            }
        };
    }

    /**
     * 打开相册后改变解码状态，是条形码还是二维码
     * */
    private Runnable resetGalleryAction() {
        return new Runnable() {
            @Override
            public void run() {
                DecodeState curState = getCurrentDecodeState();
                if (curState.isGallery()) {
                //    if (curState.isQrcode()) {
                        changeDecodeState(DecodeState.buildQrcodeCameraState());
                  //  } else if (curState.isBarcode()) {
                     //   changeDecodeState(DecodeState.buildBarcodeCameraState());
                  //  }
                    restartScan();
                }
            }
        };
    }

    /**
     * 当调用相机或是初始化相机失败时，弹出错误的对话框
     * */
    private void displayFrameworkBugMessageAndExit(int wrongMessage) {
    	   AlertDialog.Builder builder = new Builder(this);
           builder.setMessage("没有可以识别的二维码");
           builder.create().show();
    }

    /**
     * 打开Dialog展示识别的结果
     * */
    private void openAlertDialog(String titleText, String okText, String cancelText, boolean cancelable, String msg,
            final Runnable okAction, final Runnable cancelAction) {
    	
        if (isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				   doResumeProcess();
                   SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
                   initCamera(surfaceHolder);
                   dialog.dismiss();
			}
		});
        
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				  resetGalleryAction().run();
				  dialog.dismiss();
			}
		});
        builder.create().show();
    }

    /**
     * 判断是否支持闪光灯
     * */
    @TargetApi(Build.VERSION_CODES.DONUT)
	private boolean isSupportFlashLight() {
        boolean ret = false;
        String flash = "android.hardware.camera.flash";
        Method method;
        Field field;
        String name;
        try {
            method = getPackageManager().getClass().getMethod("getSystemAvailableFeatures", null);
            Object[] infos = (Object[]) method.invoke(getPackageManager(), null);
            for (Object f : infos) {
                field = f.getClass().getField("name");
                name = (String) field.get(f);
                if (flash.equals(name)) {
                    ret = true;
                    break;
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    private boolean isFlashLightOK(){
    	
    	return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * 判断手机是否支持闪光灯，支持则显示开闪光灯按钮
     * */
    private void setTvLigthVisuableOrNot() {
        if (android.os.Build.VERSION.SDK_INT > 4) {
            // wangwentao 2013-3-29 手机不支持闪光灯功能时，扫描界面还显示闪光灯 [s]
            if (isSupportFlashLight()) {
                rlLightParent.setVisibility(View.VISIBLE);
                tvLight.setText("闪光灯");
          //      tvLight.setVisibility(View.VISIBLE)
            } else {
                rlLightParent.setVisibility(View.GONE);
            }
            // wangwentao 2013-3-29 手机不支持闪光灯功能时，扫描界面还显示闪光灯 [e]
        } else {
            rlLightParent.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 扫码成功后播放声音
     */
    private void playRings() {
        AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        if (currentVolume == 0) {// silence mode
            return;
        }
        mSoundPool.play(mSoundId, currentVolume, currentVolume, 0, 0, 1);
    }

    /**
     * 获取跳转是否能打开该扫描类
     * */
    private void externalProcess() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        shouldReturnRawResult = intent.getBooleanExtra(REQUEST_RAW_RESULT, false);
        String scheme = intent.getScheme();
       // if (!SchemeConst.WEIBO_URI_SCHEME.equals(scheme)) {
          //  return;
      //  }
        Uri data = intent.getData();
        if (!data.isHierarchical()) {
            return;
        }
        String host = data.getHost();
      // System.out.println("878 "+host);
    }

    /**
     * 更新闪光灯状态
     * */
    private void updateLight(boolean open) {
        if (open) {
            tvLight.setText("关闭闪光灯");
        } else {
            tvLight.setText("打开闪光灯");
        }
    }

    /**
     * 重新回到CaptureActivity类时需要进行的一些操作
     * */
    private void doResumeProcess() {

        resetStatusView();
        updateLight(CameraManager.get().isFlashLighting());

        if (mCaptureActivityHandler == null) {
            mBarCodeResult = new BarCodeResult();
            mVedioBarCodeScanner = new VideoBarCodeScanner();
            mCaptureActivityHandler = new CaptureActivityHandler(this);
        }

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    /**
     * 初始化相机
     * @param surfaceHolder
     * @return 成功返回 {@link #INIT_CAMERA_SUCCESS},失败返回 {@link #INIT_CAMERA_FAILURE}
     * 
     */
    private int initCamera(SurfaceHolder surfaceHolder) {

        if (CameraManager.get().isOpen()) {
            return INIT_CAMERA_SUCCESS;
        }

        Log.d(TAG, "Check camera permission in initCamera");
        if (!CameraConfigurationManager.checkCameraPermissionByAPI(getApplicationContext())) {
            Log.d(TAG, "Find camera permission denied by checking");
            displayFrameworkBugMessageAndExit(WRONG_WITH_CAMERA_PERMISSION_DENIED);
            return INIT_CAMERA_FAILURE;
        }

        try {
           // QRLogUtil.recodeInitCameraDateINInitCamera();
        	Log.d(TAG, "start  "+ System.currentTimeMillis());
            CameraManager.get().openDriver(surfaceHolder);
           // startUpSpecialLogic();

            if (mCaptureActivityHandler != null) {
            	  Log.e(TAG, "initCamera scanDate = " + System.currentTimeMillis());
                mCaptureActivityHandler.init();
                mCaptureActivityHandler.delaySetMetering();
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Catch IOException in initCamera, show user the occupied message", ioe);
            displayFrameworkBugMessageAndExit(WRONG_WITH_CAMERA_OCCUPIED);
            return INIT_CAMERA_FAILURE;
        } catch (RuntimeException e) {
            Log.e(TAG, "Catch RuntimeException in initCamera, show user the permission message", e);
            if (CameraConfigurationManager.chechIfPermissionException(e)) {
                displayFrameworkBugMessageAndExit(WRONG_WITH_CAMERA_PERMISSION_DENIED);
            } else {
                displayFrameworkBugMessageAndExit(WRONG_WITH_CAMERA_OCCUPIED);
            }
            return INIT_CAMERA_FAILURE;
        } catch (Exception e) {
            Log.e(TAG, "Catch unkonw Exception in initCamera, show user the occupied message", e);
            displayFrameworkBugMessageAndExit(WRONG_WITH_CAMERA_OCCUPIED);
            return INIT_CAMERA_FAILURE;
        }

        return INIT_CAMERA_SUCCESS;
    }

    /**
     * 设置viewfinderView（即扫描框）可见
     * */
    private void resetStatusView() {
        mViewfinderView.setVisibility(View.VISIBLE);
    }
    /**
     * 跳转至相册，选择照片
     * 
     */
    private void startToPhotoAlbumActivity() {
       //TODO 打开系统相册
    	Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
    	getAlbum.setType("image/*");
    	startActivityForResult(getAlbum, REQUEST_CODE_PICK);
    }

    /**
     * 初始化扫描结束成功时的声音
     * */
    private void initSoundPool() {
        mSoundPool = new SoundPool(5, AudioManager.STREAM_NOTIFICATION, 0);
        mSoundId = mSoundPool.load(this.getApplicationContext(), R.raw.kakalib_scan, 1);
    }

    /**
     * 判断是否重新扫描，如果当前结果和扫描结果一致，则不用重新扫描
     * */
    private boolean onInterceptDecodeSucc(DecodeState copyState) {
        if (!getCurrentDecodeState().equals(copyState) || !canShowDialog) {
            restartScan();
            return true;
        }
        return false;
    }

    /**
     * 得到图片没有识别时，显示的Dialog的Title是未识别的二维码还是未识别的条形码
     * */
    private String getNotIdentificationTitle() {

            return "未识别的二维码";

    }

    /**
     * 根据当前状态重新开始
     * */
    private Runnable getResetRunnable(DecodeState state) {
        if (state.isGallery()) {
            return resetGalleryAction();
        } else {
            return resetAction();
        }
    }
}
