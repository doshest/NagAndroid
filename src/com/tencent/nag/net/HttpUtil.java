package com.tencent.nag.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.mime.HttpMultipartMode;
//
//import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;



import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class HttpUtil {
	public static HttpClient httpClient = new DefaultHttpClient();
	public static final String BASE_URL = "http://121.42.211.99:3000/msg/";
	public static final String BASE_URL_RETRIVE = "http://121.42.211.99:3000/back/";
	//public static final String BASE_SHARE_URL =BASE_URL+ "goods/share_goods.html?goods_id=";
	//public static final String BASE_SHARE_ESSAY_URL =BASE_URL+ "essay/share_essay.html?essay_id=";

	public static String getRequest(final String url) throws Exception{
		
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {

			@Override
			public String call() throws Exception {
				// TODO Auto-generated method stub
				HttpGet get = new HttpGet(url);
				//
				HttpResponse httpResponse = httpClient.execute(get);
				if(httpResponse.getStatusLine().getStatusCode() == 200){
					//成功
					String result = EntityUtils.toString(httpResponse.getEntity());
					return result;
				}else{
					Log.e("server error", "bad response");
					return "";
				}

			}			
		});
		
		new Thread(task).start();
		return task.get();
	}
	
	public static String PostRequest(final String url,final Map<String,String>rawParams) throws Exception{
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {

			@Override
			public String call() throws Exception {
				// TODO Auto-generated method stub
				HttpPost post = new HttpPost(url);
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				for(String key : rawParams.keySet()){
					params.add(new BasicNameValuePair(key, rawParams.get(key)));
				}
				post.setEntity(new UrlEncodedFormEntity(params,"utf-8"));
				
				HttpResponse response = httpClient.execute(post);
				if(response.getStatusLine().getStatusCode() == 200){
					String result = EntityUtils.toString(response.getEntity());
					return result;
					
				}else{
					Log.e("server error", "bad response");
					return "";
				}
		
			}
			
		});
		
		new Thread(task).start();
		return task.get();
		
	}
	
//	public static String uploadFile(final String url,final List<String> filePaths ,final Map<String,String>params) throws Exception{
//		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
//
//			@Override
//			public String call() throws Exception {
//				// TODO Auto-generated method stub
//				HttpPost post = new HttpPost(url);
//				post.setHeader("enctype","multipart/form-data");
//				//post.set
//				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			
//				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//				int count =0;
//				for(String f : filePaths){
//					builder.addBinaryBody("file"+count, new File(f));
//					count++;
//				}
//				
//				//添加正常参数非文件类型
//				ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);  
//				Iterator i = params.entrySet().iterator();
//				while(i.hasNext()){
//					Map.Entry<String, String> entry  = (Map.Entry<String, String>)i.next();
//				
//					builder.addTextBody(entry.getKey(), entry.getValue(),contentType);
//				}
//				
//				
//				HttpEntity entity = builder.build();
//				post.setEntity(entity);
//	
//				HttpResponse response = httpClient.execute(post);
//				if(response.getStatusLine().getStatusCode() == 200){
//					String result = EntityUtils.toString(response.getEntity());
//					return result;
//				}else{
//					Log.e("server error", "bad response");
//					return "";
//				}
//				
//			}
//			
//		});
//		
//		new Thread(task).start();
//		return task.get();
//	}
//	
//	public static Bitmap getBitmap(final String url) throws Exception{
//		ExecutorService exec = Executors.newFixedThreadPool(5);
//		FutureTask<Bitmap> task = new FutureTask<Bitmap>(new Callable<Bitmap>() {
//
//			@Override
//			public Bitmap call() throws Exception {
//				// TODO Auto-generated method stub
//				 Bitmap bitmap = null;
//				HttpClient client = new DefaultHttpClient();
//				HttpGet get = new HttpGet(url);
//				HttpResponse response = client.execute(get);
//				  // 如果服务器响应的是OK的话！
//				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//				   InputStream is = response.getEntity().getContent();
//				   bitmap = Bimp.downSizeBitmap(BitmapFactory.decodeStream(is),50);
//				   is.close();
//				 }
//				return bitmap;
//			}	
//		});
//
//		exec.submit(task);
//		return task.get();
//		
//	}
//	/**
//	 * 
//	 * @param url
//	 * @return 网络图片/文件流
//	 * @throws Exception
//	 */
//	public static InputStream getImageInputStream(final String url) throws Exception{
//		ExecutorService exec = Executors.newFixedThreadPool(5);
//		FutureTask<InputStream> task = new FutureTask<InputStream>(new Callable<InputStream>() {
//
//			@Override
//			public InputStream call() throws Exception {
//				 InputStream is = null;
//				HttpClient client = new DefaultHttpClient();
//				HttpGet get = new HttpGet(url);
//				HttpResponse response = client.execute(get);
//				  // 如果服务器响应的是OK的话！
//				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//				   is = response.getEntity().getContent();
//				   is.close();
//				 }
//				return is;
//			}	
//		});
//		exec.submit(task);
//		return task.get();
//	}

}
