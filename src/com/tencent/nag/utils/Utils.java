package com.tencent.nag.utils;

import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class Utils {
	public static boolean  isKeyBoardShowing(Activity context){
		if(context.getWindow().getAttributes().softInputMode==WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
		{
			return true;
		}
		return false;
	}
	
	 public static void hideInput(Context context,View view){
	        InputMethodManager inputMethodManager =
	        (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	    }
	 
	 public static int getScreenHeigth( Context ctx){
		 WindowManager wm = (WindowManager) ctx
                 .getSystemService(Context.WINDOW_SERVICE);

		 int width = wm.getDefaultDisplay().getWidth();
		 int height = wm.getDefaultDisplay().getHeight();
//		 DisplayMetrics metric = new DisplayMetrics();  
//		 ctx.getWindowManager().getDefaultDisplay().getMetrics(metric);  
//		 int width = metric.widthPixels;     // 屏幕宽度（像素）  
//		 int height = metric.heightPixels;
		 return height;
  
	 }
	 
	  /** 
	     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
	     */  
	    public static int dip2px(Context context, float dpValue) {  
	        final float scale = context.getResources().getDisplayMetrics().density;  
	        return (int) (dpValue * scale + 0.5f);  
	    }  
	  
	    /** 
	     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
	     */  
	    public static int px2dip(Context context, float pxValue) {  
	        final float scale = context.getResources().getDisplayMetrics().density;  
	        return (int) (pxValue / scale + 0.5f);  
	    }
	    
	    public static void showKeyboard(Context ctx,EditText view){
	    	InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);     
	    	imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN); 
	    	view.requestFocus();
	    }
	    
	    private static final int BLACK = 0xff000000;
	    
	    private static final int PADDING_SIZE_MIN = 10; // 最小留白长度, 单位: px
	     
	    public static Bitmap createQRCode(String str, int widthAndHeight) throws WriterException {
	        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
	        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
	        BitMatrix matrix = new MultiFormatWriter().encode(str,
	                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
	 
	        int width = matrix.getWidth();
	        int height = matrix.getHeight();
	        int[] pixels = new int[width * height];
	 
	        boolean isFirstBlackPoint = false;
	        int startX = 0;
	        int startY = 0;
	 
	        for (int y = 0; y < height; y++) {
	            for (int x = 0; x < width; x++) {
	                if (matrix.get(x, y)) {
	                    if (isFirstBlackPoint == false)
	                    {
	                        isFirstBlackPoint = true;
	                        startX = x;
	                        startY = y;
	                    }
	                    pixels[y * width + x] = BLACK;
	                }else{
	                	pixels[y * width + x] = 0xFFFFFFFF;
	                }
	            }
	        }
	 
	        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
	         
	        // 剪切中间的二维码区域，减少padding区域
	        if (startX <= PADDING_SIZE_MIN) return bitmap;
	 
	        int x1 = startX - PADDING_SIZE_MIN;
	        int y1 = startY - PADDING_SIZE_MIN;
	        if (x1 < 0 || y1 < 0) return bitmap;
	 
	        int w1 = width - x1 * 2;
	        int h1 = height - y1 * 2;
	 
	        Bitmap bitmapQR = Bitmap.createBitmap(bitmap, x1, y1, w1, h1);
	         
	        return bitmapQR;
	    }
	    public static String getPhoneId(Context ctx){
	    	TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
	    	return tm.getDeviceId();
	    }
	    
	 
	 
}
