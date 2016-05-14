package com.tencent.nag.qrcode.view;



import com.example.facedemo.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * ���ɨ��Ԥ������.
 * 
 * @author
 */
@SuppressWarnings("unused")
public class ViewFinderView extends FrameLayout {

    private static final long ANIMATION_DELAY = 15L;
    private static final long SCAN_LINE_DELAY = 50L;

    private static int SCAN_LINE_STEP;

    private Context context;

    private int scanLineOffsetY = 0;

    private int mQrcodeFrameWidth;
    private int mQrcodeFrameHeight;

    private int mFrameBorderWidth;

    private int mQrcodeScanWidth;
    private int mQrcodeScanHeight;


    private Drawable mScanFrameDrawable;
    private Drawable mQrcodeScanLineDrawable;

    private RelativeLayout rlScanFrame;
    private ImageView ivScanLine;
    private TextView tvTips;

    private DrawRunnable mDrawRunnable;
    private DrawUIRunnable mDrawUIRunnable;
    private ToQrcodeRunnable toQrcodeRun;

    private Paint paint;

    private Handler mHandler;
    private HandlerThread mHandlerThread;
    
    /** ʶ��ɹ�ʱ�򵯳����ڴ����dialog */
    private Dialog mPgDialog;


    public ViewFinderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewFinderView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        SCAN_LINE_STEP = getResources().getDimensionPixelSize(R.dimen.qrcode_scan_step);

        this.mQrcodeFrameWidth = context.getResources().getDimensionPixelSize(R.dimen.qrcode_frame_width);
        this.mQrcodeFrameHeight = context.getResources().getDimensionPixelSize(R.dimen.qrcode_frame_height);
        this.mScanFrameDrawable = getResources().getDrawable(R.drawable.qrcode_border);
        this.mQrcodeScanLineDrawable = getResources().getDrawable(R.drawable.qrcode_scanline_qrcode);
   
        this.mQrcodeScanWidth = context.getResources().getDimensionPixelSize(R.dimen.qrcode_scanning_width);
        this.mQrcodeScanHeight = context.getResources().getDimensionPixelSize(R.dimen.qrcode_scanning_height);
        
        this.mFrameBorderWidth = getResources().getDimensionPixelSize(R.dimen.qrcode_frame_border_width);

        setWillNotDraw(false);
        paint = new Paint();
        paint.setColor(0x00333333);

        LayoutInflater.from(getContext()).inflate(R.layout.qrcode_view, this);
        View parent = getChildAt(0);
        FrameLayout.LayoutParams parentlp = (FrameLayout.LayoutParams) parent.getLayoutParams();
        parentlp.bottomMargin = context.getResources().getDimensionPixelSize(R.dimen.qrcode_flash_light_height)
                + context.getResources().getDimensionPixelSize(R.dimen.qrcode_flash_light_margin_bottom)
                + context.getResources().getDimensionPixelSize(R.dimen.qrcode_bottom_tab_height)
                + context.getResources().getDimensionPixelSize(R.dimen.qrcode_tips_margin_top);

        rlScanFrame = (RelativeLayout) findViewById(R.id.scanFrame);
        updateFrameView(rlScanFrame, mQrcodeFrameWidth, mQrcodeFrameHeight);
        ivScanLine = (ImageView) findViewById(R.id.scanLine);
        scanLineOffsetY = -1 * mQrcodeScanHeight;
        updateScanView(ivScanLine, scanLineOffsetY);

        tvTips = (TextView) findViewById(R.id.tips);
        tvTips.setText(R.string.qrcode_tips_text_unique);
        
        if(mPgDialog!=null && mPgDialog.isShowing()){
            mPgDialog.dismiss();
        }

        this.toQrcodeRun = new ToQrcodeRunnable();
        this.mDrawRunnable = new DrawRunnable();

        this.mDrawUIRunnable = new DrawUIRunnable();
        this.mDrawUIRunnable.setTopOffset(scanLineOffsetY);
        this.mDrawUIRunnable.setView(ivScanLine);

        mHandlerThread = new HandlerThread("ViewFinderView");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHandler != null) {
            mHandler.getLooper().quit();
        }
        mHandler = null;
        mHandlerThread = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        int left;
        int top;
        int right;
        int bottom;

        left = rlScanFrame.getLeft();
        top = rlScanFrame.getTop();
        right = rlScanFrame.getRight();
        bottom = rlScanFrame.getBottom();

        canvas.save();
       
        paint.setColor(0x80808080);
        canvas.drawRect(0, 0, left, height, paint);
        canvas.drawRect(right, 0, width, height, paint);
        canvas.drawRect(left, 0, right, top, paint);
        canvas.drawRect(left, bottom, right, height, paint);
        paint.setColor(0x00333333);
        RectF leftRect = new RectF(left, top, left + mFrameBorderWidth, bottom);
        canvas.drawRoundRect(leftRect, 1f, 1f, paint);

        RectF rightRect = new RectF(right - mFrameBorderWidth, top, right, bottom);
        canvas.drawRoundRect(rightRect, 1f, 1f, paint);

        canvas.drawRect(left, top, right, top + mFrameBorderWidth, paint);

        canvas.drawRect(left, bottom - mFrameBorderWidth, right, bottom, paint);

        canvas.restore();
    }

    private void postRunnable(Handler handler, Runnable r, long time) {
        Handler h = handler;
        if (h != null) {
            if (time == 0) {
                h.post(r);
            } else {
                h.postDelayed(r, time);
            }
        }
    }

    private void cancelRunnable(Handler handler, Runnable r) {
        Handler h = handler;
        if (h != null) {
            h.removeCallbacks(r);
        }
    }

    private void updateFrameView(View frameView, int widht, int height) {
        frameView.setBackgroundDrawable(mScanFrameDrawable);
        RelativeLayout.LayoutParams framelp = (RelativeLayout.LayoutParams) frameView.getLayoutParams();
        framelp.width = widht;
        framelp.height = height;
        frameView.setLayoutParams(framelp);
    }

    private void updateScanView( ImageView scanView, int topOffset) {

   
            scanView.setImageDrawable(mQrcodeScanLineDrawable);
            FrameLayout.LayoutParams scanlp = (FrameLayout.LayoutParams) scanView.getLayoutParams();
            scanView.layout(-1, topOffset, mQrcodeScanWidth - 3, topOffset + mQrcodeScanHeight);
    }

    private void postUpdateScanView( final ImageView scanView, final int topOffset) {
        mDrawUIRunnable.setView(scanView);
        mDrawUIRunnable.setTopOffset(topOffset);
        cancelRunnable(getHandler(), mDrawUIRunnable);
        postRunnable(getHandler(), mDrawUIRunnable, 0);
    }

    public void startDrawViewfinder() {
        cancelRunnable(getHandler(), mDrawUIRunnable);
        cancelRunnable(mHandler, mDrawRunnable);
        postRunnable(mHandler, mDrawRunnable, 0);
    }

    private class DrawUIRunnable implements Runnable {

    
        private ImageView mView;
        private int mTopOffset;

        @Override
        public void run() {
            updateScanView( mView, mTopOffset);
        }

    

        public void setView(ImageView iv) {
            this.mView = iv;
        }

        public void setTopOffset(int offset) {
            this.mTopOffset = offset;
        }
    }

    private class DrawRunnable implements Runnable {

        @Override
        public void run() {

            if (ivScanLine.getVisibility() == VISIBLE) {
                    if (scanLineOffsetY >= (mQrcodeFrameHeight - mQrcodeScanHeight)) {
                        scanLineOffsetY = -1 * mQrcodeScanHeight;
                    }
                postUpdateScanView( ivScanLine, scanLineOffsetY);

                scanLineOffsetY += SCAN_LINE_STEP;
            }

            postRunnable(mHandler, this, SCAN_LINE_DELAY);
        }

    }

    private class ToQrcodeRunnable implements Runnable {

        private volatile boolean isRunning = true;
        private int deltaX;
        private int deltaY;

        public ToQrcodeRunnable() {
        }

        @Override
        public void run() {

            if (isRunning) {
                int width = rlScanFrame.getWidth();
                int height = rlScanFrame.getHeight();
                if (width != mQrcodeFrameWidth || height != mQrcodeFrameHeight) {

                    width += deltaX;
                    height += deltaY;

                    if (width < mQrcodeFrameWidth) {
                        width = mQrcodeFrameWidth;
                    }
                    if (height > mQrcodeFrameHeight) {
                        height = mQrcodeFrameHeight;
                    }

                    updateFrameView(rlScanFrame, width, height);

                    postRunnable(getHandler(), this, ANIMATION_DELAY);
                } else {
                    ivScanLine.setVisibility(VISIBLE);
                    tvTips.setText(R.string.qrcode_tips_text);
                    tvTips.setVisibility(VISIBLE);
                    ((View) tvTips.getParent()).setVisibility(VISIBLE);

                    startDrawViewfinder();
                }
            }
        }

        public void start() {
            isRunning = true;
            postRunnable(getHandler(), this, 0);
        }

        public void cancel() {
            isRunning = false;
            int width = rlScanFrame.getWidth();
            int height = rlScanFrame.getHeight();

            if (width != mQrcodeFrameWidth || height != mQrcodeFrameHeight) {
                ViewGroup.LayoutParams lp = rlScanFrame.getLayoutParams();
                rlScanFrame.setLayoutParams(lp);
            }
        }
    }

    public Rect getFrameReleativeRect() {
        Rect rect = new Rect();
        rect.left = rlScanFrame.getLeft();
        rect.right = rlScanFrame.getRight();
        rect.bottom = rlScanFrame.getBottom();
        rect.top = rlScanFrame.getTop();
        return rect;
    }


    /**
     * ʶ��ͼƬʱ������ʶ���е�Dialog
     * */
    public void setProcessBarVisuable() {
        if (mPgDialog == null) {
            mPgDialog =createProgressDialog(R.string.qr_loading, context,
                    1);
        }
        if (mPgDialog.isShowing()) {
            return;
        }
        mPgDialog.show();
    }

    /**
     * ʶ�����ʶ����Dialog��ʧ
     * */
    public void setProcessBarInVisuable() {
        if (mPgDialog != null && mPgDialog.isShowing()) {
            mPgDialog.dismiss();
        }
    }
    
    public static Dialog createProgressDialog( int res, Context a, int style ) {
        Dialog mPgDialog = new Dialog(a, R.style.TransparentDialog);
        View pgLayout = createLoadingLayout(res, a);
        mPgDialog.setContentView(pgLayout);
        mPgDialog.setCancelable(true);
        return mPgDialog;
    }
    
    public static View createLoadingLayout( int res, Context a ) {
        View pgLayout = LayoutInflater.from(a).inflate(R.layout.toast_progress_text, null);
        TextView tv = (TextView) pgLayout.findViewById(R.id.toast_textview);
        tv.setText(res);

        return pgLayout;
    }
}

