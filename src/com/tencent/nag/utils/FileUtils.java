package com.tencent.nag.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

/**
 * 
 ******************************************
 * @文件名称	:  FileUtils.java
 * @创建时间	: 2013-1-27 下午02:35:09
 * @文件描述	: 文件工具类
 ******************************************
 */
public class FileUtils {
	/**
	 * 读取表情配置文件
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getEmojiFile(Context context) {
		try {
			List<String> list = new ArrayList<String>();
			InputStream in = context.getResources().getAssets().open("emoji");// �ļ�����Ϊrose.txt
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String FILE_DIR_QRCODE = Environment.getExternalStorageDirectory().toString()+"/nag/qr_imgs/";
	/** 保存方法 */
	 public static  String saveBitmap(String picName,Bitmap bm,Context c) {
		 File dir = new File(FILE_DIR_QRCODE);
		 if(!dir.exists()){
			 System.out.println("dfsdfsdf");
			 dir.mkdirs();
		 }
	  File f = new File(FILE_DIR_QRCODE, picName);
	  
	  if (f.exists()) {
		  
	   f.delete();
	  }
	  try {
	   FileOutputStream out = new FileOutputStream(f);
	   bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
	   out.flush();
	   out.close();
	  } catch (FileNotFoundException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  } catch (IOException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }
	  
	  Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	  Uri uri = Uri.fromFile(f);
	  intent1.setData(uri);
	  c.sendBroadcast(intent1);
	  return uri.toString();

	 }
}
