package com.tencent.nag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.dosh.swaplistview.SwipeMenu;
import com.dosh.swaplistview.SwipeMenuCreator;
import com.dosh.swaplistview.SwipeMenuItem;
import com.dosh.swaplistview.SwipeMenuListView;
import com.example.facedemo.R;
import com.google.zxing.WriterException;
import com.sina.barcode.QrECLevel;
import com.sina.barcode.QrEncoder;
import com.tencent.nag.adapter.RoomListAdapter;
import com.tencent.nag.adapter.RoomModel;
import com.tencent.nag.db.ChatDataModel;
import com.tencent.nag.db.DataHelper;
import com.tencent.nag.qrcode.CaptureActivity;
import com.tencent.nag.qrcode.DecodeResult;
import com.tencent.nag.utils.FaceConversionUtil;
import com.tencent.nag.utils.FileUtils;
import com.tencent.nag.utils.Utils;
import com.tencent.nag.view.AddPopWindow;
import com.tencent.nag.view.AndroidShare;
import com.tencent.nag.view.CustomDialog;

@SuppressLint("NewApi") 
public class MainActivity extends Activity implements View.OnClickListener {

	private SwipeMenuListView mRoomList;
	private  RoomListAdapter mAdapter;
	private TextView mTitleText;
	private DataHelper helper;
	private ImageButton rightButton;
	private View mTitleLeft;
	private AddPopWindow mPopup;
	private View emptyView;
	
	private List<RoomModel> mDatas ;
	public static int QRCODE_RES = 0x124;
	public static final String ROOM_ID_LABEL = "room_id";
	public static final String ROOM_NAME_LABEL = "room_name";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//			}
//		}).start();
		new FaceTask().execute();
		initviews();
	}
	
	private class FaceTask extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			if(FaceConversionUtil.getInstace().emojiLists.size()>0){
				return null;
			}
			FaceConversionUtil.getInstace().getFileText(MainActivity.this);
			
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			
			System.out.println("over");
			super.onPostExecute(result);
		}
		
	}

	private void initviews() {
		
		mTitleLeft = findViewById(R.id.title_bar_left_button);
		mTitleLeft.setVisibility(View.GONE);
		
		emptyView = findViewById(R.id.emptyView);
		
		

		
		mTitleText = (TextView) findViewById(R.id.title_name);
		mTitleText.setText("房间列表");
		rightButton = (ImageButton) findViewById(R.id.title_right_button);
		rightButton.setVisibility(View.VISIBLE);
		rightButton.setImageDrawable(getResources().getDrawable(R.drawable.add_room));
		rightButton.setOnClickListener(this);
		mRoomList = (SwipeMenuListView) findViewById(R.id.room_list);
		initListViewData();
		
		mRoomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				// TODO Auto-generated method stub
				enterChatRoom(mDatas.get(position).roomId,mDatas.get(position).roomName);
				
			}
		});
		
	}
	
	private void enterChatRoom(String roomId,String roomName){
		
		if(TextUtils.isEmpty(roomName)){
			roomName = "Nag";
		}
		if(!helper.HaveChatDataModel(roomId)){//本地没有房间创建一个书库
			ChatDataModel chatData = new ChatDataModel();
			chatData.setRoomId(roomId);
			chatData.setRoomName(roomName);
			chatData.setChatMsgEntryJsonString("");
			helper.SaveChatDataModel(chatData);
		}
		
		Intent intent1 = new Intent(MainActivity.this, ChatActivity.class);
		intent1.putExtra(ROOM_ID_LABEL, roomId);
		intent1.putExtra(ROOM_NAME_LABEL, roomName);
		startActivity(intent1);
		finish();
	}
	
	private Drawable getRoomDrawable(int type,String name){
		Drawable res = null;
		System.out.println("sssssss------->"+name);
		int drawableId = 0;
		if(name.contains("Nag")){
			res = getResources().getDrawable(R.drawable.ic_launcher);
			return res;
		}else{
			switch (type%10) {
			case 0:
				drawableId = R.drawable.room_0;
				break;
			case 1:
				drawableId = R.drawable.room_1;
				break;
			case 2:
				drawableId = R.drawable.room_2;
				break;
			case 3:
				drawableId = R.drawable.room_3;
				break;
			case 4:
				drawableId = R.drawable.room_4;
				break;
			case 5:
				drawableId = R.drawable.room_5;
				break;
			case 6:
				drawableId = R.drawable.room_6;
				break;
			case 7:
				drawableId = R.drawable.room_7;
				break;
			case 8:
				drawableId = R.drawable.room_8;
				break;
			case 9:
				drawableId = R.drawable.room_9;
				break;
			default:
				drawableId = R.drawable.ic_launcher;
				break;
			}
		}
		res = getResources().getDrawable(drawableId);
		return res;
	}
	private void initListViewData(){
		helper = new DataHelper(this);
		List<ChatDataModel> data = helper.getchatDataList();
		
		if(data==null || data.size() == 0){
			emptyView.setVisibility(View.VISIBLE);
			mRoomList.setVisibility(View.GONE);
		
		}else{
			emptyView.setVisibility(View.GONE);
			mRoomList.setVisibility(View.VISIBLE);
		}
		//System.out.println(data.size());
		mDatas= new ArrayList<RoomModel>();
		for(ChatDataModel m :data){
			RoomModel roomModel = new RoomModel();
			roomModel.roomId = m.getRoomId();
		
			roomModel.roomName = m.getRoomName();
			int type = Integer.parseInt(roomModel.roomId);
			
			roomModel.roomDrawable = getRoomDrawable(type, roomModel.roomName);
			mDatas.add(roomModel);
		}
		
		mAdapter = new RoomListAdapter(this, mDatas);
		mRoomList.setAdapter(mAdapter);
		
		SwipeMenuCreator creator = new SwipeMenuCreator() {
			
			@Override
			public void create(SwipeMenu menu) {
				 SwipeMenuItem item1 = new SwipeMenuItem(
	                        getApplicationContext());
	                item1.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
	                        0xCE)));
	                item1.setWidth(Utils.dip2px(MainActivity.this, 90));
	                item1.setIcon(R.drawable.ic_action_share);
	                menu.addMenuItem(item1);
	                
	             // create "delete" item
	                SwipeMenuItem deleteItem = new SwipeMenuItem(
	                        getApplicationContext());
	                // set item background
	                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
	                        0x3F, 0x25)));
	                // set item width
	                deleteItem.setWidth(Utils.dip2px(MainActivity.this, 90));
	                // set a icon
	                deleteItem.setIcon(R.drawable.ic_delete);
	                // add to menu
	                menu.addMenuItem(deleteItem);
				
			}
		};
		mRoomList.setMenuCreator(creator);
		
		mRoomList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                RoomModel item = mDatas.get(position);
                switch (index) {
                    case 0:
                        // share
                    	System.out.println("sahre click");
                    	
                    	new ShareTask().execute(item.roomId,item.roomName);
                        break;
                    case 1:
                        // delete
//					delete(item);
                    	System.out.println("click delete");
                    	  helper.delChatDataModel(mDatas.get(position).roomId);
                        mDatas.remove(position);
                     
                        
                        mAdapter.notifyDataSetChanged();
                        if(mDatas.size()==0){
                        	emptyView.setVisibility(View.VISIBLE);
                        }
                        break;
                }
                return false;
            }
        });
		mRoomList.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            	System.out.println(" swipe start");
            }

            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            	System.out.println(" swipe end");
            }
        });
		mRoomList.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {
            	System.out.println(" open "+position);
            }

            @Override
            public void onMenuClose(int position) {
            	System.out.println(" close "+position);
            }
        });
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == QRCODE_RES) {

			DecodeResult res = (DecodeResult) data
					.getSerializableExtra("result");
			String content = res.getText();
			System.out.println(content+" "+(content.split(":")[1].length()<5)+" "+content.split(":").length+(content.split(":").length != 2)+" "+(!isNumeric(content.split(":")[0])));
			if(!content.contains(":")
					||content.split(":")[0].length()<5
					||content.split(":").length != 2
					||!isNumeric(content.split(":")[0])){
				Toast.makeText(this, "无效的房间二维码,请重新扫描", Toast.LENGTH_LONG).show();
				return;
			}
			
			String[] resString = content.split(":");
			String roomId = resString[0];
			String roomName = resString[1];
			
			if(!TextUtils.isEmpty(roomId)&&!TextUtils.isEmpty(roomName)){
				enterChatRoom(roomId, roomName);
				
			}
			
			
			//int type = res.getFormat();
		//	result.setText("扫描结果：类型：" + type + "，内容：" + content);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public boolean isNumeric(String str){ 
		   Pattern pattern = Pattern.compile("[0-9]*"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
		}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		
		case R.id.title_right_button:
				showPopupWindow();
			break;

		default:
			break;
		}

	}
	

	
	
	private void showPopupWindow(){
		if(mPopup == null){
			mPopup = new AddPopWindow(this);
			mPopup.setItemClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mPopup.dismiss();
					Intent intent2 = new Intent(MainActivity.this,CaptureActivity.class);
					startActivityForResult(intent2, QRCODE_RES);
					
				}
			}, new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mPopup.dismiss();
					showDialog();
				}
			});
		}
		mPopup.showAsDropDown(rightButton,-10,10,Gravity.RIGHT);
		//mPopup.showPopupWindow(rightButton);
		
	}
	
	  void showDialog(){
		  final CustomDialog.Builder builder = new CustomDialog.Builder(this);  
	        builder.setTitle("请输入房间号");  
	        builder.setPositiveButton("加入", new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialog, int which) {  
	                
	                String roomId = builder.getRoomId();
	                if(!TextUtils.isEmpty(roomId)&&roomId.length()>4){
	                	enterChatRoom(roomId,"");
	                	dialog.dismiss();  
	                	
	                }else{
	                	Toast.makeText(MainActivity.this, "请输入正确房间号", Toast.LENGTH_SHORT).show();;
	                }
	            }  
	        });  
	  
	        builder.setNegativeButton("取消",  
	                new android.content.DialogInterface.OnClickListener() {  
	                    public void onClick(DialogInterface dialog, int which) {  
	                        dialog.dismiss();  
	                    }  
	                });  
	  
	        builder.create().show(); 
	    }
	  
	  private class ShareTask extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			Bitmap bmp = null;
			try {
				System.out.println("pa  "+params[0]+":"+params[1]);
				bmp = Utils.createQRCode(params[0]+":"+params[1], 150);
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String res = FileUtils.saveBitmap(params[0]+".png", bmp, MainActivity.this);
			return res;
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			System.out.println("sssssss--->"+result);
			AndroidShare as = new AndroidShare(MainActivity.this,"分享房间",result);
			as.show();
			super.onPostExecute(result);
		}
		  
	  }
}
